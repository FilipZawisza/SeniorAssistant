package com.zst.senior.assistant.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Obiekt przechowujący wynik zapytania o lokalizację urządzenia.
 *
 * @property address Tekstowa reprezentacja adresu (np. "ul. Kwiatowa 15, 00-001 Warszawa")
 * lub, w przypadku błędu Geocodera, surowe współrzędne geograficzne.
 * @property mapsLink Bezpośredni link do Map Google, który po kliknięciu otwiera
 * aplikację/stronę z pinezką w dokładnym miejscu.
 */
data class LocationResult(
    val address: String,
    val mapsLink: String
)

/**
 * Asynchronicznie pobiera aktualną, bardzo dokładną lokalizację urządzenia.
 *
 * Funkcja wykorzystuje [LocationServices.getFusedLocationProviderClient] z priorytetem
 * [Priority.PRIORITY_HIGH_ACCURACY], aby wymusić odczyt z modułu GPS (a nie tylko z sieci).
 * Po udanym pobraniu współrzędnych, podejmuje próbę przetłumaczenia ich na czytelny adres
 * ulicy przy pomocy usługi [Geocoder].
 *
 * UWAGA: Funkcja zakłada, że uprawnienia lokalizacyjne (`ACCESS_FINE_LOCATION` lub `ACCESS_COARSE_LOCATION`)
 * zostały już przyznane przez użytkownika na poziomie interfejsu (UI). Brak uprawnień spowoduje wyrzucenie
 * cichego wyjątku i zwrócenie wartości `null`.
 *
 * @param context Kontekst aplikacji wymagany do inicjalizacji usług lokalizacyjnych i Geocodera.
 * @return [LocationResult] zawierający adres i link, lub `null` w przypadku błędu (np. wyłączony GPS, brak uprawnień).
 */
@SuppressLint("MissingPermission") // Zakładamy, że sprawdziliśmy uprawnienia w UI
suspend fun getCurrentLocationSafely(context: Context): LocationResult? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    return try {
        // Pobieramy jednorazowo bardzo dokładną lokalizację
        val location = fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await()

        if (location != null) {
            val geocoder = Geocoder(context, Locale.getDefault())
            var addressText = "Współrzędne: ${location.latitude}, ${location.longitude}"

            // Próba zamiany na adres tekstowy (np. ulica, miasto)
            try {
                // Bezpieczne wywołanie Geocodera dla wszystkich wersji Androida.
                // Metoda getFromLocation jest oznaczona jako DEPRECATED w nowszych API,
                // ale dla zapewnienia kompatybilności wstecznej to wywołanie jest poprawne.
                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    addressText = addresses[0].getAddressLine(0)
                }
            } catch (e: Exception) {
                // Jeśli Geocoder zawiedzie (np. brak internetu, niedostępność usług Google Play),
                // zostają same współrzędne - to bezpieczne zachowanie chroniące przed crashem aplikacji.
            }

            // POPRAWIONE: Oficjalny format linku (Universal Link) do Google Maps ze znacznikiem (pinem)
            val mapsLink = "https://www.google.com/maps/search/?api=1&query=${location.latitude},${location.longitude}"

            LocationResult(addressText, mapsLink)
        } else {
            null
        }
    } catch (e: Exception) {
        // Wyłapanie potencjalnych błędów, np. braku uprawnień (SecurityException) lub braku usług Google.
        null
    }
}