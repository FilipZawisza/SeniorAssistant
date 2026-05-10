package com.zst.senior.assistant.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.components.GradientButton
import com.zst.senior.assistant.ui.theme.SosRed
import com.zst.senior.assistant.ui.theme.WcagBlack
import com.zst.senior.assistant.ui.theme.volunteerGradient
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.ZlecenieActionUiState
import com.zst.senior.assistant.viewmodel.ZlecenieDetailsUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * Ekran szczegółowy konkretnego zlecenia pomocy.
 *
 * Wyświetla kompletne informacje o prośbie o pomoc, w tym: opis, status, dane seniora,
 * lokalizację (GPS i profilową) oraz numer telefonu. Interfejs dynamicznie dostosowuje
 * dostępne akcje w zależności od roli użytkownika (Senior/Wolontariusz/Admin) oraz bieżącego stanu zlecenia.
 *
 * **Logika akcji:**
 * * **Wolontariusz:** Może podjąć wolne zlecenie.
 * * **Senior (Właściciel):** Może anulować zlecenie, potwierdzić jego wykonanie lub zgłosić brak stawiennictwa wolontariusza.
 * * **Admin:** Posiada wgląd w dane bez możliwości bezpośredniej modyfikacji z tego poziomu (chyba że rozszerzono).
 *
 * **Wsparcie Dostępności:**
 * Ekran automatycznie wykrywa tryb wysokiego kontrastu (High Contrast) i modyfikuje paletę barw
 * na czarno-żółtą/neonową, aby zapewnić czytelność zgodnie z wytycznymi WCAG.
 *
 * @param zlecenieId Unikalny identyfikator zlecenia do pobrania z bazy danych.
 * @param navController Kontroler nawigacji do obsługi powrotów i przejść.
 * @param helpRequestViewModel ViewModel zarządzający stanem szczegółów zlecenia i akcjami API.
 * @param authViewModel ViewModel przechowujący informacje o roli i ID zalogowanego użytkownika.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZlecenieDetailsScreen(
    zlecenieId: String,
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by helpRequestViewModel.zlecenieDetailsUiState.collectAsState()
    val actionState by helpRequestViewModel.actionUiState.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val context = LocalContext.current

    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black || MaterialTheme.colorScheme.background == WcagBlack

    val isAdmin = userRole?.lowercase() == "admin"
    val isSenior = userRole?.lowercase() == "senior"

    // --- LOGIKA KOLORÓW ---
    val backgroundColor = if (isHighContrast) Color.Black else if (isAdmin) Color.White else Color.Transparent
    val backgroundModifier = if (isHighContrast) {
        Modifier.background(Color.Black)
    } else if (isAdmin) {
        Modifier.background(Color.White)
    } else {
        Modifier.background(volunteerGradient)
    }

    val headerContentColor = if (isHighContrast) MaterialTheme.colorScheme.primary else if (isAdmin) Color.Black else Color.White
    val cardContainerColor = if (isHighContrast) Color.Black else Color.White
    val cardBorder = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else if (isAdmin) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null

    val labelColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
    val iconColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val dividerColor = if (isHighContrast) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)

    LaunchedEffect(key1 = zlecenieId) {
        helpRequestViewModel.pobierzPelneDaneZlecenia(context, zlecenieId)
    }

    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is ZlecenieActionUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                helpRequestViewModel.resetActionState()
                helpRequestViewModel.fetchPublicZlecenia()

                if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                    navController.popBackStack()
                }
            }
            is ZlecenieActionUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                helpRequestViewModel.resetActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = headerContentColor
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                            navController.safePopBackStack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is ZlecenieDetailsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = if (isHighContrast) MaterialTheme.colorScheme.secondary else if (isAdmin) MaterialTheme.colorScheme.primary else headerContentColor
                    )
                }
                is ZlecenieDetailsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.common_error),
                            tint = headerContentColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.helper_dashboard_error_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = headerContentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.message,
                            color = if (isHighContrast || isAdmin) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                is ZlecenieDetailsUiState.Success -> {
                    val zlecenie = state.pelneZlecenie
                    val isActionLoading = actionState is ZlecenieActionUiState.Loading

                    val isWolne = zlecenie.status == "Wolne"
                    val isAktywne = zlecenie.status == "Aktywne"
                    val isDoPotwierdzenia = zlecenie.status == "DoPotwierdzenia"

                    val isMyZlecenie = isSenior && zlecenie.seniorId == currentUserId

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isHighContrast) Color.Black else if (isAdmin) Color.LightGray.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
                            border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else if (isAdmin) BorderStroke(1.dp, Color.LightGray) else null,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.welcome_info_tts),
                                    tint = if (isHighContrast) MaterialTheme.colorScheme.primary else if (isAdmin) Color.Black else headerContentColor,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.order_details_title),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = headerContentColor
                        )

                        Spacer(Modifier.height(24.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = cardContainerColor),
                            elevation = CardDefaults.cardElevation(if (isHighContrast) 0.dp else 8.dp),
                            border = cardBorder
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        stringResource(R.string.order_details_status_label),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = labelColor
                                    )

                                    val statusColor = when (zlecenie.status) {
                                        "Wolne" -> MaterialTheme.colorScheme.primary
                                        "Aktywne" -> MaterialTheme.colorScheme.tertiary
                                        "DoPotwierdzenia" -> MaterialTheme.colorScheme.secondary
                                        "Anulowane", "Niezrealizowane" -> SosRed
                                        else -> MaterialTheme.colorScheme.error
                                    }

                                    val (badgeBg, badgeTxt, badgeBorder) = if (isHighContrast) {
                                        Triple(Color.Black, MaterialTheme.colorScheme.primary, BorderStroke(2.dp, MaterialTheme.colorScheme.primary))
                                    } else {
                                        Triple(statusColor.copy(alpha = 0.15f), statusColor, BorderStroke(1.dp, statusColor))
                                    }

                                    Surface(
                                        color = badgeBg,
                                        shape = RoundedCornerShape(50),
                                        border = badgeBorder
                                    ) {
                                        Text(
                                            text = zlecenie.status.uppercase(),
                                            color = badgeTxt,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = dividerColor)

                                if (zlecenie.timestamp > 0) {
                                    val date = Date(zlecenie.timestamp)
                                    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", LocalLocale.current.platformLocale).apply {
                                        timeZone = TimeZone.getDefault()
                                    }
                                    DetailRow(
                                        icon = Icons.Default.CalendarToday,
                                        label = stringResource(R.string.order_details_date_label),
                                        value = format.format(date),
                                        iconColor = iconColor,
                                        labelColor = labelColor,
                                        valueColor = valueColor
                                    )
                                    HorizontalDivider(color = dividerColor)
                                }

                                DetailRow(
                                    icon = Icons.Default.Description,
                                    label = stringResource(R.string.order_details_desc_label),
                                    value = zlecenie.opisZlecenia,
                                    iconColor = iconColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor
                                )

                                HorizontalDivider(color = dividerColor)

                                DetailRow(
                                    icon = Icons.Default.Person,
                                    label = stringResource(R.string.order_details_senior_label),
                                    value = zlecenie.seniorImieNazwisko,
                                    iconColor = iconColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor,
                                    isBold = true
                                )

                                HorizontalDivider(color = dividerColor)

                                if (zlecenie.adresAktualny != null) {
                                    DetailRow(
                                        icon = Icons.Default.LocationOn,
                                        label = stringResource(R.string.order_details_loc_gps_label),
                                        value = zlecenie.adresAktualny,
                                        iconColor = iconColor,
                                        labelColor = labelColor,
                                        valueColor = valueColor
                                    )
                                    HorizontalDivider(color = dividerColor)
                                }

                                DetailRow(
                                    icon = Icons.Default.Home,
                                    label = stringResource(R.string.order_details_loc_profile_label),
                                    value = zlecenie.adresZProfilu,
                                    iconColor = iconColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor
                                )

                                HorizontalDivider(color = dividerColor)

                                DetailRow(
                                    icon = Icons.Default.Phone,
                                    label = stringResource(R.string.order_details_phone_label),
                                    value = zlecenie.seniorTelefon,
                                    iconColor = iconColor,
                                    labelColor = labelColor,
                                    valueColor = valueColor
                                )

                                Spacer(Modifier.height(16.dp))

                                // Sekcja akcji sterowana rolami
                                if (!isSenior && !isAdmin && isWolne) {
                                    if (isHighContrast) {
                                        OutlinedButton(
                                            onClick = { if (!isActionLoading) helpRequestViewModel.podejmijZlecenie(zlecenie.zlecenieId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                                            enabled = !isActionLoading
                                        ) {
                                            Text(if (isActionLoading) stringResource(R.string.order_details_accepting) else stringResource(R.string.order_details_accept_button), fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        GradientButton(
                                            text = if (isActionLoading) stringResource(R.string.order_details_accepting) else stringResource(R.string.order_details_accept_button),
                                            onClick = {
                                                if (!isActionLoading) helpRequestViewModel.podejmijZlecenie(zlecenie.zlecenieId)
                                            },
                                            enabled = !isActionLoading,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                                if (isMyZlecenie) {
                                    if (isDoPotwierdzenia) {
                                        if (isHighContrast) {
                                            OutlinedButton(
                                                onClick = { if (!isActionLoading) helpRequestViewModel.potwierdzWykonanie(zlecenie.zlecenieId) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                                                enabled = !isActionLoading
                                            ) {
                                                Text(stringResource(R.string.order_details_confirm_button), fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            GradientButton(
                                                text = stringResource(R.string.order_details_confirm_button),
                                                onClick = { if (!isActionLoading) helpRequestViewModel.potwierdzWykonanie(zlecenie.zlecenieId) },
                                                enabled = !isActionLoading,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }

                                    if (isWolne || isAktywne) {
                                        Button(
                                            onClick = { if (!isActionLoading) helpRequestViewModel.anulujZlecenie(zlecenie.zlecenieId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isHighContrast) Color.Black else SosRed,
                                                contentColor = if (isHighContrast) SosRed else Color.White
                                            ),
                                            border = if (isHighContrast) BorderStroke(1.dp, SosRed) else null,
                                            enabled = !isActionLoading
                                        ) {
                                            Text(stringResource(R.string.order_details_cancel_button), fontWeight = if (isHighContrast) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }

                                    if (isAktywne) {
                                        OutlinedButton(
                                            onClick = { if (!isActionLoading) helpRequestViewModel.zglosBrakWolontariusza(zlecenie.zlecenieId) },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !isActionLoading,
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = if (isHighContrast) Color.Black else Color.Transparent
                                            ),
                                            border = BorderStroke(if (isHighContrast) 2.dp else 1.dp, SosRed)
                                        ) {
                                            Text(stringResource(R.string.order_details_report_no_show), color = SosRed, textAlign = TextAlign.Center, fontWeight = if (isHighContrast) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }

                        if (!isWolne && !isAdmin && !isMyZlecenie) {
                            Spacer(Modifier.height(32.dp))
                            Surface(
                                color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isHighContrast) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                            ) {
                                Text(
                                    text = stringResource(R.string.order_details_unavailable),
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isHighContrast) Color.White else Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

/**
 * Pomocniczy komponent reprezentujący pojedynczy wiersz z danymi w karcie szczegółów.
 *
 * Składa się z ikony, etykiety (label) oraz wartości tekstowej. Zapewnia spójne wyrównanie
 * i formatowanie tekstu dla całego ekranu.
 *
 * @param icon Ikona wektorowa opisująca rodzaj informacji.
 * @param label Tekst nagłówka pola (np. "Telefon").
 * @param value Właściwa treść informacji.
 * @param iconColor Kolor ikony.
 * @param labelColor Kolor tekstu nagłówka.
 * @param valueColor Kolor tekstu wartości.
 * @param isBold Określa, czy tekst wartości ma być pogrubiony (domyślnie false).
 */
@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    labelColor: Color,
    valueColor: Color,
    isBold: Boolean = false
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                color = valueColor
            )
        }
    }
}