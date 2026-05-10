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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.zst.senior.assistant.model.PelneZlecenieDlaWolontariusza
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * Komponent interfejsu wyświetlający szczegółowe informacje o zleceniu z perspektywy Wolontariusza.
 *
 * Karta jest w pełni zoptymalizowana pod kątem dostępności, ze szczególnym uwzględnieniem
 * trybu wysokiego kontrastu (WCAG). Zawiera kompleksowe dane niezbędne do realizacji zadania,
 * w tym dane kontaktowe seniora oraz interaktywny przycisk pozwalający na bezpośrednie
 * połączenie telefoniczne.
 *
 * Zachowanie komponentu zmienia się dynamicznie w zależności od statusu zlecenia:
 * * Jeśli zlecenie ma status "DoPotwierdzenia", główny przycisk akcji zostaje zablokowany,
 * a w nagłówku pojawia się informacja o oczekiwaniu na weryfikację (potwierdzenie) ze strony seniora.
 * * W trybie standardowym (gdy status to "Aktywne"), wolontariusz ma możliwość zgłoszenia
 * zakończenia zlecenia za pomocą przycisku na dole ekranu.
 *
 * @param zlecenie Obiekt [PelneZlecenieDlaWolontariusza] zawierający pełne dane zlecenia, w tym adresy (stały i GPS) oraz dane kontaktowe seniora.
 * @param onZakoncz Funkcja zwrotna wywoływana, gdy wolontariusz deklaruje wykonanie i zgłasza chęć zakończenia zlecenia.
 * @param isLoading Flaga logiczna blokująca przyciski interakcji w trakcie oczekiwania na odpowiedź z serwera (np. podczas finalizowania zlecenia).
 */
@Composable
fun ZlecenieItemWolontariusz(
    zlecenie: PelneZlecenieDlaWolontariusza,
    onZakoncz: () -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val isPending = zlecenie.status == "DoPotwierdzenia"

    // --- DETEKCJA TRYBU HC ---
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black

    // --- KOLORY I STYLE ---
    val pendingColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color(0xFFFF9800)

    // Tło i Ramka Karty
    val cardContainerColor = if (isHighContrast) Color.Black else Color.White
    val cardBorder = if (isHighContrast) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        if (isPending) BorderStroke(1.dp, pendingColor.copy(alpha = 0.5f)) else null
    }

    // Kolory tekstów
    val titleColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val labelColor = if (isHighContrast) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // --- NAGŁÓWEK: STATUS I OPIS ---
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isPending) {
                        // Badge statusu
                        val badgeBg = if (isHighContrast) Color.Black else pendingColor.copy(alpha = 0.1f)
                        val badgeBorder = if (isHighContrast) BorderStroke(1.dp, pendingColor) else null

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .background(badgeBg, RoundedCornerShape(8.dp))
                                .then(if (badgeBorder != null) Modifier.border(badgeBorder, RoundedCornerShape(8.dp)) else Modifier)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.HourglassEmpty, null, tint = pendingColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.comp_order_item_volunteer_waiting),
                                style = MaterialTheme.typography.labelSmall,
                                color = pendingColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = zlecenie.opis,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = titleColor,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = if (isHighContrast) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // --- SEKCJA DANYCH ---

            // Data dodania
            if (zlecenie.timestamp > 0) {
                val date = Date(zlecenie.timestamp)
                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale).apply {
                    timeZone = TimeZone.getTimeZone("Europe/Warsaw")
                }
                InfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.order_details_date_label),
                    value = format.format(date),
                    iconColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color(0xFF673AB7),
                    textColor = contentColor,
                    labelColor = labelColor,
                    isHighContrast = isHighContrast
                )
                Spacer(Modifier.height(12.dp))
            }

            // Senior
            InfoRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.comp_order_item_volunteer_senior_label),
                value = zlecenie.seniorImieNazwisko,
                iconColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                textColor = contentColor,
                labelColor = labelColor,
                isHighContrast = isHighContrast
            )

            Spacer(Modifier.height(12.dp))

            // Adres Aktualny (GPS)
            if (zlecenie.seniorAdresAktualny != null) {
                InfoRow(
                    icon = Icons.Default.Place,
                    label = stringResource(R.string.comp_order_item_volunteer_loc_gps),
                    value = zlecenie.seniorAdresAktualny!!,
                    iconColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color(0xFFE91E63),
                    textColor = contentColor,
                    labelColor = labelColor,
                    isHighContrast = isHighContrast
                )
                Spacer(Modifier.height(12.dp))
            }

            // Adres Stały
            InfoRow(
                icon = Icons.Default.Home,
                label = stringResource(R.string.comp_order_item_volunteer_loc_profile),
                value = zlecenie.seniorAdresStały,
                iconColor = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                textColor = contentColor,
                labelColor = labelColor,
                isHighContrast = isHighContrast
            )

            Spacer(Modifier.height(12.dp))

            // Telefon (Interaktywny)
            val phoneBg = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            val phoneBorder = if (isHighContrast) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            val phoneIconColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.White
            val phoneCircleColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL,
                            "tel:${zlecenie.seniorTelefon}".toUri())
                        context.startActivity(intent)
                    }
                    .background(phoneBg)
                    .then(if (phoneBorder != null) Modifier.border(phoneBorder, RoundedCornerShape(12.dp)) else Modifier)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = phoneCircleColor,
                    border = if (isHighContrast) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Phone, null, tint = phoneIconColor, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.comp_order_item_volunteer_call_label), style = MaterialTheme.typography.labelSmall, color = labelColor)
                    Text(
                        text = zlecenie.seniorTelefon,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isHighContrast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // --- PRZYCISK AKCJI ---
            GradientButton(
                text = if (isPending) stringResource(R.string.comp_order_item_volunteer_pending_btn) else (if(isLoading) stringResource(R.string.common_waiting) else stringResource(R.string.comp_order_item_volunteer_finish_btn)),
                onClick = onZakoncz,
                enabled = !isPending && !isLoading,
                icon = if (!isPending) Icons.Default.CheckCircle else null
            )
        }
    }
}

/**
 * Wewnętrzny komponent pomocniczy służący do spójnego wyświetlania wierszy informacyjnych.
 * * Generuje powtarzalny układ składający się z okrągłego tła z ikoną wektorową, etykiety
 * pomocniczej (tytułu) oraz właściwej wartości tekstowej. Komponent automatycznie
 * dostosowuje swoje krawędzie i tła w zależności od tego, czy aktywny jest tryb
 * wysokiego kontrastu (HC).
 *
 * @param icon Ikona wektorowa reprezentująca charakter wyświetlanych danych (np. ikona telefonu).
 * @param label Krótki tekst opisujący wyświetlaną wartość (np. "Adres stały").
 * @param value Główna informacja tekstowa przekazywana do wyświetlenia (np. nazwa ulicy i numer).
 * @param iconColor Podstawowy kolor ikony wykorzystywany w standardowym trybie wyświetlania.
 * @param textColor Kolor głównego tekstu informacyjnego.
 * @param labelColor Kolor mniejszego tekstu pomocniczego (etykiety).
 * @param isHighContrast Flaga informująca, czy widok powinien używać stylizacji o wysokim kontraście.
 */
@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    textColor: Color,
    labelColor: Color,
    isHighContrast: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = if (isHighContrast) Color.Black else iconColor.copy(alpha = 0.1f),
            border = if (isHighContrast) BorderStroke(1.dp, iconColor) else null,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = labelColor)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}