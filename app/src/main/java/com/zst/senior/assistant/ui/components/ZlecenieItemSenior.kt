package com.zst.senior.assistant.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.PelneZlecenieDlaSeniora

/**
 * Komponent interfejsu wyświetlający szczegółowe informacje o zleceniu z perspektywy Seniora.
 *
 * Karta ta jest zoptymalizowana pod kątem dostępności (wsparcie dla trybu wysokiego kontrastu)
 * oraz czytelności. Jej układ i dostępne akcje zmieniają się dynamicznie w zależności od
 * statusu zlecenia:
 * * **Wolne:** Wyświetla tylko opis i pozwala na anulowanie zlecenia.
 * * **Aktywne:** Odkrywa dane przypisanego wolontariusza, w tym jego ocenę oraz interaktywny
 * przycisk ułatwiający szybkie nawiązanie połączenia telefonicznego.
 * * **DoPotwierdzenia:** Wyświetla przyciski pozwalające seniorowi zweryfikować pracę
 * wolontariusza (potwierdzenie wykonania lub zgłoszenie braku realizacji).
 *
 * @param zlecenie Obiekt [PelneZlecenieDlaSeniora] zawierający pełne dane zlecenia oraz ewentualnego wolontariusza.
 * @param onPotwierdz Funkcja zwrotna wywoływana, gdy senior potwierdza pomyślne wykonanie zlecenia.
 * @param onOdrzuc Funkcja zwrotna wywoływana, gdy senior zgłasza oszustwo/brak wykonania zadania pomimo statusu. Domyślnie pusta.
 * @param onAnuluj Funkcja zwrotna wywoływana, gdy senior decyduje się usunąć oczekujące lub aktywne zlecenie. Domyślnie pusta.
 * @param isLoading Flaga logiczna blokująca przyciski interakcji w trakcie oczekiwania na odpowiedź z serwera.
 */
@Composable
fun ZlecenieItemSenior(
    zlecenie: PelneZlecenieDlaSeniora,
    onPotwierdz: () -> Unit,
    onOdrzuc: () -> Unit = {},
    onAnuluj: () -> Unit = {},
    isLoading: Boolean
) {
    val context = LocalContext.current
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black

    val successGreen = Color(0xFF4CAF50)
    val pendingOrange = MaterialTheme.colorScheme.secondary
    val brandBlue = MaterialTheme.colorScheme.primary
    val cancelRed = Color(0xFFD32F2F)

    val cardContainerColor = if (isHighContrast) Color.Black else Color.White
    val cardBorder = if (isHighContrast) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        if (zlecenie.status == "DoPotwierdzenia") BorderStroke(1.dp, pendingOrange.copy(alpha = 0.5f)) else null
    }
    val cardElevation = if (isHighContrast) 0.dp else 6.dp

    val titleColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isHighContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    val (statusColor, statusText, statusIcon) = if (isHighContrast) {
        Triple(MaterialTheme.colorScheme.primary, przekszalcStatusNaTekst(context, zlecenie.status), pobierzIkoneStatusu(zlecenie.status))
    } else {
        when (zlecenie.status) {
            "Wolne" -> Triple(brandBlue, stringResource(R.string.comp_order_item_senior_status_free), Icons.Default.Schedule)
            "Aktywne" -> Triple(successGreen, stringResource(R.string.comp_order_item_senior_status_active),
                Icons.AutoMirrored.Filled.DirectionsRun
            )
            "DoPotwierdzenia" -> Triple(pendingOrange, stringResource(R.string.comp_order_item_senior_status_confirm), Icons.Default.Verified)
            else -> Triple(Color.Gray, zlecenie.status, Icons.Default.Schedule)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = cardBorder
    ) {
        Column(Modifier.padding(20.dp)) {

            val badgeBg = if (isHighContrast) Color.Black else statusColor.copy(alpha = 0.1f)
            val badgeBorder = if (isHighContrast) BorderStroke(1.dp, statusColor) else null

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(badgeBg, RoundedCornerShape(8.dp))
                    .then(if (badgeBorder != null) Modifier.border(badgeBorder, RoundedCornerShape(8.dp)) else Modifier)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = statusText.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = zlecenie.opis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = titleColor
            )

            if (zlecenie.status != "Wolne") {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = if (isHighContrast) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f))
                Spacer(Modifier.height(16.dp))

                Text(stringResource(R.string.comp_order_item_senior_volunteer_label), style = MaterialTheme.typography.labelMedium, color = labelColor)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val avatarBg = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    val avatarBorder = if (isHighContrast) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null

                    Surface(
                        shape = CircleShape,
                        color = avatarBg,
                        border = avatarBorder,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.width(12.dp))

                    // --- SEKCJA IMIENIA I OCENY GWIAZDKOWEJ ---
                    Column {
                        Text(
                            text = zlecenie.wolontariuszImieNazwisko,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                        // Jeśli wolontariusz ma przynajmniej 1 ukończone zlecenie, pokazujemy ocenę
                        if (zlecenie.wolontariuszLiczbaZlecen > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = stringResource(R.string.leaderboard_star_desc),
                                    tint = if (isHighContrast) MaterialTheme.colorScheme.primary else Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = String.format(LocalLocale.current.platformLocale, "%.1f (%d pomaganych)", zlecenie.wolontariuszOcena, zlecenie.wolontariuszLiczbaZlecen),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = labelColor
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.comp_order_item_senior_new_volunteer),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                val phoneBg = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                val phoneBorder = if (isHighContrast) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_DIAL,
                                "tel:${zlecenie.wolontariuszTelefon}".toUri())
                            context.startActivity(intent)
                        }
                        .background(phoneBg)
                        .then(if (phoneBorder != null) Modifier.border(phoneBorder, RoundedCornerShape(12.dp)) else Modifier)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.comp_order_item_senior_call_volunteer, zlecenie.wolontariuszTelefon),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (zlecenie.status == "DoPotwierdzenia") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GradientButton(
                        text = if(isLoading) stringResource(R.string.common_waiting) else stringResource(R.string.comp_order_item_senior_confirm_btn),
                        onClick = onPotwierdz,
                        enabled = !isLoading,
                        icon = Icons.Default.CheckCircle
                    )
                    OutlinedButton(
                        onClick = onOdrzuc,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        border = BorderStroke(1.dp, if (isHighContrast) MaterialTheme.colorScheme.primary else cancelRed)
                    ) {
                        Text(
                            text = stringResource(R.string.comp_order_item_senior_reject_btn),
                            color = if (isHighContrast) MaterialTheme.colorScheme.primary else cancelRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (zlecenie.status == "Wolne" || zlecenie.status == "Aktywne") {
                OutlinedButton(
                    onClick = onAnuluj,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    border = BorderStroke(1.dp, if (isHighContrast) MaterialTheme.colorScheme.primary else cancelRed)
                ) {
                    Text(
                        text = stringResource(R.string.comp_order_item_senior_cancel_btn),
                        color = if (isHighContrast) MaterialTheme.colorScheme.primary else cancelRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Funkcja pomocnicza tłumacząca techniczny status zlecenia na tekst przyjazny dla seniora.
 */
private fun przekszalcStatusNaTekst(context: android.content.Context, status: String): String {
    return when (status) {
        "Wolne" -> context.getString(R.string.comp_order_item_senior_status_free)
        "Aktywne" -> context.getString(R.string.comp_order_item_senior_status_active)
        "DoPotwierdzenia" -> context.getString(R.string.comp_order_item_senior_status_confirm)
        else -> status
    }
}

/**
 * Funkcja pomocnicza przypisująca odpowiednią ikonę wektorową w zależności od statusu zlecenia.
 */
private fun pobierzIkoneStatusu(status: String): ImageVector {
    return when (status) {
        "Wolne" -> Icons.Default.Schedule
        "Aktywne" -> Icons.AutoMirrored.Filled.DirectionsRun
        "DoPotwierdzenia" -> Icons.Default.Verified
        else -> Icons.Default.Schedule
    }
}