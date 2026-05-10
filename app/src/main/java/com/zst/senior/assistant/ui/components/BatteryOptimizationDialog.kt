package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zst.senior.assistant.R
import com.zst.senior.assistant.ui.theme.WcagYellow

/**
 * Komponent interfejsu (Dialog) informujący użytkownika o konieczności wyłączenia
 * systemowej optymalizacji baterii dla tej aplikacji.
 *
 * Wyłączenie optymalizacji jest kluczowe, aby krytyczne funkcje działające w tle,
 * takie jak detekcja upadków, alarmy przypominające o lekach czy odświeżanie lokalizacji,
 * działały niezawodnie i nie były przedwcześnie "usypiane" przez system Android.
 *
 * @param onConfirm Funkcja zwrotna wywoływana po zatwierdzeniu przez użytkownika chęci
 * zmiany ustawień. Zazwyczaj powinna wywoływać intencję przenoszącą do ustawień baterii urządzenia.
 * @param onDismiss Funkcja zwrotna wywoływana po wybraniu opcji "Później" lub kliknięciu w tło,
 * służąca do zamknięcia okna dialogowego bez podejmowania akcji.
 */
@Composable
fun BatteryOptimizationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isHighContrast = MaterialTheme.colorScheme.primary == WcagYellow

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
        modifier = if (isHighContrast) Modifier.border(2.dp, WcagYellow, RoundedCornerShape(28.dp)) else Modifier,
        title = {
            Text(
                text = stringResource(R.string.battery_dialog_title),
                fontWeight = FontWeight.Bold,
                color = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(R.string.battery_dialog_message),
                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.battery_dialog_confirm),
                    color = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.battery_dialog_dismiss),
                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}