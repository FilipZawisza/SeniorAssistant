package com.zst.senior.assistant.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zst.senior.assistant.model.CalendarEvent
import com.zst.senior.assistant.repository.CalendarRepository
import com.zst.senior.assistant.utils.NotificationReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * ViewModel zarządzający kalendarzem i jednorazowymi wydarzeniami w aplikacji.
 *
 * Klasa integruje synchronizację z chmurą za pomocą [CalendarRepository]
 * z lokalnym systemem powiadomień urządzenia ([AlarmManager]).
 *
 * @param application Kontekst aplikacji.
 * @param authViewModel ViewModel autoryzacji.
 * @property repository Repozytorium obsługujące operacje na danych kalendarza.
 */
@RequiresApi(Build.VERSION_CODES.O)
class SeniorCalendarViewModel(
    application: Application,
    private val authViewModel: AuthViewModel,
    private val repository: CalendarRepository = CalendarRepository()
) : AndroidViewModel(application) {

    private val _events = MutableStateFlow<Map<LocalDate, List<CalendarEvent>>>(emptyMap())
    val events = _events.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        viewModelScope.launch {
            authViewModel.userRole.collect { role ->
                if (role != null) loadEvents()
                else _events.value = emptyMap()
            }
        }
    }

    /**
     * Pobiera wszystkie wydarzenia zaplanowane dla aktualnie zalogowanego użytkownika.
     */
    fun loadEvents() {
        val userId = authViewModel.currentUserId.value ?: return
        val role = authViewModel.userRole.value ?: return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                _events.value = repository.getEvents(userId, role)
            } catch (e: Exception) {
                _error.value = "Błąd ładowania wydarzeń: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Zapisuje nowe wydarzenie w chmurze i planuje powiadomienie przypominające.
     *
     * @param date Data wydarzenia.
     * @param time Godzina wydarzenia (HH:mm).
     * @param title Tytuł wydarzenia.
     */
    fun addEvent(date: LocalDate, time: String, title: String) {
        val userId = authViewModel.currentUserId.value ?: return
        val role = authViewModel.userRole.value ?: return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val docId = repository.addEvent(userId, role, date, time, title)
                scheduleNotification(date, time, title, docId.hashCode().toLong())
                loadEvents()
            } catch (e: Exception) {
                _error.value = "Błąd dodawania wydarzenia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Usuwa wydarzenie z chmury i anuluje zaplanowany alarm.
     *
     * @param event Obiekt wydarzenia do usunięcia.
     */
    fun deleteEvent(event: CalendarEvent) {
        val role = authViewModel.userRole.value ?: return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteEvent(role, event.firebaseDocId)
                cancelNotification(event)
                loadEvents()
            } catch (e: Exception) {
                _error.value = "Błąd usuwania wydarzenia: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Czyści aktualny stan błędu. */
    fun clearError() {
        _error.value = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleNotification(date: LocalDate, time: String, title: String, eventId: Long) {
        val localTime = try { LocalTime.parse(time) } catch (_: Exception) { return }
        val dateTime = LocalDateTime.of(date, localTime)
        val triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (triggerAtMillis < System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "EVENT_ALARM"
            putExtra("NOTIFICATION_TITLE", "Przypomnienie: $title")
            putExtra("NOTIFICATION_MESSAGE", "Zaplanowane na $time")
            putExtra("NOTIFICATION_ID", eventId.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelNotification(event: CalendarEvent) {
        val eventId = event.firebaseDocId.hashCode().toLong()
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "EVENT_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /** Fabryka dla [SeniorCalendarViewModel]. */
    class Factory(
        private val application: Application,
        private val authViewModel: AuthViewModel
    ) : ViewModelProvider.Factory {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SeniorCalendarViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SeniorCalendarViewModel(application, authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
