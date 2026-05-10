package com.zst.senior.assistant.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zst.senior.assistant.repository.Seniorm2Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.zst.senior.assistant.model.Seniorm2Item
import com.zst.senior.assistant.utils.NotificationReceiver
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * ViewModel odpowiedzialny za zarządzanie cyklicznymi przypomnieniami (np. o lekach, rutynach).
 *
 * Klasa łączy w sobie zapis/odczyt danych z chmury za pośrednictwem [Seniorm2Repository]
 * z precyzyjnym planowaniem lokalnych powiadomień na urządzeniu za pomocą [AlarmManager].
 * Zapewnia, że zadania ustawione na konkretne dni tygodnia będą niezawodnie budzić aplikację
 * i powiadamiać użytkownika.
 *
 * @param application Kontekst aplikacji.
 * @property repository Repozytorium obsługujące operacje na danych przypomnień.
 */
@RequiresApi(Build.VERSION_CODES.O)
class Seniorm2ViewModel(
    application: Application,
    private val repository: Seniorm2Repository = Seniorm2Repository()
) : AndroidViewModel(application) {

    /** Strumień przechowujący listę wszystkich przypomnień cyklicznych użytkownika. */
    private val _seniorm2Items = MutableStateFlow<List<Seniorm2Item>>(emptyList())
    val seniorm2Items = _seniorm2Items.asStateFlow()

    /** Flaga ładowania danych z bazy. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val context: Context
        get() = getApplication<Application>().applicationContext

    /** Systemowy menedżer alarmów do planowania wybudzeń i powiadomień. */
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        loadReminders()
    }

    /**
     * Pobiera listę przypomnień z repozytorium i konfiguruje alarmy systemowe.
     */
    private fun loadReminders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val items = repository.getReminders()
                _seniorm2Items.value = items.sortedBy { it.time }

                items.forEach { item ->
                    setupAlarms(item)
                }
            } catch (e: Exception) {
                Log.e("Seniorm2ViewModel", "Błąd ładowania: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Dodaje nowe przypomnienie cykliczne do bazy danych, a po pomyślnym zapisie
     * nakręca systemowe alarmy na wybrane dni tygodnia.
     *
     * @param name Nazwa przypomnienia (np. "Weź leki").
     * @param time Czas przypomnienia w formacie 24h ("HH:mm").
     * @param daysOfWeek Zbiór dni tygodnia, w których przypomnienie ma być aktywne.
     */
    fun addSeniorm2(name: String, time: String, daysOfWeek: Set<DayOfWeek>) {
        viewModelScope.launch {
            try {
                val newId = repository.addReminder(name, time, daysOfWeek)
                val newItem = Seniorm2Item(id = newId, name = name, time = time, daysOfWeek = daysOfWeek)
                _seniorm2Items.update { (it + newItem).sortedBy { item -> item.time } }
                setupAlarms(newItem)
            } catch (e: Exception) {
                Log.e("Seniorm2ViewModel", "Błąd dodawania: ${e.message}")
            }
        }
    }

    /**
     * Aktualizuje istniejące przypomnienie.
     * Anuluje wszystkie dotychczasowe alarmy dla tego zadania i ustawia nowe,
     * zgodnie z wprowadzonymi poprawkami.
     *
     * @param updatedItem Zmodyfikowany obiekt przypomnienia.
     */
    fun updateSeniorm2(updatedItem: Seniorm2Item) {
        viewModelScope.launch {
            try {
                repository.updateReminder(updatedItem)

                for (day in DayOfWeek.entries) {
                    cancelWeeklyRepeatingNotification(updatedItem.id, day)
                }
                setupAlarms(updatedItem)

                _seniorm2Items.update { currentList ->
                    currentList.map {
                        if (it.id == updatedItem.id) updatedItem else it
                    }.sortedBy { item -> item.time }
                }
            } catch (e: Exception) {
                Log.e("Seniorm2ViewModel", "Błąd aktualizacji: ${e.message}")
            }
        }
    }

    /**
     * Usuwa przypomnienie cykliczne z bazy oraz całkowicie wyrejestrowuje je z [AlarmManager].
     *
     * @param itemToDelete Przypomnienie, które ma zostać skasowane.
     */
    fun deleteSeniorm2(itemToDelete: Seniorm2Item) {
        viewModelScope.launch {
            try {
                repository.deleteReminder(itemToDelete.id)

                for (day in DayOfWeek.entries) {
                    cancelWeeklyRepeatingNotification(itemToDelete.id, day)
                }
                _seniorm2Items.update { currentList ->
                    currentList.filterNot { it.id == itemToDelete.id }
                }
            } catch (e: Exception) {
                Log.e("Seniorm2ViewModel", "Błąd usuwania: ${e.message}")
            }
        }
    }

    /** Helper rejestrujący powiadomienia dla każdego wybranego dnia tygodnia. */
    private fun setupAlarms(item: Seniorm2Item) {
        for (day in item.daysOfWeek) {
            scheduleWeeklyRepeatingNotification(item.time, item.name, item.id, day)
        }
    }

    /**
     * Generuje unikalny kod intencji powiadomienia (Request Code) oparty na identyfikatorze
     * dokumentu z bazy oraz konkretnym dniu tygodnia. Zapobiega to nadpisywaniu się powiadomień.
     */
    private fun getUniqueAlarmId(itemId: String, day: DayOfWeek): Int {
        return (itemId + day.name).hashCode()
    }

    /**
     * Oblicza dokładny czas następnego wystąpienia wybranego dnia i godziny, po czym zleca
     * systemowi wybudzenie o tym czasie.
     * Przekazuje flagę `IS_RECURRING`, by `NotificationReceiver` wiedział, że musi odnowić ten alarm na kolejny tydzień.
     *
     * @param time Godzina (HH:mm).
     * @param title Nazwa przypomnienia (np. "Wypij wodę").
     * @param itemId ID dokumentu Firebase.
     * @param day Konkretny dzień tygodnia.
     */
    private fun scheduleWeeklyRepeatingNotification(time: String, title: String, itemId: String, day: DayOfWeek) {
        val localTime = try {
            LocalTime.parse(time)
        } catch (_: Exception) {
            return
        }

        val now = LocalDateTime.now()
        var nextTriggerDateTime = now.with(TemporalAdjusters.nextOrSame(day)).with(localTime)

        // Jeśli czas już minął w tym tygodniu, przeskocz na następny tydzień
        if (nextTriggerDateTime.isBefore(now)) {
            nextTriggerDateTime = nextTriggerDateTime.plusWeeks(1)
        }

        val triggerAtMillis = nextTriggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmId = getUniqueAlarmId(itemId, day)

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "SENIORM1_ALARM"
            putExtra("NOTIFICATION_TITLE", "Pora na: $title")
            putExtra("NOTIFICATION_MESSAGE", "Zaplanowane na $time")
            putExtra("NOTIFICATION_ID", alarmId)
            putExtra("IS_RECURRING", true) // Ważne: Oznacza, że Receiver zapętli ten alarm za 7 dni!
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Używamy najmocniejszego dostępnego w Androidzie alarmu wybudzającego
            // (setExactAndAllowWhileIdle przebija się przez mechanizm Doze Mode).
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.d("Seniorm2ViewModel", "Ustawiono DOKŁADNY alarm wybudzający: $title na $day o $time")
        } catch (e: SecurityException) {
            // Wyjątek bezpieczeństwa na Android 14+ jeśli aplikacja straciła pozwolenie EXACT_ALARM
            Log.e("Seniorm2ViewModel", "Błąd ustawiania alarmu: ${e.message}")
        }
    }

    /**
     * Anuluje wcześniej zaplanowane powiadomienie z `AlarmManager`, znajdując je po jego
     * unikalnym `alarmId` wyliczonym dla konkretnego dnia tygodnia.
     */
    private fun cancelWeeklyRepeatingNotification(itemId: String, day: DayOfWeek) {
        val alarmId = getUniqueAlarmId(itemId, day)
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "SENIORM1_ALARM"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}