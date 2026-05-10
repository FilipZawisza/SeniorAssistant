package com.zst.senior.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zst.senior.assistant.repository.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Model danych reprezentujący pojedynczego gracza w rankingu gier umysłowych.
 *
 * @property id Unikalny identyfikator użytkownika (UID z Firebase).
 * @property imie Imię gracza wyświetlane na liście (lub "Anonim", jeśli brak danych).
 * @property punkty Suma zdobytych punktów w minigrach.
 * @property pozycja Miejsce gracza w rankingu (np. 1 dla lidera).
 */
data class LeaderboardEntry(
    val id: String,
    val imie: String,
    val punkty: Int,
    val pozycja: Int = 0
)

/**
 * ViewModel zarządzający rankingiem (Leaderboard) graczy w module gier umysłowych.
 *
 * Pobiera listę najlepszych seniorów (TOP 10) oraz niezwykle wydajnie oblicza
 * aktualną pozycję i punkty obecnie zalogowanego użytkownika, wykorzystując [LeaderboardRepository].
 *
 * @property repository Repozytorium obsługujące pobieranie danych rankingu.
 */
class LeaderboardViewModel(
    private val repository: LeaderboardRepository = LeaderboardRepository()
) : ViewModel() {

    /** Stan przechowujący listę 10 najlepszych graczy. */
    private val _topPlayers = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val topPlayers: StateFlow<List<LeaderboardEntry>> = _topPlayers.asStateFlow()

    /** Stan przechowujący aktualną pozycję (miejsce) w rankingu dla zalogowanego użytkownika. */
    private val _currentUserRank = MutableStateFlow<Int?>(null)
    val currentUserRank: StateFlow<Int?> = _currentUserRank.asStateFlow()

    /** Stan przechowujący liczbę punktów zalogowanego użytkownika. */
    private val _currentUserPoints = MutableStateFlow(0)
    val currentUserPoints: StateFlow<Int> = _currentUserPoints.asStateFlow()

    /** Flaga określająca, czy dane są aktualnie pobierane z serwera. */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Automatyczne pobranie rankingu przy utworzeniu ViewModelu
        fetchLeaderboard()
    }

    /**
     * Asynchronicznie pobiera dane do rankingu korzystając z metod repozytorium.
     *
     * Proces dzieli się na dwa optymalne zapytania:
     * 1. Pobranie TOP 10 użytkowników z największą liczbą punktów.
     * 2. Wyliczenie pozycji zalogowanego użytkownika.
     */
    fun fetchLeaderboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Pobierz TOP 10 Seniorów
                _topPlayers.value = repository.getTopPlayers()

                // 2. Oblicz pozycję zalogowanego Seniora
                val rankData = repository.getCurrentUserRank()
                if (rankData != null) {
                    _currentUserPoints.value = rankData.first
                    _currentUserRank.value = rankData.second
                }
            } catch (e: Exception) {
                // Wersja produkcyjna mogłaby tu wysyłać błąd do UI za pomocą np. SharedFlow
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
