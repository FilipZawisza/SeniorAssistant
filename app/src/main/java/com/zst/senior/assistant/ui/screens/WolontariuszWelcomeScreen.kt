package com.zst.senior.assistant.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.ui.theme.volunteerGradient
import com.zst.senior.assistant.utils.LocalTtsSpeaker
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.ProfileState
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Ekran główny (dashboard) dla użytkowników zalogowanych jako Wolontariusz.
 *
 * Pełni rolę centralnego punktu nawigacyjnego aplikacji dla osób pomagających.
 * Oferuje spersonalizowane powitanie (pobierane z profilu użytkownika) oraz
 * listę kafelków przekierowujących do najważniejszych funkcji, takich jak:
 * przeglądanie dostępnych zleceń, zarządzanie własnymi zadaniami, czat czy kalendarz.
 *
 * Ekran jest w pełni zintegrowany z systemem dostępności aplikacji:
 * 1. Obsługuje tryb wysokiego kontrastu (High Contrast), dynamicznie dostosowując kolory.
 * 2. Wykorzystuje system TTS (Text-to-Speech) do odczytywania nazw sekcji po ich kliknięciu.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia między ekranami.
 * @param authViewModel ViewModel zarządzający sesją użytkownika (wykorzystywany do wylogowania).
 * @param userProfileViewModel ViewModel zarządzający danymi profilowymi (wykorzystywany do pobrania imienia).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WolontariuszWelcomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    val tts = LocalTtsSpeaker.current

    // --- POBRANIE ZASOBÓW TEKSTOWYCH ---
    val defaultVolunteerName = stringResource(R.string.volunteer_default_name)
    val infoTtsMessage = stringResource(R.string.welcome_info_tts)
    val logoutTtsMessage = stringResource(R.string.welcome_logout_tts)

    // --- MOTYW I KOLORY ---
    val isThemeHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val primaryColor = MaterialTheme.colorScheme.primary
    val headerTextColor = if (isThemeHighContrast) primaryColor else Color.White

    // --- STAN DANYCH UŻYTKOWNIKA ---
    val profileState by userProfileViewModel.profileState.collectAsState()
    var userName by remember { mutableStateOf(defaultVolunteerName) }

    LaunchedEffect(Unit) {
        userProfileViewModel.loadProfile()
    }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val data = (profileState as ProfileState.Success).data
            userName = data["Imie"] as? String ?: defaultVolunteerName
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = stringResource(R.string.volunteer_greeting, userName),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = headerTextColor
                        )
                        Text(
                            text = stringResource(R.string.volunteer_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isThemeHighContrast) Color.White else Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 16.dp)) {
                        // PRZYCISK INFORMACJI
                        Surface(
                            color = if (isThemeHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = if (isThemeHighContrast) BorderStroke(1.dp, primaryColor) else null,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            IconButton(onClick = {
                                tts.speak(infoTtsMessage)
                                navController.safeNavigate(AppRoutes.ABOUT)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.welcome_about_desc),
                                    tint = headerTextColor
                                )
                            }
                        }

                        // PRZYCISK WYLOGOWANIA
                        Surface(
                            color = if (isThemeHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = if (isThemeHighContrast) BorderStroke(1.dp, primaryColor) else null
                        ) {
                            IconButton(onClick = {
                                tts.speak(logoutTtsMessage)
                                authViewModel.logout()
                                navController.safeNavigate(AppRoutes.LOGIN) {
                                    popUpTo(AppRoutes.WOLONTARIUSZ_WELCOME) { inclusive = true }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = stringResource(R.string.welcome_logout_desc),
                                    tint = headerTextColor
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isThemeHighContrast) Modifier.background(Color.Black)
                    else Modifier.background(volunteerGradient)
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                MenuCard(
                    title = stringResource(R.string.volunteer_available_orders_title),
                    description = stringResource(R.string.volunteer_available_orders_subtitle),
                    icon = Icons.AutoMirrored.Filled.PlaylistAddCheck,
                    baseIconColor = MaterialTheme.colorScheme.secondary,
                    onClick = { navController.safeNavigate(AppRoutes.HELPER_DASHBOARD) },
                    isHighContrast = isThemeHighContrast,
                    primaryColor = primaryColor
                )

                MenuCard(
                    title = stringResource(R.string.volunteer_my_orders_title),
                    description = stringResource(R.string.volunteer_my_orders_subtitle),
                    icon = Icons.Default.AssignmentInd,
                    baseIconColor = MaterialTheme.colorScheme.primary,
                    onClick = { navController.safeNavigate(AppRoutes.WOLONTARIUSZ_MOJE_ZLECENIA) },
                    isHighContrast = isThemeHighContrast,
                    primaryColor = primaryColor
                )

                MenuCard(
                    title = stringResource(R.string.welcome_chat_title),
                    description = stringResource(R.string.welcome_chat_subtitle),
                    icon = Icons.AutoMirrored.Filled.Chat,
                    baseIconColor = Color(0xFF2196F3),
                    onClick = { navController.navigate(AppRoutes.CHAT) },
                    isHighContrast = isThemeHighContrast,
                    primaryColor = primaryColor
                )

                MenuCard(
                    title = stringResource(R.string.volunteer_calendar_title),
                    description = stringResource(R.string.volunteer_calendar_subtitle),
                    icon = Icons.Default.CalendarMonth,
                    baseIconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = { navController.safeNavigate(AppRoutes.SENIOR_CALENDAR) },
                    isHighContrast = isThemeHighContrast,
                    primaryColor = primaryColor
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SmallMenuCard(
                        title = stringResource(R.string.welcome_profile_title),
                        icon = Icons.Default.Person,
                        onClick = { navController.safeNavigate(AppRoutes.PROFILE) },
                        modifier = Modifier.weight(1f),
                        isHighContrast = isThemeHighContrast,
                        primaryColor = primaryColor
                    )
                    SmallMenuCard(
                        title = stringResource(R.string.welcome_settings_title),
                        icon = Icons.Default.Settings,
                        onClick = { navController.safeNavigate(AppRoutes.SETTINGS) },
                        modifier = Modifier.weight(1f),
                        isHighContrast = isThemeHighContrast,
                        primaryColor = primaryColor
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Podstawowa karta nawigacyjna (o pełnej szerokości), stosowana dla kluczowych funkcjonalności.
 *
 * Zawiera dużą ikonę na okrągłym tle, wyróżniony tytuł, opis poboczny oraz strzałkę
 * zachęcającą do kliknięcia. Wspiera system Text-to-Speech odczytując tytuł po kliknięciu.
 *
 * @param title Główny tekst karty (np. "Moje zlecenia").
 * @param description Tekst pomocniczy wyjaśniający działanie funkcji.
 * @param icon Ikona wektorowa reprezentująca daną opcję.
 * @param baseIconColor Domyślny kolor ikony (nadpisywany przez [primaryColor] w trybie wysokiego kontrastu).
 * @param onClick Akcja wywoływana po kliknięciu karty (nawigacja).
 * @param isHighContrast Flaga określająca, czy włączony jest tryb wysokiego kontrastu.
 * @param primaryColor Główny kolor motywu używany do rysowania ramek i ikon w trybie HC.
 */
@Composable
private fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    baseIconColor: Color,
    onClick: () -> Unit,
    isHighContrast: Boolean,
    primaryColor: Color
) {
    val tts = LocalTtsSpeaker.current
    val containerColor = if (isHighContrast) Color.Black else Color.White
    val contentColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
    val finalIconColor = if (isHighContrast) primaryColor else baseIconColor
    val border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else null

    Card(
        onClick = {
            tts.speak(title)
            onClick()
        },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = finalIconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = finalIconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isHighContrast) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = if (isHighContrast) primaryColor else contentColor.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Kompaktowa karta nawigacyjna (w kształcie kwadratu), stosowana dla opcji dodatkowych.
 *
 * Używana głównie w układzie rzędowym (Row) dla oszczędności miejsca.
 * Zawiera centralnie wyśrodkowaną ikonę i tytuł.
 * Podobnie jak główna karta, obsługuje tryb HC i Text-to-Speech.
 *
 * @param title Etykieta przycisku (np. "Profil").
 * @param icon Ikona wektorowa umieszczona nad tekstem.
 * @param onClick Akcja wywoływana po kliknięciu.
 * @param modifier Pozwala na nałożenie dodatkowych parametrów z zewnątrz (np. `weight`).
 * @param isHighContrast Flaga określająca, czy włączony jest tryb wysokiego kontrastu.
 * @param primaryColor Główny kolor motywu używany w trybie HC.
 */
@Composable
private fun SmallMenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighContrast: Boolean,
    primaryColor: Color
) {
    val tts = LocalTtsSpeaker.current
    val containerColor = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.9f)
    val contentColor = if (isHighContrast) Color.White else MaterialTheme.colorScheme.onSurface
    val iconTint = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.primary
    val border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else null

    Card(
        onClick = {
            tts.speak(title)
            onClick()
        },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}