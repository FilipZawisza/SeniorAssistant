package com.zst.senior.assistant.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.model.Seniorm2Item
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.AddSeniorHarmonogramDialog
import com.zst.senior.assistant.ui.components.Seniorm2DisplayItem
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.viewmodel.Seniorm2ViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Ekran harmonogramu (przypomnień) dedykowany dla seniorów.
 *
 * Umożliwia przeglądanie, dodawanie, edycję oraz usuwanie cyklicznych zadań,
 * takich jak zażywanie leków, wizyty lekarskie czy regularne opłaty. Ekran
 * dynamicznie dostosowuje swój interfejs użytkownika w zależności od wybranego
 * motywu (np. tryb wysokiego kontrastu wspierający standardy WCAG).
 *
 * Funkcje ekranu:
 * * **Wyświetlanie listy:** Prezentuje aktywne przypomnienia pobrane z [Seniorm2ViewModel].
 * * **Dodawanie/Edycja:** Wywołuje okno dialogowe [AddSeniorHarmonogramDialog] pozwalające
 * zdefiniować nazwę, godzinę oraz powtarzalność (dni tygodnia) zadania.
 * * **Stany interfejsu:** Obsługuje stany ładowania (spinner) oraz puste listy ("Twój harmonogram jest pusty").
 * * **Wysoki Kontrast:** Detekcja koloru tła (`WcagBlack`) automatycznie przełącza widok na kafelki
 * o wzmocnionych konturach i czarnym tle.
 *
 * @param navController Kontroler nawigacji Jetpack Compose używany do powrotu do poprzedniego ekranu.
 * @param seniorm2ViewModel Instancja ViewModelu zarządzająca logiką biznesową i stanem przypomnień.
 */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorHarmonogramScreen(
    navController: NavController,
    seniorm2ViewModel: Seniorm2ViewModel
) {
    var showDialogForItem by rememberSaveable { mutableStateOf<Seniorm2Item?>(null) }

    val seniorm2Items by seniorm2ViewModel.seniorm2Items.collectAsState()
    val isLoading by seniorm2ViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Wcześniejsze pobranie stringów do wyświetlenia w Snackbarze ---
    val msgDeleted = stringResource(R.string.reminders_msg_deleted)
    val msgUpdated = stringResource(R.string.reminders_msg_updated)
    val msgAdded = stringResource(R.string.reminders_msg_added)

    val newItemTemplate = Seniorm2Item(
        name = "",
        time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
        daysOfWeek = emptySet()
    )

    // --- 1. DETEKCJA TRYBU HC ---
    val isHighContrast = MaterialTheme.colorScheme.background == WcagBlack
    val primaryColor = MaterialTheme.colorScheme.primary

    // --- 2. TŁO I KOLORY ---
    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isHighContrast) {
            Modifier.background(Color.Black)
        } else {
            Modifier.background(AccentGradient)
        }
    )

    val contentColor = if (isHighContrast) primaryColor else Color.White

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.reminders_title),
                        color = contentColor,
                        fontWeight = if (isHighContrast) FontWeight.Black else FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = contentColor
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.reminders_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialogForItem = newItemTemplate },
                containerColor = if (isHighContrast) Color.Black else Color.White,
                contentColor = primaryColor,
                shape = CircleShape,
                modifier = if (isHighContrast) Modifier.border(2.dp, primaryColor, CircleShape) else Modifier
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.reminders_add_desc))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = contentColor)
                }
            } else if (seniorm2Items.isEmpty()) {
                // --- EMPTY STATE ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.15f),
                        border = if (isHighContrast) BorderStroke(3.dp, primaryColor) else null,
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EventRepeat,
                                contentDescription = stringResource(R.string.reminders_empty_icon_desc),
                                tint = contentColor,
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.reminders_empty_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = stringResource(R.string.reminders_empty_subtitle),
                        color = if (isHighContrast) contentColor else Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                // --- LISTA ---
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(seniorm2Items) { item ->
                        if (isHighContrast) {
                            HighContrastSeniorm2Tile(
                                item = item,
                                primaryColor = primaryColor,
                                onClick = { showDialogForItem = item },
                                onDelete = {
                                    seniorm2ViewModel.deleteSeniorm2(item)
                                    scope.launch { snackbarHostState.showSnackbar(msgDeleted) }
                                }
                            )
                        } else {
                            Seniorm2DisplayItem(
                                item = item,
                                onClick = { showDialogForItem = item },
                                onDelete = {
                                    seniorm2ViewModel.deleteSeniorm2(item)
                                    scope.launch { snackbarHostState.showSnackbar(msgDeleted) }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- DIALOG ---
        showDialogForItem?.let {
            AddSeniorHarmonogramDialog(
                itemToEdit = it,
                onDismiss = { showDialogForItem = null },
                onSave = { item ->
                    val isEditing = seniorm2Items.any { i -> i.id == item.id }
                    if (isEditing) {
                        seniorm2ViewModel.updateSeniorm2(item)
                        scope.launch { snackbarHostState.showSnackbar(msgUpdated) }
                    } else {
                        seniorm2ViewModel.addSeniorm2(item.name, item.time, item.daysOfWeek)
                        scope.launch { snackbarHostState.showSnackbar(msgAdded) }
                    }
                    showDialogForItem = null
                }
            )
        }
    }
}

/**
 * Specjalny kafelek dla trybu High Contrast (Czarne tło, silne kontury).
 * * Zapewnia maksymalną czytelność dla użytkowników z wadami wzroku. Komponent składa się z
 * wyraźnego obramowania, silnie kontrastujących kolorów tekstu i dedykowanego przycisku usuwania.
 *
 * @param item Obiekt modelu reprezentujący pojedyncze przypomnienie (nazwa, czas, dni).
 * @param primaryColor Kolor wiodący używany do obramowania oraz głównych tekstów w trybie HC.
 * @param onClick Funkcja wywoływana po kliknięciu w kafelek (np. w celu edycji).
 * @param onDelete Funkcja wywoływana po kliknięciu w ikonę kosza.
 */
@Composable
fun HighContrastSeniorm2Tile(
    item: Seniorm2Item,
    primaryColor: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(2.dp, primaryColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.EventRepeat,
                    contentDescription = stringResource(R.string.reminders_item_icon_desc),
                    tint = primaryColor,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.size(16.dp))

                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = primaryColor
                    )
                    Text(
                        text = stringResource(R.string.reminders_time_format, item.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (item.daysOfWeek.isNotEmpty()) {
                        Text(
                            text = item.daysOfWeek.joinToString(", ") { it.name.take(3) },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.reminders_delete_desc),
                    tint = Color.Red,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}