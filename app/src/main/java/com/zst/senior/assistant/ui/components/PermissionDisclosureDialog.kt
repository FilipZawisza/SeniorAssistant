package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zst.senior.assistant.R
import com.zst.senior.assistant.ui.theme.WcagYellow

/**
 * Komponent interfejsu (Prominent Disclosure) w postaci okna dialogowego, informujący
 * użytkownika o gromadzeniu danych o lokalizacji w tle.
 *
 * Wyświetlenie tego komunikatu przed właściwym monitem systemowym jest **ścisłym wymogiem
 * regulaminu Google Play**. Wyjaśnia on w jasny i zrozumiały sposób, dlaczego aplikacja
 * potrzebuje tych danych (ratowanie zdrowia poprzez powiadomienie SMS po upadku) oraz
 * fakt, że proces ten zachodzi również wtedy, gdy aplikacja nie jest aktywnie używana.
 *
 * @param onAccept Funkcja zwrotna wywoływana po zaakceptowaniu komunikatu przez użytkownika.
 * Powinna ona inicjować właściwe, systemowe żądanie o przyznanie uprawnień (np. `ACCESS_BACKGROUND_LOCATION`).
 * @param onDecline Funkcja zwrotna wywoływana, gdy użytkownik odrzuci prośbę lub zamknie okno
 * (np. klikając poza nie). Aplikacja powinna wtedy obsłużyć brak uprawnień (np. wyłączając funkcję detekcji upadków).
 */
@Composable
fun PermissionDisclosureDialog(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val isHighContrast = MaterialTheme.colorScheme.primary == WcagYellow
    // Zmienna przechowująca stan przewijania dla treści komunikatu
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDecline,
        containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface,
        modifier = if (isHighContrast) Modifier.border(2.dp, WcagYellow, RoundedCornerShape(28.dp)) else Modifier,
        title = {
            Text(
                text = stringResource(R.string.comp_perm_disclosure_title),
                // Używamy stylu z motywu, żeby tekst tytułu ładnie reagował na skalę
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Text(
                    text = stringResource(R.string.comp_perm_disclosure_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.comp_perm_disclosure_google_req),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.primary,
                    contentColor = if (isHighContrast) Color.Black else Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.comp_perm_disclosure_accept),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDecline,
                // Analogicznie dla przycisku odrzucenia
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                border = if (isHighContrast) BorderStroke(1.dp, Color.White) else ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(
                    text = stringResource(R.string.comp_perm_disclosure_decline),
                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}