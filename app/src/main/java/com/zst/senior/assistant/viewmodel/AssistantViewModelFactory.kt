package com.zst.senior.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Fabryka (Factory) odpowiedzialna za tworzenie instancji [AssistantViewModel].
 *
 * Ponieważ [AssistantViewModel] przyjmuje w swoim konstruktorze parametry (inne ViewModele),
 * domyślna, systemowa fabryka Androida nie potrafi go samodzielnie zainicjować. Ta klasa
 * rozwiązuje ten problem, ręcznie wstrzykując wymagane zależności podczas tworzenia obiektu.
 *
 * @property application Instancja aplikacji.
 * @property calendarViewModel Instancja [SeniorCalendarViewModel] zarządzająca jednorazowymi wydarzeniami.
 * @property seniorm2ViewModel Instancja [Seniorm2ViewModel] zarządzająca zadaniami cyklicznymi.
 */
class AssistantViewModelFactory(
    private val application: Application,
    private val calendarViewModel: SeniorCalendarViewModel,
    private val seniorm2ViewModel: Seniorm2ViewModel // Wstrzykiwany drugi ViewModel
) : ViewModelProvider.Factory {

    /**
     * Tworzy i zwraca nową instancję żądanego ViewModelu.
     *
     * Metoda sprawdza, czy system operacyjny (lub Compose) faktycznie prosi o utworzenie
     * klasy [AssistantViewModel]. Jeśli tak, wywołuje jego konstruktor z przekazanymi wcześniej zależnościami.
     *
     * @param modelClass Obiekt Class reprezentujący ViewModel, który system próbuje utworzyć.
     * @return Gotowa, zainicjowana instancja [AssistantViewModel].
     * @throws IllegalArgumentException Jeśli fabryka zostanie wywołana do stworzenia innej, nieobsługiwanej klasy.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssistantViewModel(application, calendarViewModel, seniorm2ViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
