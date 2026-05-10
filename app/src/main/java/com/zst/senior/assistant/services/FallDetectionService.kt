package com.zst.senior.assistant.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*
import com.zst.senior.assistant.repository.FallDetectionState
import com.zst.senior.assistant.repository.FallStateRepository
import com.zst.senior.assistant.repository.SettingsRepository
import kotlin.math.sqrt

/**
 * Usługa działająca na pierwszym planie (Foreground Service) odpowiedzialna za ciągłe monitorowanie
 * akcelerometru pod kątem nagłych zmian przyśpieszenia wskazujących na upadek seniora.
 * Zaimplementowana jako [SensorEventListener], aby odbierać surowe dane z czujników urządzenia.
 */
class FallDetectionService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var vibrator: Vibrator

    // Dodajemy repozytorium ustawień, aby numer przetrwał restart urządzenia
    private lateinit var settingsRepository: SettingsRepository

    private var guardianPhoneNumber: String? = null

    /** Próg przyśpieszenia (w m/s^2) uznawany za swobodne spadanie. */
    private val FREE_FALL_THRESHOLD = 2.0f

    /** Próg nagłego przyśpieszenia (w m/s^2) uznawany za uderzenie o podłoże. */
    private val IMPACT_THRESHOLD = 25.0f

    private var wasInFreeFall = false
    private var freeFallTimestamp: Long = 0
    private var lastAlertTime: Long = 0
    private var countdownJob: Job? = null

    /**
     * Inicjalizuje wymagane systemowe usługi (SensorManager, Vibrator, SettingsRepository)
     * oraz pobiera domyślny akcelerometr urządzenia.
     */
    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        settingsRepository = SettingsRepository(applicationContext)
    }

    /**
     * Obsługuje komendy wysyłane do usługi z innych komponentów aplikacji oraz systemu.
     * Automatycznie pobiera numer opiekuna z lokalnej, zaszyfrowanej pamięci na wypadek restartu.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FallService", "Serwis uruchomiony. Akcja: ${intent?.action}")

        // 1. Zabezpieczenie Złotej Godziny: Ładowanie/Zapis numeru SOS
        val numberFromIntent = intent?.getStringExtra("GUARDIAN_NUMBER")

        if (numberFromIntent != null) {
            // Serwis uruchomiony z aplikacji - aktualizujemy numer w RAM i zapisujemy na stałe
            guardianPhoneNumber = numberFromIntent
            settingsRepository.saveGuardianPhoneNumber(numberFromIntent)
            Log.d("FallService", "Otrzymano i zapisano nowy numer opiekuna.")
        } else {
            // Serwis uruchomiony z tła (np. przez BootReceiver po restarcie telefonu)
            guardianPhoneNumber = settingsRepository.getGuardianPhoneNumber()
            Log.d("FallService", "Załadowano numer opiekuna z pamięci urządzenia.")
        }

        if (guardianPhoneNumber.isNullOrEmpty()) {
            Log.w("FallService", "UWAGA: Brak zapisanego numeru opiekuna. SMS nie zostanie wysłany w razie upadku!")
        }

        // 2. Obsługa akcji
        when (intent?.action) {
            "CANCEL" -> {
                cancelCountdown()
            }
            "STOP" -> {
                stopMonitoring()
                stopSelf()
            }
            "UPDATE_NUMBER" -> {
                // Numer został już zaktualizowany wyżej
            }
            "START" -> {
                startForegroundServiceWithNotification()
                startMonitoring()
            }
            else -> {
                // Obejmuje sytuację, gdy Intent action jest null (typowe dla automatycznego startu
                // z BootReceiver po restarcie urządzenia lub po ubiciu przez system).
                startForegroundServiceWithNotification()
                startMonitoring()
            }
        }

        // Zwracamy START_STICKY, żeby system sam reanimował serwis po ubiciu z braku pamięci RAM
        return START_STICKY
    }

    /**
     * Promuje usługę do rangi Foreground Service.
     */
    private fun startForegroundServiceWithNotification() {
        val channelId = com.zst.senior.assistant.utils.NotificationHelper.FALL_DETECTION_CHANNEL_ID
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(com.zst.senior.assistant.R.string.fall_service_title))
            .setContentText(getString(com.zst.senior.assistant.R.string.fall_service_desc))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()

        try {
            // Próba uruchomienia usługi - jeśli brak uprawnień w Android 14, system rzuci wyjątek
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(1, notification)
            }
        } catch (e: SecurityException) {
            // Przechwytujemy crash Androida 14!
            Log.e("FallService", "CRITICAL: Zablokowano start usługi. Brak uprawnień do lokalizacji!", e)
            // Bezpiecznie ubijamy serwis. Nie wykryje upadku, ale aplikacja przetrwa.
            stopSelf()
        } catch (e: Exception) {
            Log.e("FallService", "Nieznany błąd podczas startu Foreground Service", e)
            stopSelf()
        }
    }

    /**
     * Rejestruje nasłuchiwacz (listener) akcelerometru, rozpoczynając monitorowanie ruchów.
     */
    private fun startMonitoring() {
        if (FallStateRepository.state.value == FallDetectionState.Idle) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            Log.d("FallService", "Monitorowanie uruchomione w tle.")
        }
    }

    private fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        serviceScope.coroutineContext.cancelChildren()
        Log.d("FallService", "Monitorowanie zatrzymane.")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (FallStateRepository.state.value != FallDetectionState.Idle) return

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentAcceleration = sqrt(
                (event.values[0] * event.values[0] +
                        event.values[1] * event.values[1] +
                        event.values[2] * event.values[2]).toDouble()
            ).toFloat()

            // Wykrywanie swobodnego spadania
            if (currentAcceleration < FREE_FALL_THRESHOLD) {
                wasInFreeFall = true
                freeFallTimestamp = System.currentTimeMillis()
                return
            }

            // Wykrywanie uderzenia po fazie spadania
            if (wasInFreeFall) {
                val timeSinceFreeFall = System.currentTimeMillis() - freeFallTimestamp
                if (currentAcceleration > IMPACT_THRESHOLD && timeSinceFreeFall < 1000) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAlertTime > 5000) {
                        lastAlertTime = currentTime
                        startCountdown()
                    }
                    wasInFreeFall = false
                }
                if (timeSinceFreeFall > 1500) {
                    wasInFreeFall = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startCountdown() {
        stopMonitoring()
        triggerVibration(isCountdown = true)

        countdownJob?.cancel()
        countdownJob = serviceScope.launch {
            for (i in 15 downTo 1) {
                FallStateRepository.updateState(FallDetectionState.Countdown(i))
                delay(1000L)
            }
            triggerAlarm()
        }
    }

    private fun cancelCountdown() {
        countdownJob?.cancel()
        stopVibration()
        FallStateRepository.updateState(FallDetectionState.Idle)
        wasInFreeFall = false
        startMonitoring()
    }

    private fun triggerAlarm() {
        FallStateRepository.updateState(FallDetectionState.Alarm)
        triggerVibration(isCountdown = false)
        Log.e("FallService", "ALARM! Próba wysłania SMS z tła...")
        fetchLocationAndSendSms()
    }

    private fun fetchLocationAndSendSms() {
        val targetNumber = guardianPhoneNumber
        if (targetNumber.isNullOrEmpty()) {
            Log.e("FallService", "BRAK NUMERU OPIEKUNA! Nie można wysłać SMS.")
            return
        }

        val hasSmsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val hasLocPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasSmsPermission) return

        if (hasLocPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                val cancellationTokenSource = CancellationTokenSource()

                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                    .addOnSuccessListener { location: Location? ->
                        val msg = if (location != null) {
                            // Poprawiono budowę linku, aby zawsze działał w Google Maps
                            getString(com.zst.senior.assistant.R.string.fall_sms_location, "https://maps.google.com/?q=${location.latitude},${location.longitude}")
                        } else {
                            getString(com.zst.senior.assistant.R.string.fall_sms_no_gps)
                        }
                        sendSms(targetNumber, msg)
                    }.addOnFailureListener {
                        sendSms(targetNumber, getString(com.zst.senior.assistant.R.string.fall_sms_error))
                    }
            } catch (e: SecurityException) {
                sendSms(targetNumber, getString(com.zst.senior.assistant.R.string.fall_sms_generic))
            }
        } else {
            sendSms(targetNumber, getString(com.zst.senior.assistant.R.string.fall_sms_no_permission))
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION") SmsManager.getDefault()
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
            Log.d("FallService", "SMS wysłany!")
        } catch (e: Exception) {
            Log.e("FallService", "Błąd wysyłania SMS: ${e.message}")
        }
    }

    private fun triggerVibration(isCountdown: Boolean) {
        val pattern = if (isCountdown) longArrayOf(0, 300, 700) else longArrayOf(0, 500, 200, 500, 200, 1000)
        val repeatMode = if (isCountdown) 0 else -1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeatMode))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(pattern, repeatMode)
        }
    }

    private fun stopVibration() {
        vibrator.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMonitoring()
        stopVibration()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}