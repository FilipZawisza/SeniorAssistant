package com.zst.senior.assistant.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zst.senior.assistant.repository.SeniordleRepository
import com.zst.senior.assistant.utils.SeniordleWords // Upewnij się, że ten import się zgadza
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enum reprezentujący aktualny stan rozgrywki w grze Seniordle.
 */
enum class GameStatus {
    PLAYING,   // Gra w toku
    WON,       // Gracz odgadł hasło
    LOST,      // Gracz wyczerpał limit prób (6) i nie odgadł hasła
    GIVEN_UP   // Gracz dobrowolnie poddał grę
}

/**
 * ViewModel zarządzający logiką i stanem mini-gry słownej "Seniordle" (odpowiednik Wordle).
 *
 * Klasa ta kontroluje wprowadzanie liter, weryfikację słów, system podpowiedzi,
 * zarządzanie serią zwycięstw (win streak) oraz integrację z [SeniordleRepository]
 * w celu przypisywania punktów za wygrane gry do profilu Seniora.
 *
 * @property repository Repozytorium obsługujące system punktacji w grze.
 */
class SeniordleViewModel(
    private val repository: SeniordleRepository = SeniordleRepository()
) : ViewModel() {

    /** Hasło, które gracz musi odgadnąć w obecnej rundzie. */
    private val _targetWord = MutableStateFlow("")
    val targetWord: StateFlow<String> = _targetWord.asStateFlow()

    /** Lista dotychczasowych, w pełni zatwierdzonych prób (5-literowych słów). */
    private val _guesses = MutableStateFlow<List<String>>(emptyList())
    val guesses: StateFlow<List<String>> = _guesses.asStateFlow()

    /** Aktualnie wpisywane słowo (ciąg liter przed wciśnięciem ENTER). */
    private val _currentGuess = MutableStateFlow("")
    val currentGuess: StateFlow<String> = _currentGuess.asStateFlow()

    /** Aktualny status gry (trwa, wygrana, przegrana, poddana). */
    private val _gameStatus = MutableStateFlow(GameStatus.PLAYING)
    val gameStatus: StateFlow<GameStatus> = _gameStatus.asStateFlow()

    /** * Flaga ładowania. Ponieważ baza słów ([SeniordleWords]) została przeniesiona
     * do pamięci lokalnej aplikacji, nie musimy już czekać na zapytania sieciowe.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** Przechowuje dane o podpowiedzi. */
    private val _hintData = MutableStateFlow<HintData?>(null)
    val hintData: StateFlow<HintData?> = _hintData.asStateFlow()

    data class HintData(val position: Int = -1, val letter: Char = ' ', val allFound: Boolean = false)

    /** Licznik nieprzerwanej serii wygranych gier (Win Streak). Resetowany przy przegranej lub poddaniu. */
    private val _winStreak = MutableStateFlow(0)
    val winStreak: StateFlow<Int> = _winStreak.asStateFlow()

    init {
        // Startujemy grę natychmiast przy inicjalizacji ViewModelu
        resetGame()
    }

    /**
     * Resetuje stan gry i losuje nowe słowo z lokalnej bazy [SeniordleWords].
     * Zabezpiecza przed wylosowaniem tego samego słowa dwa razy z rzędu.
     */
    fun resetGame() {
        val wordList = SeniordleWords.wordsList

        // Zabezpieczenie na wypadek, gdyby lista była pusta
        if (wordList.isEmpty()) {
            _targetWord.value = "SERCE"
        } else {
            var newWord = wordList.random()

            // Losuj dopóki nie trafisz na inne słowo niż poprzednio (jeśli lista ma więcej niż 1 element)
            while (newWord == _targetWord.value && wordList.size > 1) {
                newWord = wordList.random()
            }
            _targetWord.value = newWord
        }

        _guesses.value = emptyList()
        _currentGuess.value = ""
        _hintData.value = null
        _gameStatus.value = GameStatus.PLAYING
    }

    // --- SYSTEM PODPOWIEDZI ---

    /**
     * Generuje podpowiedź dla gracza, odkrywając jedną z liter, która nie została jeszcze
     * wprowadzona w dotychczasowych próbach. Użycie podpowiedzi skutkuje otrzymaniem
     * mniejszej liczby punktów po wygranej.
     */
    fun useHint() {
        if (_gameStatus.value != GameStatus.PLAYING || _hintData.value != null) return

        val target = _targetWord.value
        val guessedLetters = _guesses.value.joinToString("").toCharArray().toSet()

        // Szuka pierwszej litery z hasła, której gracz jeszcze nie użył w swoich próbach
        val hintLetter = target.toCharArray().firstOrNull { it !in guessedLetters }

        if (hintLetter != null) {
            val position = target.indexOf(hintLetter) + 1
            _hintData.value = HintData(position = position, letter = hintLetter)
        } else {
            _hintData.value = HintData(allFound = true)
        }
    }

    // --- PODDANIE SIĘ ---

    /**
     * Pozwala graczowi dobrowolnie zakończyć aktualną rundę jako przegraną.
     * Skutkuje to wyzerowaniem aktualnej serii zwycięstw (Win Streak).
     */
    fun giveUp() {
        if (_gameStatus.value == GameStatus.PLAYING) {
            _gameStatus.value = GameStatus.GIVEN_UP
            _winStreak.value = 0 // Zerujemy serię
        }
    }

    /**
     * Obsługuje wpisanie pojedynczej litery z wirtualnej klawiatury.
     * Zapobiega wpisaniu więcej niż 5 liter w jednym rzędzie.
     *
     * @param letter Wprowadzona litera.
     */
    fun onKeyPress(letter: String) {
        if (_gameStatus.value != GameStatus.PLAYING || _currentGuess.value.length >= 5) return
        _currentGuess.value += letter
    }

    /**
     * Obsługuje usunięcie ostatnio wprowadzonej litery (Backspace).
     */
    fun onBackspace() {
        if (_gameStatus.value != GameStatus.PLAYING || _currentGuess.value.isEmpty()) return
        _currentGuess.value = _currentGuess.value.dropLast(1)
    }

    /**
     * Zatwierdza aktualnie wprowadzone słowo.
     * Jeśli słowo ma dokładnie 5 liter, dodaje je do listy prób.
     * Sprawdza warunki wygranej (odgadnięcie hasła) oraz przegranej (wyczerpanie 6 prób).
     */
    fun onEnter() {
        // Ignoruj zatwierdzenie, jeśli słowo jest za krótkie
        if (_gameStatus.value != GameStatus.PLAYING || _currentGuess.value.length != 5) return

        val guess = _currentGuess.value
        val newGuesses = _guesses.value + guess
        _guesses.value = newGuesses
        _currentGuess.value = ""

        if (guess == _targetWord.value) {
            _gameStatus.value = GameStatus.WON
            _winStreak.value += 1 // Zwiększamy serię wygranych
            dodajPunktyZaWygrana()
        } else if (newGuesses.size >= 6) {
            _gameStatus.value = GameStatus.LOST
            _winStreak.value = 0 // Zerujemy serię przy przegranej
        }
    }

    /**
     * Asynchronicznie dodaje punkty do konta Seniora po wygranej grze za pomocą repozytorium.
     *
     * System punktacji:
     * - Bazowa wygrana: 10 punktów.
     * - Wygrana z użyciem podpowiedzi: 5 punktów (kara -5 pkt).
     * - Bonus za serię zwycięstw (Streak >= 3): dodatkowe +2 punkty do wyniku.
     */
    private fun dodajPunktyZaWygrana() {
        // Podpowiedź zmniejsza nagrodę o połowę. Bonusowy punkt za serię (streak >= 3)
        var punkty = if (_hintData.value != null) 5L else 10L
        if (_winStreak.value >= 3) punkty += 2L

        viewModelScope.launch {
            repository.addSeniordlePoints(punkty)
        }
    }
}