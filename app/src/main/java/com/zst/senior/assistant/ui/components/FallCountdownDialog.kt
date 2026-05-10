package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zst.senior.assistant.R

/**
 * Komponent interfejsu (Dialog) wyświetlany krytycznym momencie po wykryciu potencjalnego upadku.
 *
 * Prezentuje wyraźne wizualnie odliczanie do momentu automatycznego wysłania wezwania
 * o pomoc (np. SMS z lokalizacją). Daje użytkownikowi szansę na anulowanie procedury w przypadku
 * fałszywego alarmu (np. telefon upadł na dywan, ale użytkownik jest bezpieczny).
 * Okno to celowo blokuje możliwość zamknięcia go poprzez kliknięcie w tło, aby uniknąć
 * przypadkowego zignorowania krytycznej sytuacji.
 *
 * @param secondsLeft Aktualna liczba sekund pozostałych do wywołania ostatecznego alarmu.
 * Powinna być przekazywana z ViewModelu/Serwisu w czasie rzeczywistym.
 * @param onCancel Funkcja zwrotna wywoływana, gdy użytkownik naciśnie przycisk odwołania alarmu.
 * Powinna przerwać logikę odliczania, zresetować stan detektora i zamknąć to okno.
 */
@Composable
fun FallCountdownDialog(
    secondsLeft: Int,
    onCancel: () -> Unit
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    // Dialog blokuje interakcję z tłem (onDismissRequest puste)
    Dialog(onDismissRequest = { }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .padding(16.dp)
                .then(if (isHighContrast) Modifier.border(2.dp, primaryColor, RoundedCornerShape(28.dp)) else Modifier)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // --- IKONA ALARMU ---
                // Duża, czerwona, pulsująca wizualnie przez tło
                Surface(
                    shape = CircleShape,
                    color = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = stringResource(R.string.comp_fall_dialog_alarm_desc),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- TYTUŁ ---
                Text(
                    text = stringResource(R.string.comp_fall_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.comp_fall_dialog_countdown_prefix),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isHighContrast) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // --- LICZNIK ---
                // Ogromna czcionka, kluczowa informacja
                Text(
                    text = "$secondsLeft",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // --- PRZYCISK ANULUJ ---
                // Duży przycisk "Tabletka", łatwy do kliknięcia
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50), // Pełne zaokrąglenie
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary,
                        contentColor = if (isHighContrast) Color.Black else Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_cancel), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        stringResource(R.string.comp_fall_dialog_cancel_btn),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    stringResource(R.string.comp_fall_dialog_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isHighContrast) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}