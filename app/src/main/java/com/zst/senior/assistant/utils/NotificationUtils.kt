package com.zst.senior.assistant.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.zst.senior.assistant.MainActivity

/**
 * Odbiornik zdarzeń (BroadcastReceiver) odpowiedzialny za wyświetlanie powiadomień
 * oraz zarządzanie cyklicznymi przypomnieniami w aplikacji.
 *
 * Komponent ten jest wybudzany przez systemowy `AlarmManager` w dokładnie zaplanowanym czasie.
 * Oprócz samego wyświetlenia powiadomienia o wysokim priorytecie (z wibracją), sprawdza,
 * czy zadanie ma charakter cykliczny. Jeśli tak, automatycznie "nakręca" kolejny alarm
 * na dokładnie za 7 dni.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Główna metoda wywoływana, gdy nadejdzie czas zaplanowanego alarmu.
     * * UWAGA: Użycie `setExactAndAllowWhileIdle` w tej metodzie wymaga zadeklarowania
     * uprawnienia `SCHEDULE_EXACT_ALARM` w pliku AndroidManifest.xml (od Androida 12),
     * a w Androidzie 14+ użytkownik musi wyrazić na to jawną zgodę, w przeciwnym razie
     * system zgłosi wyjątek [SecurityException].
     *
     * @param context Kontekst aplikacji.
     * @param intent Intencja niosąca dane powiadomienia (tytuł, treść, ID, flaga cykliczności).
     */
    @android.annotation.SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = NotificationHelper.SENIOR_CHANNEL_ID

        // Pobranie danych do wyświetlenia z intencji
        val title = intent.getStringExtra("NOTIFICATION_TITLE") ?: "Przypomnienie"
        val message = intent.getStringExtra("NOTIFICATION_MESSAGE") ?: "Masz nowe zadanie"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

        // Intencja uruchamiająca główny ekran aplikacji po kliknięciu w powiadomienie
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Budowanie struktury powiadomienia
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .setAutoCancel(true) // Znika po kliknięciu
            .setContentIntent(tapPendingIntent)
            .build()

        // 1. WYŚWIETLAMY POWIADOMIENIE
        notificationManager.notify(notificationId, notification)

        // 2. NOWOŚĆ: JEŚLI TO ZADANIE CYKLICZNE -> PLANUJEMY KOLEJNE ZA RÓWNE 7 DNI
        val isRecurring = intent.getBooleanExtra("IS_RECURRING", false)
        if (isRecurring) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            // Równe 7 dni w milisekundach
            val nextTriggerAtMillis = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)

            val nextIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = "SENIORM1_ALARM"
                putExtra("NOTIFICATION_TITLE", title)
                putExtra("NOTIFICATION_MESSAGE", message)
                putExtra("NOTIFICATION_ID", notificationId)
                putExtra("IS_RECURRING", true)
            }
            val nextPendingIntent = PendingIntent.getBroadcast(
                context, notificationId, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                // Nakręcamy budzik ponownie na przyszły tydzień.
                // allowWhileIdle zapewnia, że alarm zadziała nawet w trybie Doze (oszczędzania baterii).
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    nextTriggerAtMillis,
                    nextPendingIntent
                )
                Log.d("NotificationReceiver", "Zadanie cykliczne '$title' zostało zaplanowane na następny tydzień.")
            } catch (e: SecurityException) {
                // Może wystąpić na Android 14+, jeśli aplikacja straciła uprawnienie dokładnych alarmów.
                Log.e("NotificationReceiver", "Błąd odnawiania alarmu: ${e.message}")
            }
        }
    }
}

/**
 * Wyskupuje wiadomość SMS na podany numer telefonu w trybie cichym (w tle).
 *
 * Funkcja automatycznie dzieli długie teksty (powyżej standardowego limitu 160 znaków SMS)
 * na wiadomości wieloczęściowe (Multipart). Obsługuje błędy braku uprawnień (`SEND_SMS`)
 * i loguje komunikaty diagnostyczne.
 *
 * @param context Kontekst niezbędny do pobrania usług systemowych i wyświetlenia Toastu.
 * @param phoneNumber Numer docelowy, na który zostanie wysłana wiadomość.
 * @param message Treść wiadomości do wysłania.
 */
fun sendSms(context: Context, phoneNumber: String, message: String) {
    if (phoneNumber.isBlank() || message.isBlank()) {
        Log.w("SMS", "Próba wysłania SMS z pustym numerem lub treścią.")
        showToastInMainThread(context, "Brak numeru opiekuna w profilu!")
        return
    }

    try {
        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(SmsManager::class.java)
        } else {
            @Suppress("DEPRECATION")
            SmsManager.getDefault()
        }

        // Automatyczny podział długich wiadomości
        val parts = smsManager.divideMessage(message)
        if (parts.size > 1) {
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } else {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        }

        Log.i("SMS", "Wysłano wiadomość do $phoneNumber")
        showToastInMainThread(context, "Wysłano SMS alarmowy do opiekuna.")

    } catch (e: SecurityException) {
        Log.e("SMS", "Brak uprawnień SEND_SMS.", e)
        showToastInMainThread(context, "BŁĄD: Aplikacja nie ma uprawnień do SMS.")
    } catch (e: Exception) {
        Log.e("SMS", "Błąd wysyłania: ${e.message}", e)
        showToastInMainThread(context, "Nie udało się wysłać SMS. Sprawdź zasięg.")
    }
}

/**
 * Wyświetla komunikat typu [Toast] mając pewność, że operacja zostanie wykonana
 * na głównym wątku (Main/UI Thread).
 *
 * Jest to niezbędne zabezpieczenie, ponieważ metody takie jak `sendSms` mogą
 * być wywoływane w tle (np. z asynchronicznej korutyny), a próba uruchomienia
 * interfejsu graficznego poza wątkiem UI powoduje natychmiastowe zgniecenie (crash) aplikacji.
 *
 * @param context Kontekst powiązany z powiadomieniem.
 * @param message Tekst do wyświetlenia.
 */
private fun showToastInMainThread(context: Context, message: String) {
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}