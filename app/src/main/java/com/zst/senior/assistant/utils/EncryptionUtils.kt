package com.zst.senior.assistant.utils

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Narzędzie do zaawansowanego szyfrowania wiadomości przy użyciu algorytmu AES w trybie GCM (AEAD).
 *
 * Zapewnia zarówno poufność (szyfrowanie), jak i integralność (autentykację) danych.
 * Jest to rozwiązanie znacznie bezpieczniejsze i bardziej nowoczesne niż standardowe AES/ECB,
 * ponieważ chroni również przed manipulacją szyfrogramem w locie.
 *
 */
object EncryptionUtils {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val TAG_LENGTH = 128
    private const val IV_LENGTH = 12
    private const val SECRET_KEY = "SeniorAsystent26" // Klucz 16-znakowy

    /**
     * Szyfruje podany ciąg znaków przy użyciu algorytmu AES-GCM.
     *
     * Dla każdej wiadomości generowany jest nowy, losowy 12-bajtowy wektor inicjalizujący (IV).
     * Wektor ten jest dołączany na samym początku wynikowego ciągu bajtów (iv + ciphertext),
     * a następnie całość kodowana jest do bezpiecznego w transmisji formatu Base64.
     *
     * UWAGA: W przypadku błędu kryptograficznego funkcja zwraca oryginalny, niezaszyfrowany tekst (`value`).
     *
     * @param value Wiadomość jawna do zaszyfrowania.
     * @return Zaszyfrowany i zakodowany w Base64 ciąg znaków (wraz z IV), lub oryginalny tekst w razie błędu.
     */
    fun encrypt(value: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)

            // Generowanie losowego IV (Initialization Vector) dla każdej wiadomości
            val iv = ByteArray(IV_LENGTH)
            SecureRandom().nextBytes(iv)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
            val encryptedBytes = cipher.doFinal(value.toByteArray())

            // Łączymy IV z zaszyfrowanymi danymi
            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.DEFAULT).trim()
        } catch (e: Exception) {
            e.printStackTrace()
            value
        }
    }

    /**
     * Deszyfruje wiadomość zapisaną w formacie Base64.
     *
     * Metoda dekoduje ciąg znaków, a następnie wyodrębnia pierwsze 12 bajtów jako
     * unikalny dla tej wiadomości wektor inicjalizujący (IV). Reszta danych (ciphertext)
     * zostaje odszyfrowana przy użyciu tego wektora.
     *
     * W przypadku niepowodzenia (np. natrafienia na starą wiadomość bez wygenerowanego IV),
     * metoda wykonuje próbę awaryjną korzystając ze starego systemu deszyfrowania [decryptLegacy].
     *
     * @param value Zaszyfrowany tekst w formacie Base64 (z doklejonym IV na początku).
     * @return Odszyfrowany, jawny tekst oryginalnej wiadomości.
     */
    fun decrypt(value: String): String {
        return try {
            val combined = Base64.decode(value, Base64.DEFAULT)
            if (combined.size < IV_LENGTH) return value

            // Wyodrębnienie IV
            val iv = combined.sliceArray(0 until IV_LENGTH)
            val encryptedBytes = combined.sliceArray(IV_LENGTH until combined.size)

            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(TAG_LENGTH, iv)

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            // Próba deszyfrowania starym sposobem (ECB) dla kompatybilności wstecznej
            decryptLegacy(value)
        }
    }

    /**
     * Próbuje zdeszyfrować wiadomość przy użyciu starszego trybu AES/ECB.
     *
     * Funkcja służy wyłącznie do zachowania kompatybilności wstecznej (Backward Compatibility),
     * by móc odczytać wiadomości zapisane w bazie jeszcze przed wdrożeniem bezpieczniejszego trybu GCM.
     * * W przeciwieństwie do GCM, tryb ECB nie stosuje unikalnych wektorów inicjalizujących (IV).
     *
     * @param value Zaszyfrowany tekst starszego formatu w Base64.
     * @return Odszyfrowany tekst jawny lub oryginalny wejściowy tekst w razie niepowodzenia.
     */
    private fun decryptLegacy(value: String): String {
        return try {
            val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(value, Base64.DEFAULT)
            String(cipher.doFinal(decodedBytes))
        } catch (e: Exception) {
            value
        }
    }
}