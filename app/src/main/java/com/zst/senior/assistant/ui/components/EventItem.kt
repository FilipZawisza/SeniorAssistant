package com.zst.senior.assistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.CalendarEvent
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// --- 1. DETEKCJA TRYBU NEON (Helper) ---
// Pomocnicza funkcja lub zmienne wewnątrz komponentów
// (Zastosowana lokalnie w każdym komponencie dla pewności)

// --- 2. ITEM LISTY (EventItem) ---
/**
 * Komponent reprezentujący pojedyncze wydarzenie na liście kalendarza.
 * Wyświetla czas, tytuł oraz przycisk pozwalający na usunięcie wydarzenia.
 * Automatycznie dostosowuje kolorystykę do trybu wysokiego kontrastu (Neon).
 *
 * @param event Obiekt [CalendarEvent] zawierający dane wydarzenia do wyświetlenia.
 * @param onDelete Funkcja zwrotna wywoływana po kliknięciu przycisku usunięcia (kosza).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventItem(event: CalendarEvent, onDelete: () -> Unit) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    // Kolory karty
    val cardColor = if (isHighContrast) Color.Black else Color.White
    val borderStroke = if (isHighContrast) BorderStroke(2.dp, primaryColor) else null
    val contentColor = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = borderStroke
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ikona w kółku
            Surface(
                shape = CircleShape,
                color = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccessTimeFilled,
                        contentDescription = null,
                        // Jeśli tło neonowe (HC), ikona czarna. Jeśli tło jasne, ikona primary.
                        tint = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Treść
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.time,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Przycisk usuwania
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// --- 3. OKNO DODAWANIA (AddEventDialog) ---
/**
 * Komponent okna dialogowego umożliwiającego użytkownikowi dodanie nowego wydarzenia.
 * Składa się z pola wyboru godziny (uruchamiającego [TimePickerDialog]) oraz pola wprowadzania tytułu wydarzenia.
 * Dostosowuje wygląd elementów (ramki, kolory tła i czcionek) do trybu wysokiego kontrastu.
 *
 * @param onDismiss Funkcja zamykająca okno dialogowe bez zapisu (np. kliknięcie Anuluj).
 * @param onSave Funkcja zapisująca wprowadzone dane. Oczekuje parametrów [LocalTime] i [String] (tytuł).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(onDismiss: () -> Unit, onSave: (LocalTime, String) -> Unit) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    // Kolory
    val dialogBg = if (isHighContrast) Color.Black else Color.White
    val textColor = if (isHighContrast) primaryColor else Color.Black
    val inputContainer = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant
    val inputContent = if (isHighContrast) Color.White else Color.Black

    var title by remember { mutableStateOf("") }
    // Domyślna godzina
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePicker by remember { mutableStateOf(false) }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = true
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            },
            state = timePickerState
        )
    }

    // Używamy Dialog + Card zamiast AlertDialog dla pełnej kontroli nad kolorami/ramką
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier
                .fillMaxWidth()
                // Neonowa ramka w HC
                .then(if (isHighContrast) Modifier.border(2.dp, primaryColor, RoundedCornerShape(24.dp)) else Modifier),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.calendar_add_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                // Pole Godziny (Read-only + Click)
                Box {
                    OutlinedTextField(
                        value = selectedTime.format(timeFormatter),
                        onValueChange = { },
                        label = { Text(stringResource(R.string.calendar_field_time), color = if(isHighContrast) primaryColor else Color.Gray) },
                        enabled = false, // Wyłączamy edycję tekstu, bo klikamy
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = inputContainer,
                            disabledTextColor = inputContent,
                            disabledBorderColor = if(isHighContrast) primaryColor else Color.Gray,
                            disabledLabelColor = if(isHighContrast) primaryColor else Color.Gray
                        )
                    )
                    // Niewidoczna warstwa klikalna na wierzchu
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showTimePicker = true }
                    )
                }

                // Pole Tytułu
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.calendar_field_name), color = if(isHighContrast) primaryColor else Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputContainer,
                        unfocusedContainerColor = inputContainer,
                        focusedTextColor = inputContent,
                        unfocusedTextColor = inputContent,
                        cursorColor = primaryColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = if (isHighContrast) primaryColor.copy(alpha = 0.5f) else Color.Gray
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Przyciski
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_cancel), color = if (isHighContrast) Color.White else Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(selectedTime, title)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary,
                            contentColor = if (isHighContrast) Color.Black else Color.White
                        )
                    ) {
                        Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- 4. DIALOG WYBORU GODZINY (TimePickerDialog) ---
/**
 * Komponent wyświetlający systemowy, ale oparty na Compose, selektor czasu (zegar) do wyboru godziny i minut.
 * Osadzony wewnątrz własnego Dialogu i Karty z pełną obsługą trybu wysokiego kontrastu.
 *
 * @param onDismiss Funkcja zamykająca selektor czasu.
 * @param onConfirm Funkcja potwierdzająca wybór godziny.
 * @param state Instancja [TimePickerState] przechowująca bieżący stan selektora (godziny i minuty).
 * @param title Tytuł okna selektora czasu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    state: TimePickerState,
    title: String = stringResource(R.string.calendar_time_picker_title)
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary
    val dialogBg = if (isHighContrast) Color.Black else Color.White
    val textColor = if (isHighContrast) primaryColor else Color.Black

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .padding(16.dp)
                // Neonowa ramka
                .then(if (isHighContrast) Modifier.border(2.dp, primaryColor, RoundedCornerShape(28.dp)) else Modifier),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(Modifier.height(24.dp))

                // Konfiguracja kolorów TimePickera dla High Contrast
                val timePickerColors = if (isHighContrast) {
                    TimePickerDefaults.colors(
                        clockDialColor = Color.DarkGray,
                        clockDialSelectedContentColor = Color.Black,
                        clockDialUnselectedContentColor = Color.White,
                        selectorColor = primaryColor,
                        periodSelectorBorderColor = primaryColor,
                        periodSelectorSelectedContainerColor = primaryColor,
                        periodSelectorSelectedContentColor = Color.Black,
                        periodSelectorUnselectedContentColor = Color.White,
                        timeSelectorSelectedContainerColor = primaryColor,
                        timeSelectorSelectedContentColor = Color.Black,
                        timeSelectorUnselectedContainerColor = Color.DarkGray,
                        timeSelectorUnselectedContentColor = Color.White
                    )
                } else {
                    TimePickerDefaults.colors()
                }

                TimePicker(state = state, colors = timePickerColors)

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.common_cancel), color = if(isHighContrast) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.common_confirm), fontWeight = FontWeight.Bold, color = textColor)
                    }
                }
            }
        }
    }
}
