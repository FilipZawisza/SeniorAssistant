package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.WorkOutline
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.ZlecenieRef
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.ui.theme.BrandOrange

/**
 * Komponent interfejsu reprezentujący pojedynczy element na liście zleceń (zadań).
 * * Służy do wyświetlania podstawowych informacji o zleceniu, takich jak jego opis oraz (opcjonalnie) status.
 * Komponent jest w pełni zoptymalizowany pod kątem dostępności: automatycznie wykrywa tryb
 * wysokiego kontrastu (HC) i dostosowuje do niego kolory tekstów, ikon, tła oraz dodaje
 * wyraźne obramowania, aby zachować zgodność ze standardami WCAG.
 *
 * @param zlecenie Obiekt modelu [ZlecenieRef] zawierający dane zlecenia do wyświetlenia (opis, status).
 * @param isAdminView Flaga logiczna określająca, czy użytkownik przegląda listę z perspektywy
 * administratora (opiekuna/koordynatora). Jeśli `true`, pod opisem zlecenia zostanie wyświetlona
 * specjalna etykieta (badge) informująca o aktualnym statusie zlecenia (np. "Wolne", "Aktywne").
 * @param onClick Funkcja zwrotna wywoływana po kliknięciu w dowolne miejsce na karcie.
 * Zazwyczaj inicjuje nawigację do ekranu szczegółów wybranego zlecenia.
 */
@Composable
fun ZlecenieItemPublic(
    zlecenie: ZlecenieRef,
    isAdminView: Boolean,
    onClick: () -> Unit
) {
    val statusText = zlecenie.status ?: "Wolne"

    // --- DETEKCJA TRYBU HC ---
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val context = androidx.compose.ui.platform.LocalContext.current

    val displayStatus = when(statusText) {
        "Wolne" -> stringResource(R.string.comp_order_item_senior_status_free)
        "Aktywne" -> stringResource(R.string.comp_order_item_senior_status_active)
        "DoPotwierdzenia" -> stringResource(R.string.comp_order_item_senior_status_confirm)
        "Zakonczone" -> stringResource(R.string.status_finished)
        else -> statusText
    }

    // --- LOGIKA KOLORÓW STATUSU (Standard vs HC) ---
    val statusColor = if (isHighContrast) {
        MaterialTheme.colorScheme.primary // Żółty w HC
    } else {
        when (statusText) {
            "Wolne" -> BrandBlue
            "Aktywne" -> Color(0xFF4CAF50) // Zielony
            "DoPotwierdzenia" -> BrandOrange
            "Zakonczone" -> Color.Gray
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    // --- KOLORY KARTY ---
    val containerColor = if (isHighContrast) Color.Black else Color.White
    val cardBorder = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    val elevation = if (isHighContrast) 0.dp else 4.dp

    // --- KOLORY TEKSTÓW ---
    val titleColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val iconTint = if (isHighContrast) MaterialTheme.colorScheme.primary else BrandBlue
    val iconBg = if (isHighContrast) Color.Black else BrandBlue.copy(alpha = 0.1f)
    val iconBorder = if (isHighContrast) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = cardBorder // Kluczowe dla widoczności w trybie HC
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- IKONA GŁÓWNA ---
            Surface(
                shape = CircleShape,
                color = iconBg,
                border = iconBorder,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.WorkOutline,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // --- TREŚĆ ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = zlecenie.opis ?: stringResource(R.string.comp_order_item_public_no_desc),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = titleColor
                )

                // Status jako pastylka (badge)
                if (isAdminView) {
                    Spacer(modifier = Modifier.padding(top = 6.dp))

                    val badgeBg = if (isHighContrast) Color.Black else statusColor.copy(alpha = 0.1f)
                    val badgeBorder = if (isHighContrast) BorderStroke(1.dp, statusColor) else null

                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(6.dp),
                        border = badgeBorder
                    ) {
                        Text(
                            text = displayStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // --- STRZAŁKA ---
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.about_open_doc_desc),
                tint = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}