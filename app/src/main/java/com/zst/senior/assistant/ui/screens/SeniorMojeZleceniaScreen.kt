package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.PelneZlecenieDlaSeniora
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.ZlecenieItemSenior
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.ui.theme.SosRed
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ZleceniaListUiState
import com.zst.senior.assistant.viewmodel.ZlecenieActionUiState
import kotlinx.coroutines.launch

/**
 * Ekran "Moje zlecenia" przeznaczony dla seniora.
 *
 * Pozwala na przeglądanie historii oraz aktualnego statusu zgłoszonych próśb o pomoc (zleceń).
 * Użytkownik (senior) może zarządzać swoimi zgłoszeniami poprzez interakcję z listą.
 * Ekran zawiera system powiadomień Snackbar oraz obsługuje trzy główne akcje,
 * które dla bezpieczeństwa wymagają potwierdzenia w oknach dialogowych (Pop-up):
 * * * **Anulowanie zlecenia:** Możliwe, zanim zlecenie zostanie podjęte przez wolontariusza lub gdy senior zmieni zdanie.
 * * **Zgłoszenie problemu (Odrzucenie):** Używane w przypadku, gdy wolontariusz zadeklarował pomoc, ale jej nie zrealizował.
 * * **Ocena i potwierdzenie:** Pozwala seniorowi potwierdzić wykonanie zadania oraz wystawić ocenę (1-5 gwiazdek).
 *
 * Ekran automatycznie dostosowuje swój interfejs i paletę kolorów do trybu wysokiego kontrastu (High Contrast).
 *
 * @param navController Kontroler nawigacji używany do powrotu do poprzedniego ekranu.
 * @param helpRequestViewModel ViewModel zarządzający pobieraniem listy zleceń oraz akcjami (anulowanie, ocena).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorMojeZleceniaScreen(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel
) {
    val listUiState by helpRequestViewModel.zleceniaListUiState.collectAsState()
    val actionUiState by helpRequestViewModel.actionUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var loadingZlecenieId by remember { mutableStateOf<String?>(null) }

    // --- STANY OKIENEK POP-UP ---
    var zlecenieDoAnulowania by remember { mutableStateOf<String?>(null) }
    var zlecenieDoOceny by remember { mutableStateOf<String?>(null) }
    var zlecenieDoOdrzucenia by remember { mutableStateOf<String?>(null) }

    // System oceny w gwiazdkach (domyślnie 5)
    var wybranaOcena by remember { mutableIntStateOf(5) }

    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isHighContrast) Modifier.background(Color.Black) else Modifier.background(AccentGradient)
    )
    val contentColor = if (isHighContrast) primaryColor else Color.White

    // Pobranie danych przy starcie ekranu
    LaunchedEffect(Unit) { helpRequestViewModel.fetchMojeZleceniaDlaSeniora() }

    // Reagowanie na zmiany stanu akcji (np. udane usunięcie, błąd)
    LaunchedEffect(actionUiState) {
        when (val state = actionUiState) {
            is ZlecenieActionUiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                loadingZlecenieId = null
                helpRequestViewModel.resetActionState()
            }
            is ZlecenieActionUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                loadingZlecenieId = null
                helpRequestViewModel.resetActionState()
            }
            is ZlecenieActionUiState.Loading -> {}
            is ZlecenieActionUiState.Idle -> { loadingZlecenieId = null }
        }
    }

    // --- 1. OKIENKO ANULOWANIA ZLECENIA ---
    if (zlecenieDoAnulowania != null) {
        AlertDialog(
            onDismissRequest = { zlecenieDoAnulowania = null },
            title = { Text(stringResource(R.string.senior_orders_cancel_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.senior_orders_cancel_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    loadingZlecenieId = zlecenieDoAnulowania
                    helpRequestViewModel.anulujZlecenie(zlecenieDoAnulowania!!)
                    zlecenieDoAnulowania = null
                }) { Text(stringResource(R.string.senior_orders_cancel_dialog_confirm), color = SosRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { zlecenieDoAnulowania = null }) { Text(stringResource(R.string.senior_orders_cancel_dialog_dismiss)) }
            }
        )
    }

    // --- 2. OKIENKO ODRZUCENIA (WOLONTARIUSZ NIE WYKONAŁ) ---
    if (zlecenieDoOdrzucenia != null) {
        AlertDialog(
            onDismissRequest = { zlecenieDoOdrzucenia = null },
            title = { Text(stringResource(R.string.senior_orders_report_dialog_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.senior_orders_report_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    loadingZlecenieId = zlecenieDoOdrzucenia
                    helpRequestViewModel.odrzucWykonanieZlecenia(zlecenieDoOdrzucenia!!)
                    zlecenieDoOdrzucenia = null
                }) { Text(stringResource(R.string.senior_orders_report_dialog_confirm), color = SosRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { zlecenieDoOdrzucenia = null }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    // --- 3. OKIENKO OCENY GWIAZDKOWEJ ---
    if (zlecenieDoOceny != null) {
        AlertDialog(
            onDismissRequest = {
                zlecenieDoOceny = null
                wybranaOcena = 5
            },
            title = {
                Text(stringResource(R.string.senior_orders_rating_dialog_title), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.senior_orders_rating_dialog_subtitle), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Pasek gwiazdek
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= wybranaOcena) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = "Ocena $i",
                                tint = if (isHighContrast) primaryColor else Color(0xFFFFC107), // Złoty kolor
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { wybranaOcena = i }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    loadingZlecenieId = zlecenieDoOceny
                    helpRequestViewModel.potwierdzWykonanieZOcena(zlecenieDoOceny!!, wybranaOcena)
                    zlecenieDoOceny = null
                    wybranaOcena = 5
                }) { Text(stringResource(R.string.senior_orders_rating_dialog_confirm), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    zlecenieDoOceny = null
                    wybranaOcena = 5
                }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.senior_orders_title), color = contentColor, fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, actionIconContentColor = contentColor, navigationIconContentColor = contentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack()}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back)) }
                },
                actions = {
                    IconButton(onClick = { helpRequestViewModel.fetchMojeZleceniaDlaSeniora() }) { Icon(Icons.Default.Refresh, stringResource(R.string.helper_dashboard_refresh)) }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = backgroundModifier.padding(paddingValues)) {
            when (val state = listUiState) {
                is ZleceniaListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = contentColor)
                }
                is ZleceniaListUiState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.helper_dashboard_error_title), style = MaterialTheme.typography.titleMedium, color = contentColor, fontWeight = FontWeight.Bold)
                        Text(state.message, color = contentColor.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                    }
                }
                is ZleceniaListUiState.Success -> {
                    val zlecenia = state.zlecenia.filterIsInstance<PelneZlecenieDlaSeniora>()
                        .reversed() // Sortowanie: najnowsze na górze

                    if (zlecenia.isEmpty()) {
                        // --- STAN PUSTY (EMPTY STATE) ---
                        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(shape = CircleShape, color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.15f), modifier = Modifier.size(100.dp).then(if (isHighContrast) Modifier.border(3.dp, primaryColor, CircleShape) else Modifier)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = stringResource(R.string.helper_dashboard_empty_icon_desc), tint = contentColor, modifier = Modifier.size(48.dp)) }
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(stringResource(R.string.senior_orders_empty_title), color = contentColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // --- LISTA ZLECEŃ ---
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(zlecenia) { zlecenie ->
                                ZlecenieItemSenior(
                                    zlecenie = zlecenie,
                                    isLoading = (loadingZlecenieId == zlecenie.zlecenieId),
                                    onPotwierdz = { zlecenieDoOceny = zlecenie.zlecenieId }, // Otwiera okienko z oceną
                                    onOdrzuc = { zlecenieDoOdrzucenia = zlecenie.zlecenieId }, // Otwiera okienko odrzucenia
                                    onAnuluj = { zlecenieDoAnulowania = zlecenie.zlecenieId } // Otwiera okienko anulowania
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}