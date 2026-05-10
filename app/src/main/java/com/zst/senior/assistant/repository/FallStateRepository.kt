package com.zst.senior.assistant.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stan detekcji upadku widoczny dla całej aplikacji.
 * Służy do komunikacji między usługą działającą w tle a interfejsem użytkownika (UI).
 */
sealed class FallDetectionState {
    /** Stan spoczynku - usługa nasłuchuje zdarzeń, nie wykryto upadku. */
    object Idle : FallDetectionState()

    /**
     * Stan odliczania po potencjalnym wykryciu upadku.
     * @property secondsLeft Liczba sekund pozostałych do wywołania alarmu.
     */
    data class Countdown(val secondsLeft: Int) : FallDetectionState()

    /** Stan alarmowy - odliczanie zakończone, następuje wysłanie powiadomień/SMS z wezwaniem o pomoc. */
    object Alarm : FallDetectionState()
}

/**
 * Globalne repozytorium (Singleton) przechowujące aktualny stan detekcji upadku.
 * Wykorzystuje [MutableStateFlow], aby umożliwić komponentom UI reaktywne nasłuchiwanie zmian.
 */
object FallStateRepository {
    private val _state = MutableStateFlow<FallDetectionState>(FallDetectionState.Idle)

    /** Publiczny dostęp do stanu w trybie tylko do odczytu (StateFlow). */
    val state = _state.asStateFlow()

    /**
     * Aktualizuje globalny stan detekcji upadku.
     * @param newState Nowy stan do ustawienia.
     */
    fun updateState(newState: FallDetectionState) {
        _state.value = newState
    }
}
