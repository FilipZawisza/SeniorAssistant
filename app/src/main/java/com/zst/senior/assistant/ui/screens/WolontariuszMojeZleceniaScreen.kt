package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.PelneZlecenieDlaWolontariusza
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.ZlecenieItemWolontariusz
import com.zst.senior.assistant.ui.theme.volunteerGradient
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ZleceniaListUiState
import com.zst.senior.assistant.viewmodel.ZlecenieActionUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ekran wyświetlający listę zleceń (zadań) przypisanych do aktualnie zalogowanego wolontariusza.
 *
 * Ekran ten pozwala wolontariuszowi na przeglądanie zaakceptowanych przez niego próśb o pomoc
 * zgłoszonych przez seniorów oraz oznaczanie ich jako wykonane. Obsługuje różne stany interfejsu
 * (ładowanie, błąd, brak danych, sukces) i oferuje mechanizm ręcznego odświeżania listy poprzez
 * gest "Pull to Refresh" (przeciągnij, aby odświeżyć).
 *
 * Główne funkcjonalności:
 * * **Zarządzanie stanem:** Nasłuchiwanie na zmiany w liście zleceń oraz wynikach akcji (np. sukces/błąd po kliknięciu "zakończ").
 * * **UX/UI:** Obsługa powiadomień [Snackbar], animacje elementów listy oraz tryb wysokiego kontrastu.
 * * **Lokalny stan ładowania:** Blokowanie przycisku dla konkretnego zlecenia, gdy wysyłane jest żądanie do serwera.
 *
 * @param navController Kontroler nawigacji służący do bezpiecznego powrotu do poprzedniego ekranu.
 * @param helpRequestViewModel ViewModel zarządzający logiką pobierania zleceń oraz akcjami zmiany ich statusu.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WolontariuszMojeZleceniaScreen(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel
) {
    val listUiState by helpRequestViewModel.zleceniaListUiState.collectAsState()
    val actionUiState by helpRequestViewModel.actionUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Lokalny stan ładowania konkretnego elementu
    var loadingZlecenieId by remember { mutableStateOf<String?>(null) }

    // Stan odświeżania (Pull to Refresh)
    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()

    // --- 1. DETEKCJA TRYBU HC I KOLORY ---
    val isThemeHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val primaryColor = MaterialTheme.colorScheme.primary

    // Tło
    val backgroundModifier = Modifier
        .fillMaxSize()
        .then(
            if (isThemeHighContrast) Modifier.background(Color.Black)
            else Modifier.background(volunteerGradient)
        )

    val contentColor = if (isThemeHighContrast) primaryColor else Color.White
    val subTextColor = if (isThemeHighContrast) Color.White else Color.White.copy(alpha = 0.8f)

    // Funkcja odświeżania
    val onRefresh = {
        scope.launch {
            isRefreshing = true
            helpRequestViewModel.fetchMojeZleceniaDlaWolontariusza(context)
            delay(500) // Małe opóźnienie dla UX
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        helpRequestViewModel.fetchMojeZleceniaDlaWolontariusza(context)
    }

    // Obsługa akcji (np. sukcesu zakończenia zlecenia)
    LaunchedEffect(actionUiState) {
        when (val state = actionUiState) {
            is ZlecenieActionUiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                loadingZlecenieId = null
                helpRequestViewModel.resetActionState()
                helpRequestViewModel.fetchMojeZleceniaDlaWolontariusza(context)
            }
            is ZlecenieActionUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                loadingZlecenieId = null
                helpRequestViewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.welcome_my_orders_title),
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    actionIconContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    titleContentColor = contentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { onRefresh() }) {
                        Icon(Icons.Default.Refresh, stringResource(R.string.helper_dashboard_refresh))
                    }
                }
            )
        }
    ) { paddingValues ->

        // PULL TO REFRESH BOX
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = backgroundModifier.padding(paddingValues),
            state = pullRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    containerColor = if(isThemeHighContrast) Color.Black else Color.White,
                    color = if(isThemeHighContrast) primaryColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            when (val state = listUiState) {
                is ZleceniaListUiState.Loading -> {
                    if (!isRefreshing) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = contentColor)
                        }
                    }
                }
                is ZleceniaListUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.helper_dashboard_error_icon_desc), tint = contentColor, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.helper_dashboard_error_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.message,
                            color = subTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = { onRefresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isThemeHighContrast) primaryColor else Color.White,
                                contentColor = if (isThemeHighContrast) Color.Black else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(R.string.common_try_again))
                        }
                    }
                }
                is ZleceniaListUiState.Success -> {
                    val zlecenia = state.zlecenia.filterIsInstance<PelneZlecenieDlaWolontariusza>()

                    if (zlecenia.isEmpty()) {
                        // EMPTY STATE
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isThemeHighContrast) Color.Black else Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .size(100.dp)
                                    .then(if (isThemeHighContrast) Modifier.border(2.dp, primaryColor, CircleShape) else Modifier)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = stringResource(R.string.helper_dashboard_empty_icon_desc),
                                        tint = contentColor,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(
                                stringResource(R.string.helper_dashboard_empty_title),
                                color = contentColor,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.volunteer_my_orders_subtitle),
                                color = subTextColor,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else {
                        // LISTA ZLECEN
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = zlecenia,
                                key = { it.zlecenieId }
                            ) { zlecenie ->
                                Box(modifier = Modifier.animateItem()) {
                                    ZlecenieItemWolontariusz(
                                        zlecenie = zlecenie,
                                        isLoading = (loadingZlecenieId == zlecenie.zlecenieId),
                                        onZakoncz = {
                                            loadingZlecenieId = zlecenie.zlecenieId
                                            helpRequestViewModel.oznaczJakoWykonane(context, zlecenie.zlecenieId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}