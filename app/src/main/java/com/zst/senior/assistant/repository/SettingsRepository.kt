package com.zst.senior.assistant.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.create
import androidx.security.crypto.MasterKey.Builder
import androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM

/**
 * Repozytorium zarządzające lokalnym zapisem i odczytem ustawień aplikacji.
 * Wykorzystuje EncryptedSharedPreferences dla zapewnienia bezpieczeństwa danych wrażliwych.
 */
class SettingsRepository(context: Context) {
    private val masterKey = Builder(context)
        .setKeyScheme(AES256_GCM)
        .build()

    private val sharedPrefs: SharedPreferences = create(
        context,
        "SettingsPrefs",
        masterKey,
        PrefKeyEncryptionScheme.AES256_SIV,
        PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_HIGH_CONTRAST = "key_high_contrast"
        private const val KEY_FONT_SCALE = "key_font_scale"
        private const val KEY_TTS_ENABLED = "key_tts_enabled"
        private const val KEY_LANGUAGE = "key_language"
        // NOWY KLUCZ:
        private const val KEY_GUARDIAN_PHONE = "key_guardian_phone"

        private const val DEFAULT_FONT_SCALE = 1.15f
    }

    /**
     * Zapisuje numer telefonu opiekuna/osoby kontaktowej SOS.
     * Używane przez FallDetectionService do wysyłania SMS po wykryciu upadku.
     */
    fun saveGuardianPhoneNumber(number: String) {
        sharedPrefs.edit().putString(KEY_GUARDIAN_PHONE, number).apply()
    }

    /**
     * Pobiera zapisany numer opiekuna.
     * @return Numer telefonu lub null, jeśli nie został jeszcze skonfigurowany.
     */
    fun getGuardianPhoneNumber(): String? {
        return sharedPrefs.getString(KEY_GUARDIAN_PHONE, null)
    }

    /**
     * Zapisuje w pamięci urządzenia wybrany język aplikacji.
     */
    fun saveLanguage(lang: String) {
        sharedPrefs.edit().putString(KEY_LANGUAGE, lang).apply()
    }

    /**
     * Odczytuje zapisany język aplikacji.
     */
    fun getLanguage(): String {
        return sharedPrefs.getString(KEY_LANGUAGE, null) ?: "pl"
    }

    /**
     * Zapisuje flagę włączenia/wyłączenia trybu wysokiego kontrastu (WCAG).
     */
    fun saveHighContrast(isEnabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_HIGH_CONTRAST, isEnabled).apply()
    }

    fun isHighContrastEnabled() = sharedPrefs.getBoolean(KEY_HIGH_CONTRAST, false)

    /**
     * Zapisuje preferowaną skalę powiększenia czcionki.
     */
    fun saveFontSizeScale(scale: Float) {
        sharedPrefs.edit().putFloat(KEY_FONT_SCALE, scale).apply()
    }

    fun getFontSizeScale(): Float {
        return sharedPrefs.getFloat(KEY_FONT_SCALE, DEFAULT_FONT_SCALE)
    }

    /**
     * Zapisuje stan globalnego Asystenta Głosowego (TTS).
     */
    fun saveTtsEnabled(isEnabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_TTS_ENABLED, isEnabled).apply()
    }

    fun isTtsEnabled() = sharedPrefs.getBoolean(KEY_TTS_ENABLED, false)
}