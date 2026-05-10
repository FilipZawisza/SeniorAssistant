package com.zst.senior.assistant.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.AddEventDialog
import com.zst.senior.assistant.ui.components.EventItem
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.ui.theme.volunteerGradient
import com.zst.senior.assistant.viewmodel.SeniorCalendarViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Kompozywalny ekran kalendarza, zaprojektowany z myślą o użytkownikach (w tym seniorach
 * oraz wolontariuszach), by umożliwić zarządzanie i podgląd codziennych wydarzeń.
 *
 * Oferuje on m.in. zintegrowany widget kalendarza materialnego ([DatePicker]),
 * listę wydarzeń (tzw. "wydarzeń" z przypisaną datą i godziną) przewidzianych na
 * wybrany przez użytkownika dzień, oraz możliwość dodania/usuwania wybranych zapisów.
 * Wygląd interfejsu (tło oraz paleta) adaptuje się w zależności od tego, czy użytkownik jest
 * seniorem oraz czy wybrany jest tryb o wysokim kontraście (HC).
 *
 * Komunikacja i dane zarządzane są za pośrednictwem obiektu `SeniorCalendarViewModel`.
 *
 * @param navController Kontroler nawigacji służący do bezpiecznego powrotu (`popBackStack`).
 * @param calendarViewModel Obiekt ViewModel odpowiadający za logikę odczytu, zapisu i przechowywania
 * wydarzeń dla aktualnie zalogowanego użytkownika.
 * @param isSenior Flaga informująca, czy dany interfejs renderowany jest z perspektywy Seniora.
 * Zmienia to główne tło, aby pasowało do odpowiednich wytycznych projektowych.
 */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorCalendarScreen(
    navController: NavController,
    calendarViewModel: SeniorCalendarViewModel,
    isSenior: Boolean,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showAddEventDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val events by calendarViewModel.events.collectAsState()
    val isLoading by calendarViewModel.isLoading.collectAsState()
    val error by calendarViewModel.error.collectAsState()

    // --- Wcześniejsze pobranie stringów do wyświetlenia w Snackbarze ---
    val msgDeleted = stringResource(R.string.calendar_msg_deleted)
    val msgAdded = stringResource(R.string.calendar_msg_added)

    // --- 1. DETEKCJA TRYBU HC (NEON) ---
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary // Neon Yellow w HC

    // --- 2. TŁO I KOLORY ---
    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isHighContrast) {
            Modifier.background(Color.Black)
        } else {
            if (isSenior) {
                Modifier.background(AccentGradient)
            } else {
                Modifier.background(volunteerGradient)
            }
        }
    )

    val contentColor = if (isHighContrast) primaryColor else Color.White

    val selectedDate = datePickerState.selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    } ?: LocalDate.now()

    val eventsForSelectedDate = events[selectedDate] ?: emptyList()

    LaunchedEffect(Unit) {
        calendarViewModel.loadEvents()
    }

    LaunchedEffect(error) {
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            calendarViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.calendar_title),
                        color = contentColor,
                        fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = contentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.calendar_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = if (isHighContrast) Color.Black else Color.White,
                contentColor = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = if (isHighContrast) Modifier.border(2.dp, primaryColor, CircleShape) else Modifier
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.calendar_add_event_desc))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. WIDŻET KALENDARZA ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .then(
                            if (isHighContrast) Modifier.border(2.dp, primaryColor, RoundedCornerShape(24.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHighContrast) Color.Black else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(Modifier.padding(bottom = 8.dp)) {

                        val datePickerColors = if (isHighContrast) {
                            DatePickerDefaults.colors(
                                containerColor = Color.Black,
                                titleContentColor = primaryColor,
                                headlineContentColor = primaryColor,
                                weekdayContentColor = primaryColor,
                                subheadContentColor = primaryColor,
                                yearContentColor = primaryColor,
                                currentYearContentColor = primaryColor,
                                selectedYearContentColor = Color.Black,
                                selectedYearContainerColor = primaryColor,
                                dayContentColor = Color.White,
                                disabledDayContentColor = Color.DarkGray,
                                todayDateBorderColor = primaryColor,
                                todayContentColor = primaryColor,
                                selectedDayContainerColor = primaryColor,
                                selectedDayContentColor = Color.Black,
                                navigationContentColor = primaryColor
                            )
                        } else {
                            DatePickerDefaults.colors(
                                containerColor = Color.White,
                                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                                selectedDayContentColor = Color.White,
                                todayContentColor = MaterialTheme.colorScheme.primary,
                                dayContentColor = MaterialTheme.colorScheme.onSurface,
                                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DatePicker(
                            state = datePickerState,
                            title = null,
                            headline = null,
                            showModeToggle = false,
                            colors = datePickerColors
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- 2. NAGŁÓWEK ---
                Text(
                    text = stringResource(R.string.calendar_events_for_date, selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(12.dp))

                // --- 3. LISTA ---
                if (isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = contentColor)
                    }
                } else if (eventsForSelectedDate.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 32.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.15f),
                                border = if (isHighContrast) BorderStroke(3.dp, primaryColor) else null,
                                modifier = Modifier.size(80.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.EventBusy,
                                        contentDescription = stringResource(R.string.calendar_empty_icon_desc),
                                        tint = contentColor,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.calendar_no_events),
                                color = contentColor,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(eventsForSelectedDate) { event ->
                            EventItem(
                                event = event,
                                onDelete = {
                                    calendarViewModel.deleteEvent(event)
                                    scope.launch { snackbarHostState.showSnackbar(msgDeleted) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOG (POPRAWIONY) ---
        if (showAddEventDialog) {
            AddEventDialog(
                onDismiss = { showAddEventDialog = false },
                onSave = { time, title ->
                    // Konwersja LocalTime -> String (Naprawia błąd TypeMismatch)
                    val timeString = time.format(DateTimeFormatter.ofPattern("HH:mm"))

                    calendarViewModel.addEvent(selectedDate, timeString, title)

                    showAddEventDialog = false
                    scope.launch { snackbarHostState.showSnackbar(msgAdded) }
                }
            )
        }
    }
}