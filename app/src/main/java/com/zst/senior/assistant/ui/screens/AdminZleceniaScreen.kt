package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.ZlecenieRef
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.ZlecenieItemPublic
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ZleceniaListUiState

// ==========================================
// 1. EKRAN WSZYSTKICH ZLECEŃ (Wrapper)
// ==========================================
/**
 * Ekran panelu administratora wyświetlający listę wszystkich aktywnych zleceń (próśb o pomoc) w systemie.
 *
 * Działa jako tzw. funkcja "Wrapper", która konfiguruje uniwersalny komponent [SharedZleceniaListScreen]
 * pod kątem pobierania i wyświetlania bieżących, niezakończonych zadań.
 *
 * @param navController Kontroler nawigacji używany do przechodzenia do szczegółów konkretnego zlecenia oraz cofania.
 * @param helpRequestViewModel ViewModel zarządzający logiką pobierania i przechowywania stanu zleceń.
 */
@Composable
fun AdminWszystkieZleceniaScreen(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel,
) {
    SharedZleceniaListScreen(
        navController = navController,
        viewModel = helpRequestViewModel,
        screenTitle = stringResource(R.string.admin_orders_title),
        emptyIcon = Icons.AutoMirrored.Filled.Assignment,
        emptyTitle = stringResource(R.string.admin_orders_empty_title),
        emptySubtitle = stringResource(R.string.admin_orders_empty_subtitle),
        onFetchData = { helpRequestViewModel.fetchAllZlecenia() }
    )
}

/**
 * Ekran panelu administratora prezentujący historię zakończonych (zarchiwizowane) zleceń.
 *
 * Podobnie jak ekran aktywnych zleceń, wykorzystuje [SharedZleceniaListScreen], wstrzykując
 * do niego logikę pobierania zleceń archiwalnych oraz odpowiednie zasoby wizualne (ikony, teksty).
 *
 * @param navController Kontroler nawigacji używany do przechodzenia do widoku szczegółów oraz cofania.
 * @param helpRequestViewModel ViewModel zarządzający logiką pobierania i przechowywania historii zleceń.
 */
@Composable
fun AdminArchiwumScreen(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel
) {
    SharedZleceniaListScreen(
        navController = navController,
        viewModel = helpRequestViewModel,
        screenTitle = stringResource(R.string.admin_archive_title),
        emptyIcon = Icons.Default.Archive,
        emptyTitle = stringResource(R.string.admin_archive_empty_title),
        emptySubtitle = stringResource(R.string.admin_archive_empty_subtitle),
        onFetchData = { helpRequestViewModel.fetchArchiwalneZlecenia() }
    )
}

/**
 * Współdzielony, uniwersalny komponent interfejsu (Core) służący do wyświetlania list zleceń.
 * Obsługuje stany ładowania (Loading), błędów (Error) oraz sukcesu (Success).
 *
 * Wyciąga architekturę odpowiedzialną za renderowanie listy do jednego miejsca, dzięki czemu
 * unikamy powielania kodu (DRY - Don't Repeat Yourself) między różnymi zakładkami administratora.
 * Dodatkowo implementuje pełną obsługę trybu wysokiego kontrastu (WCAG).
 *
 * Wywołuje dostarczoną lambdę [onFetchData] w bloku `LaunchedEffect(Unit)`, co gwarantuje
 * załadowanie odpowiednich danych natychmiast przy wejściu na dany ekran.
 *
 * @param navController Kontroler do obsługi cofania się do poprzedniego widoku i nawigacji do szczegółów zlecenia.
 * @param viewModel ViewModel dostarczający stan interfejsu [ZleceniaListUiState].
 * @param screenTitle Tytuł wyświetlany w górnym pasku aplikacji (TopAppBar).
 * @param emptyIcon Ikona wyświetlana, gdy lista pobranych zleceń jest pusta.
 * @param emptyTitle Nagłówek wyświetlany, gdy lista pobranych zleceń jest pusta.
 * @param emptySubtitle Tekst pomocniczy wyświetlany, gdy lista pobranych zleceń jest pusta.
 * @param onFetchData Funkcja (lambda) wywoływana w celu zainicjowania pobierania odpowiedniego zestawu danych.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SharedZleceniaListScreen(
    navController: NavController,
    viewModel: HelpRequestViewModel,
    screenTitle: String,
    emptyIcon: ImageVector,
    emptyTitle: String,
    emptySubtitle: String,
    onFetchData: () -> Unit
) {
    val uiState by viewModel.zleceniaListUiState.collectAsState()

    // Logika High Contrast
    val isHighContrast = (MaterialTheme.colorScheme.background == Color.Black) || (MaterialTheme.colorScheme.background == WcagBlack)
    val backgroundColor = if (isHighContrast) Color.Black else Color.White
    val contentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black

    val backgroundModifier = Modifier.fillMaxSize().background(backgroundColor)

    // Pobieranie danych przy wejściu na ekran
    LaunchedEffect(Unit) {
        onFetchData()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = screenTitle, color = contentColor, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = contentColor,
                    titleContentColor = contentColor,
                    actionIconContentColor = contentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.admin_orders_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ZleceniaListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        // TYLKO DODANY KOLOR: neonowy zielony (secondary) dla loadera w trybie HC
                        color = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }
                is ZleceniaListUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.helper_dashboard_error_icon_desc), tint = contentColor, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.helper_dashboard_error_title), color = contentColor, fontWeight = FontWeight.Bold)
                        Text(state.message, color = contentColor, textAlign = TextAlign.Center)
                    }
                }
                is ZleceniaListUiState.Success -> {
                    val zlecenia = state.zlecenia.filterIsInstance<ZlecenieRef>()
                    if (zlecenia.isEmpty()) {
                        EmptyStateAdmin(
                            icon = emptyIcon,
                            title = emptyTitle,
                            subtitle = emptySubtitle,
                            isHighContrast = isHighContrast
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 48.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(zlecenia) { zlecenie ->
                                ZlecenieItemPublic(
                                    zlecenie = zlecenie,
                                    isAdminView = true,
                                    onClick = {
                                        navController.safeNavigate("${AppRoutes.ZLECENIE_DETAILS_ROUTE}/${zlecenie.id}")
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

/**
 * Komponent wizualny wyświetlany, gdy dana kategoria zleceń (np. wszystkie aktywne lub archiwum) jest pusta.
 *
 * Prezentuje dużą ikonę zamkniętą w kółku, główny tytuł informacyjny oraz doprecyzowujący podtytuł.
 * Został zoptymalizowany pod kątem zgodności z zasadami WCAG (tryb High Contrast) - dostosowuje
 * barwę tła, ramek, ikon oraz tekstu w celu zachowania wysokiej czytelności.
 *
 * @param icon Ikona wektorowa (Material Design) dopasowana do kontekstu braku danych (np. puste pudło dla archiwum).
 * @param title Główny nagłówek informujący użytkownika o pustym stanie (np. "Brak zleceń").
 * @param subtitle Dodatkowy opis wyjaśniający sytuację.
 * @param isHighContrast Wskazuje, czy aplikacja aktualnie pracuje w trybie wysokiego kontrastu (true) czy w standardowym (false).
 */
@Composable
fun EmptyStateAdmin(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isHighContrast: Boolean
) {
    val circleBg = if (isHighContrast) Color.Black else Color.LightGray.copy(alpha = 0.2f)
    // Ramka w kolorze żółtym (primary)
    val circleBorder = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.LightGray)
    // Ikona w kolorze żółtym (primary)
    val iconColor = if (isHighContrast) MaterialTheme.colorScheme.primary else Color.Black

    // Tytuł i podtytuł białe w trybie HC
    val titleColor = if (isHighContrast) Color.White else Color.Black
    val subtitleColor = if (isHighContrast) Color.White else Color.Gray

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = circleBg,
            border = circleBorder,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = stringResource(R.string.helper_dashboard_empty_icon_desc),
                    tint = iconColor,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = titleColor
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = subtitleColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}