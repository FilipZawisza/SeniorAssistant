package com.zst.senior.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.zst.senior.assistant.repository.FallDetectorRepository
import com.zst.senior.assistant.repository.FallStateRepository

/**
 * ViewModel zarządzający systemem detekcji upadków (Fall Detection).
 *
 * Klasa ta pełni rolę kontrolera pośredniczącego między interfejsem użytkownika (UI)
 * a działającą w tle usługą detekcji upadków. Deleguje ona operacje wykonawcze
 * do [FallDetectorRepository], izolując tym samym logikę Intenta i cyklu życia serwisu.
 *
 * @property application Instancja aplikacji wymagana przez AndroidViewModel.
 * @property repository Repozytorium obsługujące komunikację z serwisem w tle.
 */
class FallDetectorViewModel(
    application: Application,
    private val repository: FallDetectorRepository = FallDetectorRepository(application)
) : AndroidViewModel(application) {

    /**
     * Strumień stanu detekcji upadku pochodzący z repozytorium.
     * Obserwowany przez UI w celu wyświetlania odpowiednich komunikatów lub ekranu alarmowego.
     */
    val fallDetectionState = FallStateRepository.state

    /** Przechowuje numer telefonu opiekuna, do którego zostanie wysłane powiadomienie SMS w razie upadku. */
    private var guardianPhoneNumber: String? = null

    /**
     * Ustawia lub aktualizuje numer telefonu opiekuna.
     *
     * Zapisuje numer lokalnie w ViewModelu oraz informuje repozytorium o zmianie,
     * co skutkuje wysłaniem odpowiedniej komendy do działającej usługi.
     *
     * @param number Numer telefonu w formie tekstowej.
     */
    fun setGuardianPhoneNumber(number: String) {
        this.guardianPhoneNumber = number
        repository.updateGuardianNumber(number)
    }

    /**
     * Uruchamia usługę monitorowania czujników w celu wykrywania upadków.
     *
     * Metoda wywołuje repozytorium, które zajmuje się procesem uruchamiania
     * Foreground Service z zachowaniem kompatybilności wersji Androida.
     */
    fun startMonitoring() {
        repository.startMonitoring(guardianPhoneNumber)
    }

    /**
     * Zatrzymuje działanie usługi detekcji upadków poprzez repozytorium.
     */
    fun stopMonitoring() {
        repository.stopMonitoring()
    }

    /**
     * Anuluje trwające odliczanie do wysłania powiadomienia awaryjnego (SMS/połączenie).
     * Wywoływane, gdy użytkownik wciśnie przycisk "Nic mi nie jest" po wykryciu potencjalnego upadku.
     */
    fun cancelCountdown() {
        repository.cancelCountdown()
    }

    /**
     * Alias dla metody [cancelCountdown].
     * Przeznaczony do użytku w miejscach interfejsu, gdzie nazewnictwo "alarm"
     * jest bardziej semantycznie dopasowane niż "odliczanie".
     */
    fun cancelAlarm() {
        cancelCountdown()
    }
}
