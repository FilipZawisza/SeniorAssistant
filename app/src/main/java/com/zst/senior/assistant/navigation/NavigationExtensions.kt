package com.zst.senior.assistant.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Zmienna przechowująca czas (w milisekundach) ostatniego zdarzenia nawigacyjnego.
 * Współdzielona w obrębie pliku w celu globalnego zapobiegania wielokrotnym kliknięciom.
 */
private var lastClickTime = 0L

/**
 * Czas blokady (debounce) w milisekundach określający minimalny odstęp
 * pomiędzy dozwolonymi akcjami nawigacyjnymi.
 */
private const val DEBOUNCE_TIME = 500L

/**
 * Wykonuje bezpieczną nawigację do podanej ścieżki (przejście "DO PRZODU").
 *
 * Funkcja ta jest rozszerzeniem [NavController] i służy do zapobiegania błędom
 * wywołanym przez szybkie, wielokrotne naciśnięcie przycisków otwierających nowe ekrany.
 * Nawigacja zostanie wykonana tylko wtedy, gdy aktualny wpis na stosie nawigacyjnym
 * znajduje się w stanie [Lifecycle.State.RESUMED] oraz upłynął czas [DEBOUNCE_TIME]
 * od ostatniej akcji.
 *
 * Wyjątki rzucane podczas nawigacji są przechwytywane (try-catch), co dodatkowo
 * chroni aplikację przed ewentualnymi awariami (crashami) przy nietypowych stanach grafu.
 *
 * @param route Ścieżka (route) docelowego ekranu w grafie nawigacji.
 * @param builder Opcjonalny blok konfiguracyjny (DSL) dla opcji nawigacji, pozwalający np. na modyfikację stosu (popUpTo).
 */
fun NavController.safeNavigate(route: String, builder: (NavOptionsBuilder.() -> Unit)? = null) {
    val currentTime = System.currentTimeMillis()
    val isResumed = this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

    if (isResumed && (currentTime - lastClickTime > DEBOUNCE_TIME)) {
        lastClickTime = currentTime
        try {
            if (builder != null) {
                this.navigate(route, builder)
            } else {
                this.navigate(route)
            }
        } catch (e: Exception) {
            // Log.e("Navigation", "Error navigating to $route", e)
        }
    }
}

/**
 * Wykonuje bezpieczne cofnięcie do poprzedniego ekranu (przejście "WSTECZ").
 *
 * Funkcja zabezpiecza przed problemem "białego ekranu" lub awarii aplikacji,
 * który pojawia się w sytuacji spamowania (zbyt szybkiego klikania) sprzętowego
 * lub ekranowego przycisku wstecz. Operacja zdjęcia ekranu ze stosu ([popBackStack])
 * jest dozwolona wyłącznie, gdy bieżący ekran jest w pełni wyrenderowany
 * ([Lifecycle.State.RESUMED]) i minął bezpieczny czas [DEBOUNCE_TIME].
 */
fun NavController.safePopBackStack() {
    val currentTime = System.currentTimeMillis()
    val isResumed = this.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED

    if (isResumed && (currentTime - lastClickTime > DEBOUNCE_TIME)) {
        lastClickTime = currentTime
        try {
            this.popBackStack()
        } catch (e: Exception) {
            // Ignoruj błędy przy szybkim cofaniu
        }
    }
}