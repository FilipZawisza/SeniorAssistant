package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.Seniorm2Item
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.Locale

/**
 * Mapa mapująca obiekty [DayOfWeek] (z biblioteki java.time) na ich polskie,
 * skrócone odpowiedniki tekstowe, wykorzystywane w interfejsie użytkownika.
 */
/**
 * Generuje mapę skrótów nazw dni tygodnia pobieranych z zasobów językowych aplikacji.
 *
 * @return Mapa łącząca obiekt [DayOfWeek] ze zlokalizowanym skrótem (np. Pn, Mon).
 */
@Composable
fun getDayOfWeekAbbreviations(): Map<DayOfWeek, String> {
    return mapOf(
        DayOfWeek.MONDAY to stringResource(R.string.day_mon),
        DayOfWeek.TUESDAY to stringResource(R.string.day_tue),
        DayOfWeek.WEDNESDAY to stringResource(R.string.day_wed),
        DayOfWeek.THURSDAY to stringResource(R.string.day_thu),
        DayOfWeek.FRIDAY to stringResource(R.string.day_fri),
        DayOfWeek.SATURDAY to stringResource(R.string.day_sat),
        DayOfWeek.SUNDAY to stringResource(R.string.day_sun)
    )
}

/**
 * Helper do wykrywania trybu wysokiego kontrastu (czarne tło).
 * Sprawdza, czy tło zdefiniowane w aktualnym [MaterialTheme] jest czarne (WcagBlack).
 * * @return `true` jeśli aplikacja działa w trybie wysokiego kontrastu, w przeciwnym razie `false`.
 */
@Composable
private fun isHighContrastMode(): Boolean {
    return MaterialTheme.colorScheme.background == Color(0xFF000000)
}

/**
 * Komponent reprezentujący pojedynczy element na liście harmonogramu (przypomnienie).
 * Wyświetla godzinę, nazwę przypomnienia oraz dni, w których się ono powtarza.
 * Posiada przycisk pozwalający na usunięcie elementu.
 *
 * @param item Obiekt danych [Seniorm2Item] reprezentujący szczegóły przypomnienia.
 * @param onClick Funkcja zwrotna wywoływana po kliknięciu w kartę (np. w celu edycji).
 * @param onDelete Funkcja zwrotna wywoływana po kliknięciu w ikonę kosza (usunięcie).
 */
@Composable
fun Seniorm2DisplayItem(
    item: Seniorm2Item,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isHC = isHighContrastMode()
    val primaryColor = MaterialTheme.colorScheme.primary

    val everydayStr = stringResource(R.string.reminders_everyday)
    val onceStr = stringResource(R.string.reminders_once)
    val abbreviations = getDayOfWeekAbbreviations()

    val daysFormatter: (Set<DayOfWeek>) -> String = { days ->
        if (days.size == 7) everydayStr
        else if (days.isEmpty()) onceStr
        else {
            days.sorted()
                .joinToString(", ") { abbreviations[it] ?: "" }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHC) Color.Black else Color.White),
        border = if (isHC) BorderStroke(2.dp, primaryColor) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isHC) primaryColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (isHC) Color.Black else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.time,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isHC) primaryColor else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isHC) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EventRepeat,
                        contentDescription = null,
                        tint = if (isHC) primaryColor.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = daysFormatter(item.daysOfWeek),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isHC) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.reminders_delete_desc),
                    tint = if (isHC) primaryColor else MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Okno dialogowe umożliwiające dodanie nowego lub edycję istniejącego przypomnienia
 * w harmonogramie Seniora (np. przypomnienie o lekach).
 * Zapewnia formularz z polem tekstowym, selektorem czasu i dniami tygodnia.
 * Został w pełni zoptymalizowany pod kątem trybu wysokiego kontrastu (Neon/WCAG).
 *
 * @param itemToEdit Obiekt [Seniorm2Item], który ma zostać zedytowany. Jeśli jego właściwości
 * są puste, dialog zachowuje się jak formularz tworzenia nowego elementu.
 * @param onDismiss Funkcja zamykająca dialog bez zapisywania zmian.
 * @param onSave Funkcja zwrotna przekazująca nowo utworzony lub zaktualizowany obiekt
 * [Seniorm2Item] do ViewModelu.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSeniorHarmonogramDialog(
    itemToEdit: Seniorm2Item,
    onDismiss: () -> Unit,
    onSave: (Seniorm2Item) -> Unit
) {
    var name by remember { mutableStateOf(itemToEdit.name) }
    var time by remember { mutableStateOf(itemToEdit.time) }
    var selectedDays by remember { mutableStateOf(itemToEdit.daysOfWeek) }

    val isEditing = itemToEdit.name.isNotBlank()

    // --- KONFIGURACJA KOLORÓW NEONOWYCH ---
    val isHC = isHighContrastMode()

    // W Theme.kt: primary = WcagYellow (Neon), background = WcagBlack
    // Pobieramy te kolory dynamicznie:
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    // Kolory dedykowane dla Dialogu
    val dialogBg = if (isHC) backgroundColor else Color.White

    // W trybie HC tekst ma być neonowy (primary), w zwykłym czarny/szary
    val textColor = if (isHC) primaryColor else Color.Black
    val labelColor = if (isHC) primaryColor else Color.Gray
    val inputBorderColor = if (isHC) primaryColor else Color.Gray

    var showTimePicker by remember { mutableStateOf(false) }
    val initialTime = try { LocalTime.parse(time) } catch (_: Exception) { LocalTime.now() }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                time = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            },
            state = timePickerState
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        modifier = Modifier
            // W trybie HC dodajemy grubą neonową ramkę
            .border(if (isHC) 2.dp else 0.dp, primaryColor, RoundedCornerShape(28.dp)),
        title = {
            Text(
                if (isEditing) stringResource(R.string.reminders_edit_title) else stringResource(R.string.reminders_new_title),
                fontWeight = FontWeight.Bold,
                color = textColor, // Neonowy w HC
                fontSize = 22.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // 1. POLE TEKSTOWE - NAZWA
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.reminders_field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        // STANY NIEAKTYWNE (Unfocused) - też mają być neonowe w HC
                        unfocusedBorderColor = inputBorderColor,
                        unfocusedLabelColor = labelColor,
                        unfocusedTextColor = textColor,

                        // STANY AKTYWNE (Focused)
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        focusedTextColor = textColor,

                        // KURSOR
                        cursorColor = primaryColor,

                        // TŁO
                        focusedContainerColor = dialogBg,
                        unfocusedContainerColor = dialogBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // 2. WYBÓR GODZINY
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.calendar_field_time)) },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = inputBorderColor,
                            disabledLabelColor = labelColor,
                            disabledTextColor = textColor,
                            disabledContainerColor = dialogBg
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showTimePicker = true }
                    )
                }

                // 3. DNI TYGODNIA (Pastylki)
                val abbreviations = getDayOfWeekAbbreviations()
                Column {
                    Text(
                        stringResource(R.string.reminders_repeat_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = labelColor, // Neonowy nagłówek w HC
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val days = DayOfWeek.entries

                        days.forEach { day ->
                            val selected = selectedDays.contains(day)

                            // LOGIKA KOLORÓW DLA DNI
                            // Zaznaczony: Tło Neon, Tekst Czarny
                            // Niezaznaczony (HC): Tło Czarne, Tekst Neon, Ramka Neon

                            val chipContainerColor = if (selected) primaryColor else Color.Transparent

                            // Jeśli zaznaczony -> Czarny tekst (onPrimary z Theme)
                            // Jeśli niezaznaczony HC -> Neonowy tekst (primary)
                            val chipContentColor = if (selected)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                (if (isHC) primaryColor else Color.Gray)

                            val chipBorderColor = if (selected)
                                Color.Transparent
                            else
                                (if (isHC) primaryColor else Color.LightGray)

                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedDays = if (selected) selectedDays - day else selectedDays + day
                                },
                                label = {
                                    Text(
                                        abbreviations[day] ?: "",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(24.dp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = chipContainerColor,
                                    selectedLabelColor = chipContentColor,
                                    containerColor = Color.Transparent,
                                    labelColor = chipContentColor,
                                    disabledContainerColor = Color.Transparent,
                                    disabledLabelColor = chipContentColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selected,
                                    borderColor = chipBorderColor,
                                    borderWidth = 2.dp
                                ),
                                shape = CircleShape,
                                modifier = Modifier.height(40.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = itemToEdit.copy(name = name, time = time, daysOfWeek = selectedDays)
                    onSave(newItem)
                },
                enabled = name.isNotBlank() && time.isNotBlank() && selectedDays.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    // Tekst na przycisku: Czarny (onPrimary)
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = if (isHC) Color.DarkGray else Color.LightGray,
                    disabledContentColor = Color.Gray
                ),
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Text(
                    stringResource(R.string.common_save),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    stringResource(R.string.common_cancel),
                    // Tekst "Anuluj" też neonowy w HC
                    color = if (isHC) primaryColor else Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}