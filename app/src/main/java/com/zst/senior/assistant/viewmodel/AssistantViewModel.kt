package com.zst.senior.assistant.viewmodel

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.AiResponse
import com.zst.senior.assistant.model.ChatMessage
import com.zst.senior.assistant.model.ParsedResult
import com.zst.senior.assistant.model.ParsedRecurringResult
import com.zst.senior.assistant.repository.AssistantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * Główny ViewModel sterujący asystentem AI w aplikacji.
 *
 * Pełni rolę pośrednika (mostu) pomiędzy interfejsem użytkownika (UI), silnikiem AI (Gemini),
 * a widokami modeli odpowiedzialnymi za faktyczny zapis danych w kalendarzu lub liście zadań cyklicznych.
 *
 * @property calendarViewModel ViewModel zarządzający jednorazowymi wydarzeniami w kalendarzu.
 * @property seniorm2ViewModel ViewModel zarządzający cyklicznymi przypomnieniami (np. leki).
 * @property assistantRepository Repozytorium obsługujące komunikację z silnikiem AI.
 */
class AssistantViewModel(
    private val application: Application,
    private val calendarViewModel: SeniorCalendarViewModel,
    private val seniorm2ViewModel: Seniorm2ViewModel,
    private val assistantRepository: AssistantRepository = AssistantRepository()
) : AndroidViewModel(application) {

    /** Stan przechowujący ostatnią odpowiedź asystenta do wyświetlenia na ekranie. */
    private val _lastResponse = MutableStateFlow<ChatMessage?>(null)
    val lastResponse: StateFlow<ChatMessage?> = _lastResponse

    /** Flaga informująca UI, czy aktualnie trwa przetwarzanie zapytania przez AI. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** Przechowuje tymczasowo rozkodowane przez AI jednorazowe wydarzenie, czekające na akceptację użytkownika. */
    private val _pendingEvent = MutableStateFlow<ParsedResult?>(null)
    val pendingEvent: StateFlow<ParsedResult?> = _pendingEvent

    /** Przechowuje tymczasowo rozkodowane przez AI zadanie cykliczne, czekające na akceptację użytkownika. */
    private val _pendingRecurring = MutableStateFlow<ParsedRecurringResult?>(null)
    val pendingRecurring: StateFlow<ParsedRecurringResult?> = _pendingRecurring

    /**
     * Wysyła tekst wprowadzony przez użytkownika (z klawiatury lub z mowy) do analizy przez AI.
     *
     * @param text Surowy tekst komendy od użytkownika.
     * @param language Kod języka użytkownika.
     */
    fun sendMessage(text: String, language: String) {
        if (text.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                _pendingEvent.value = null
                _pendingRecurring.value = null

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val response = assistantRepository.processAssistantInput(text, language)
                    when (response) {
                        is AiResponse.Success -> {
                            // AI rozpoznało jednorazowe wydarzenie
                            _pendingEvent.value = response.result
                            _lastResponse.value = ChatMessage(
                                text = application.getString(
                                    R.string.assistant_msg_event_format,
                                    response.result.taskTitle,
                                    response.result.date,
                                    response.result.time
                                ),
                                isUser = false
                            )
                        }
                        is AiResponse.SuccessRecurring -> {
                            // AI rozpoznało cykliczne przypomnienie
                            _pendingRecurring.value = response.result

                            // Mapowanie numerów dni tygodnia na czytelny polski tekst
                            val dniMap = mapOf(
                                1 to application.getString(R.string.assistant_day_monday),
                                2 to application.getString(R.string.assistant_day_tuesday),
                                3 to application.getString(R.string.assistant_day_wednesday),
                                4 to application.getString(R.string.assistant_day_thursday),
                                5 to application.getString(R.string.assistant_day_friday),
                                6 to application.getString(R.string.assistant_day_saturday),
                                7 to application.getString(R.string.assistant_day_sunday)
                            )
                            val wybraneDni = response.result.daysOfWeek
                                .mapNotNull { dniMap[it] }
                                .joinToString(", ")

                            _lastResponse.value = ChatMessage(
                                text = application.getString(
                                    R.string.assistant_msg_recurring_format,
                                    response.result.taskTitle,
                                    wybraneDni,
                                    response.result.time
                                ),
                                isUser = false
                            )
                        }
                        is AiResponse.Failure -> {
                            // AI nie zrozumiało intencji lub odpowiedziało na czat
                            _lastResponse.value = ChatMessage(text = response.message, isUser = false)
                        }
                    }
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Potwierdza zapisanie oczekującego wydarzenia jednorazowego.
     * Zrzuca dane do [calendarViewModel] i czyści bufor oczekujących zadań.
     */
    fun confirmEvent() {
        val event = _pendingEvent.value
        if (event != null) {
            calendarViewModel.addEvent(event.date, event.time, event.taskTitle)
            _lastResponse.value = ChatMessage(text = application.getString(R.string.assistant_msg_confirmed_event), isUser = false)
            _pendingEvent.value = null
        }
    }

    /**
     * Potwierdza zapisanie oczekującego zadania cyklicznego.
     * Konwertuje listę liczb na obiekty [DayOfWeek] (wymagane od Androida O),
     * deleguje zapis do [seniorm2ViewModel] i czyści bufor.
     */
    fun confirmRecurring() {
        val rec = _pendingRecurring.value
        if (rec != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Przekonwertowanie wielu liczb na zestaw (Set) bezpiecznych obiektów DayOfWeek
                val daysSet = rec.daysOfWeek.map { DayOfWeek.of(it) }.toSet()

                seniorm2ViewModel.addSeniorm2(rec.taskTitle, rec.time, daysSet)
                _lastResponse.value = ChatMessage(text = application.getString(R.string.assistant_msg_confirmed_recurring), isUser = false)
            }
            _pendingRecurring.value = null
        }
    }

    /**
     * Anuluje proces dodawania zadania (jednorazowego lub cyklicznego).
     * Czyści bufory i informuje użytkownika o anulowaniu akcji.
     */
    fun cancelAction() {
        _pendingEvent.value = null
        _pendingRecurring.value = null
        _lastResponse.value = ChatMessage(text = application.getString(R.string.assistant_msg_cancelled), isUser = false)
    }

    /**
     * Kompletnie resetuje stan asystenta, czyszcząc komunikaty i oczekujące zadania.
     * Przydatne np. po zamknięciu ekranu asystenta.
     */
    fun clearResponse() {
        _lastResponse.value = null
        _pendingEvent.value = null
        _pendingRecurring.value = null
    }
}