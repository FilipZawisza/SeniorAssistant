package com.zst.senior.assistant.ui.components

import android.location.Location
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zst.senior.assistant.R
import com.zst.senior.assistant.ui.theme.WcagYellow
import com.zst.senior.assistant.ui.theme.WcagNeonGreen
import com.zst.senior.assistant.utils.ConnectivityObserver
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ZlecenieActionUiState

/**
 * Komponent interfejsu (Dialog) umożliwiający Seniorowi szybkie utworzenie nowego zlecenia.
 * * Wyświetla okno z formularzem, w którym użytkownik może dodać szczegółowy opis do wybranej
 * wcześniej usługi. Dialog automatycznie dostosowuje swoją kolorystykę do trybu wysokiego
 * kontrastu (zgodnego z wytycznymi WCAG).
 * * Blokuje możliwość wysłania zgłoszenia w dwóch przypadkach:
 * 1. Trwa pobieranie aktualnej lokalizacji GPS.
 * 2. Trwa wysyłanie danych zlecenia do serwera/bazy danych.
 *
 * @param serviceTitleRes Identyfikator zasobu tytułu wybranej usługi.
 * @param onDismiss Funkcja zwrotna wywoływana w celu zamknięcia okna dialogowego (np. po anulowaniu lub sukcesie).
 * @param helpRequestViewModel ViewModel zarządzający logiką tworzenia zgłoszenia oraz stanem akcji.
 * @param seniorLocation Opcjonalny obiekt [Location] zawierający współrzędne GPS miejsca pobytu Seniora.
 * @param isLocationLoading Flaga informująca, czy aplikacja aktualnie ustala pozycję GPS. Jeśli `true`, wyświetlany jest wskaźnik ładowania i przyciski są zablokowane.
 */
@Composable
fun AddHelpRequestDialog(
    serviceTitleRes: Int,
    onDismiss: () -> Unit,
    helpRequestViewModel: HelpRequestViewModel,
    seniorLocation: Location? = null,
    isLocationLoading: Boolean = false
) {
    // Stan lokalny pola tekstowego
    var details by rememberSaveable { mutableStateOf("") }

    // Pobieramy Context do obsługi Toastów
    val context = LocalContext.current
    val serviceTitle = stringResource(serviceTitleRes)

    // --- OBSERWACJA INTERNETU ---
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

    // Nasłuchiwanie stanu akcji tworzenia zlecenia
    val actionState by helpRequestViewModel.actionUiState.collectAsState()
    val isLoadingAction = actionState is ZlecenieActionUiState.Loading

    // --- DETEKCJA TRYBU WYSOKIEGO KONTRASTU ---
    val isHighContrast = MaterialTheme.colorScheme.primary == WcagYellow

    // --- KOLORYSTYKA DIALOGU ---
    val containerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surface
    val contentColor = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.onSurface

    // Obsługa zdarzeń na podstawie zmiany stanu w ViewModelu
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ZlecenieActionUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                helpRequestViewModel.resetActionState()
                onDismiss()
            }
            is ZlecenieActionUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                helpRequestViewModel.resetActionState()
            }
            else -> {}
        }
    }

    AlertDialog(
        onDismissRequest = {
            // Zablokowanie zamykania dialogu kliknięciem w tło podczas ładowania
            if (!isLoadingAction && !isLocationLoading) onDismiss()
        },
        containerColor = containerColor,
        textContentColor = contentColor,
        titleContentColor = contentColor,
        shape = RoundedCornerShape(28.dp),

        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.comp_add_help_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = serviceTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isHighContrast) WcagYellow else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },

        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = stringResource(R.string.comp_add_help_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(stringResource(R.string.comp_add_help_field_label)) },
                    enabled = !isLoadingAction && !isLocationLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = if (isHighContrast) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedTextColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.onSurface,
                        cursorColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.primary,
                        focusedLabelColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedIndicatorColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = if (isHighContrast) WcagNeonGreen else MaterialTheme.colorScheme.outline,
                        disabledIndicatorColor = if (isHighContrast) WcagNeonGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    )
                )

                // Informacja o oczekiwaniu na moduł GPS
                if (isLocationLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = contentColor)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.comp_add_help_loc_loading), style = MaterialTheme.typography.bodySmall, color = contentColor)
                    }
                }

                // Informacja o braku internetu
                if (!isConnected) {
                    Text(
                        text = stringResource(R.string.comp_add_help_no_internet),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {
                    val pelnyOpis = "$serviceTitle: ${details.trim()}"
                    helpRequestViewModel.utworzZlecenie(context, pelnyOpis, seniorLocation)
                },
                // Przycisk aktywny tylko gdy wpisano szczegóły, system nie jest w stanie ładowania i jest internet
                enabled = details.isNotBlank() && !isLoadingAction && !isLocationLoading && isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = if (isHighContrast) Color.Black else Color.White
                )
            ) {
                if (isLoadingAction) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = if (isHighContrast) Color.Black else Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isLocationLoading) stringResource(R.string.common_waiting) else stringResource(R.string.common_send),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },

        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoadingAction && !isLocationLoading
            ) {
                Text(
                    text = stringResource(R.string.common_cancel),
                    color = if(isHighContrast) Color.White else Color.Gray,
                )
            }
        }
    )
}