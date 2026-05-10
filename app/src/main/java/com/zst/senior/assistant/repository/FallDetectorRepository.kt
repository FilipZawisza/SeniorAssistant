package com.zst.senior.assistant.repository

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.zst.senior.assistant.services.FallDetectionService

/**
 * Repozytorium odpowiedzialne za zarządzanie komunikacją z usługą detekcji upadków [FallDetectionService].
 *
 * Klasa hermetyzuje logikę wysyłania komend (Intent) do serwisu, takich jak uruchomienie monitorowania,
 * zatrzymanie, aktualizacja numeru opiekuna oraz obsługa stanów alarmowych.
 *
 * @property application Instancja aplikacji wymagana do uruchamiania usług systemowych.
 */
class FallDetectorRepository(private val application: Application) {

    /**
     * Uruchamia usługę monitorowania upadków.
     * Ponieważ minimalne API projektu to 26, usługa jest zawsze uruchamiana
     * jako Foreground Service.
     *
     * @param guardianPhoneNumber Numer telefonu, pod który zostanie wysłany alert.
     */
    fun startMonitoring(guardianPhoneNumber: String?) {
        val intent = Intent(application, FallDetectionService::class.java).apply {
            action = "START"
            putExtra("GUARDIAN_NUMBER", guardianPhoneNumber)
        }

        try {
            application.startForegroundService(intent)
        } catch (e: Exception) {
            Log.e("FallDetectorRepo", "Błąd podczas uruchamiania serwisu: ${e.message}")
        }
    }

    /**
     * Zatrzymuje usługę monitorowania upadków.
     */
    fun stopMonitoring() {
        val intent = Intent(application, FallDetectionService::class.java).apply {
            action = "STOP"
        }
        application.startService(intent)
    }

    /**
     * Aktualizuje numer telefonu opiekuna w działającej usłudze.
     *
     * @param number Nowy numer telefonu w formacie tekstowym.
     */
    fun updateGuardianNumber(number: String) {
        val intent = Intent(application, FallDetectionService::class.java).apply {
            action = "UPDATE_NUMBER"
            putExtra("GUARDIAN_NUMBER", number)
        }
        application.startService(intent)
    }

    /**
     * Przesyła żądanie anulowania aktywnego odliczania alarmu do usługi.
     */
    fun cancelCountdown() {
        val intent = Intent(application, FallDetectionService::class.java).apply {
            action = "CANCEL"
        }
        application.startService(intent)
    }
}
