package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.unit.dp
import com.zst.senior.assistant.model.Service

/**
 * Komponent interfejsu reprezentujący pojedynczy kafelek usługi (element menu) w aplikacji.
 *
 * Służy jako przycisk nawigacyjny prowadzący do konkretnej funkcjonalności.
 * Wyświetla ikonę usługi umieszczoną na okrągłym, delikatnie zabarwionym tle,
 * wyraźny tytuł oraz subtelną strzałkę na końcu wiersza, która wizualnie sugeruje
 * użytkownikowi możliwość kliknięcia i przejścia dalej.
 *
 * @param service Obiekt modelu [Service] zawierający dane do wyświetlenia (tytuł i wektorową ikonę).
 * @param onClick Funkcja zwrotna wywoływana po kliknięciu w dowolne miejsce na karcie.
 * Powinna inicjować nawigację do docelowego ekranu wybranej usługi.
 */
@Composable
fun ServiceItem(service: Service, onClick: () -> Unit) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        // Spójne zaokrąglenie z resztą kafelków
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        // Wsparcie High Contrast
        colors = CardDefaults.cardColors(containerColor = if (isHighContrast) Color.Black else Color.White),
        border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IKONA W TLE ---
            Surface(
                shape = CircleShape,
                color = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = service.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // --- TYTUŁ ---
            Text(
                text = stringResource(service.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f) // Tekst zajmuje dostępną przestrzeń
            )

            // --- STRZAŁKA ---
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}