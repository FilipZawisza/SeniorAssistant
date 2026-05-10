package com.zst.senior.assistant.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.zst.senior.assistant.services.FallDetectionService

/**
 * Nasłuchuje zdarzeń systemowych takich jak start urządzenia (Boot) lub
 * aktualizacja aplikacji, aby automatycznie wznowić działanie kluczowych usług.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Otrzymano akcję systemową: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            Log.d("BootReceiver", "Uruchamiam FallDetectionService w tle po restarcie!")

            val serviceIntent = Intent(context, FallDetectionService::class.java)

            // Od Androida 8.0 (Oreo) usługi w tle muszą być uruchamiane jako Foreground
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}