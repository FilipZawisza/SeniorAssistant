package com.zst.senior.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zst.senior.assistant.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel zarządzający stanem ustawień w interfejsie użytkownika (UI).
 *
 * Wczytuje początkowe wartości z [SettingsRepository] i udostępnia je w formie
 * reaktywnych strumieni [StateFlow]. Dzięki temu, każda zmiana w ustawieniach
 * (np. kliknięcie przełącznika przez użytkownika) natychmiast odświeża cały
 * interfejs aplikacji (np. przebudowuje `AppTheme` Compose'a).
 *
 * @property repository Instancja repozytorium do zapisu ustawień w pamięci trwałej.
 */
class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    /** Strumień przechowujący aktualny mnożnik rozmiaru czcionki. */
    private val _fontSizeScale = MutableStateFlow(repository.getFontSizeScale())
    val fontSizeScale = _fontSizeScale.asStateFlow()

    /** Strumień przechowujący informację, czy tryb WCAG (czarne tło, żółty tekst) jest aktywny. */
    private val _isHighContrast = MutableStateFlow(repository.isHighContrastEnabled())
    val isHighContrast = _isHighContrast.asStateFlow()

    /** Strumień przechowujący informację, czy aplikacja powinna czytać komunikaty na głos (TTS). */
    private val _isTtsEnabled = MutableStateFlow(repository.isTtsEnabled())
    val isTtsEnabled = _isTtsEnabled.asStateFlow()

    private val _language = MutableStateFlow(repository.getLanguage())
    /** Wybrany język aplikacji (np. "pl", "en"). */
    val language = _language.asStateFlow()

    /**
     * Zmienia rozmiar czcionki w aplikacji, aktualizując stan UI i zapisując wybór w pamięci.
     *
     * @param scale Mnożnik rozmiaru czcionki.
     */
    fun setFontSizeScale(scale: Float) {
        _fontSizeScale.value = scale
        repository.saveFontSizeScale(scale)
    }

    /**
     * Włącza lub wyłącza schemat wysokiego kontrastu, aktualizując stan UI i pamięć.
     *
     * @param isEnabled Czy tryb wysokiego kontrastu ma być aktywny.
     */
    fun setHighContrast(isEnabled: Boolean) {
        _isHighContrast.value = isEnabled
        repository.saveHighContrast(isEnabled)
    }

    /**
     * Włącza lub wyłącza funkcję Asystenta Głosowego (czytania ekranu), aktualizując UI i pamięć.
     *
     * @param isEnabled Czy asystent głosowy ma być aktywny.
     */
    fun setTtsEnabled(isEnabled: Boolean) {
        _isTtsEnabled.value = isEnabled
        repository.saveTtsEnabled(isEnabled)
    }

    /**
     * Zmienia język aplikacji i zapisuje go w ustawieniach.
     *
     * @param lang Kod języka (np. "pl", "en").
     */
    fun setLanguage(lang: String) {
        _language.value = lang
        repository.saveLanguage(lang)
    }

    /**
     * Fabryka (Factory) pozwalająca na utworzenie [SettingsViewModel] z wymaganą
     * zależnością w postaci [SettingsRepository].
     */
    class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
