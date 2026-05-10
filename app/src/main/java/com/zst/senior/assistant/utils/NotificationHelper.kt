package com.zst.senior.assistant.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Pomocnik do zarządzania kanałami powiadomień w aplikacji.
 *
 * Centralizuje definicje kanałów, ich identyfikatorów oraz konfigurację, aby zapewnić
 * spójność powiadomień w całym systemie (MainActivity, ViewModele, Serwisy).
 */
object NotificationHelper {
    /** ID kanału dla przypomnień o lekach i zadaniach cyklicznych seniora. */
    const val SENIOR_CHANNEL_ID = "SENIOR_CHANNEL_ID"
    /** ID kanału dla powiadomień o nowych zleceniach dla wolontariuszy. */
    const val VOLUNTEER_CHANNEL_ID = "WOLONTARIUSZ_CHANNEL"
    /** ID kanału informacyjnego o działaniu usługi detekcji upadków w tle. */
    const val FALL_DETECTION_CHANNEL_ID = "FallDetectionChannel"

    /**
     * Inicjalizuje i rejestruje wszystkie wymagane kanały powiadomień w systemie Android.
     *
     * Od wersji Android 8.0 (Oreo) każde powiadomienie musi być przypisane do kanału.
     * Ta metoda powinna być wywołana raz, najlepiej podczas startu aplikacji (np. w [MainActivity]).
     *
     * @param context Kontekst aplikacji wymagany do pobrania [NotificationManager].
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val seniorChannel = NotificationChannel(
                SENIOR_CHANNEL_ID,
                "Powiadomienia Seniora",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Kanał dla przypomnień o lekach i zadaniach cyklicznych"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500, 500)
            }

            val volunteerChannel = NotificationChannel(
                VOLUNTEER_CHANNEL_ID,
                "Nowe zlecenia",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Powiadomienia o zleceniach w okolicy"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 100, 300)
            }

            val fallChannel = NotificationChannel(
                FALL_DETECTION_CHANNEL_ID,
                "Monitorowanie Upadku",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Informuje o działaniu detektora upadku w tle"
            }

            notificationManager.createNotificationChannel(seniorChannel)
            notificationManager.createNotificationChannel(volunteerChannel)
            notificationManager.createNotificationChannel(fallChannel)
        }
    }
}
