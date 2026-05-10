package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WavingHand
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.utils.LocalTtsSpeaker
import com.zst.senior.assistant.viewmodel.AuthViewModel

/**
 * Główny ekran powitalny (Dashboard) przeznaczony dla seniora.
 *
 * Służy jako centralny punkt nawigacyjny aplikacji, z którego użytkownik może przejść
 * do najważniejszych modułów: proszenia o pomoc, zarządzania zleceniami, inteligentnego asystenta,
 * gier treningowych, czatu, kalendarza oraz ustawień.
 *
 * Kluczowe funkcjonalności ekranu:
 * * **Wsparcie Text-To-Speech (TTS):** Każda akcja nawigacyjna (kliknięcie w kartę) jest
 * potwierdzana głosowo, co ułatwia obsługę osobom niedowidzącym.
 * * **Wysoki Kontrast (WCAG):** Automatyczne dostosowanie kolorów, tła (czarne) i wyraźnych
 * obramowań, jeśli aplikacja działa w trybie wysokiego kontrastu.
 * * **Zarządzanie sesją:** Dostęp do przycisku wylogowania oraz informacji o aplikacji
 * bezpośrednio z górnego paska narzędzi ([TopAppBar]).
 *
 * @param navController Kontroler nawigacji używany do przełączania się między ekranami.
 * @param authViewModel ViewModel odpowiedzialny za uwierzytelnianie (np. wylogowywanie użytkownika).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeniorWelcomeScreen(navController: NavController, authViewModel: AuthViewModel) {
    // --- POBIERAMY GŁOŚNIK ---
    val tts = LocalTtsSpeaker.current

    // --- WCZEŚNIEJSZE POBRANIE TEKSTÓW TTS ---
    val infoTts = stringResource(R.string.welcome_info_tts)
    val logoutTts = stringResource(R.string.welcome_logout_tts)

    // --- 1. DETEKCJA TRYBU HC (NEON) ---
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = stringResource(R.string.welcome_greeting),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = contentColor
                        )
                        Text(
                            text = stringResource(R.string.welcome_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isHighContrast) Color.White else Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 16.dp)) {
                        // PRZYCISK INFORMACJI (O APLIKACJI)
                        Surface(
                            color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = if (isHighContrast) BorderStroke(1.dp, primaryColor) else null,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            IconButton(onClick = {
                                tts.speak(infoTts)
                                navController.safeNavigate(AppRoutes.ABOUT)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.welcome_about_desc),
                                    tint = contentColor
                                )
                            }
                        }

                        // PRZYCISK WYLOGOWANIA
                        Surface(
                            color = if (isHighContrast) Color.Black else Color.White.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = if (isHighContrast) BorderStroke(1.dp, primaryColor) else null
                        ) {
                            IconButton(onClick = {
                                tts.speak(logoutTts)
                                authViewModel.logout()
                                navController.safeNavigate(AppRoutes.LOGIN) {
                                    popUpTo(AppRoutes.SENIOR_WELCOME) { inclusive = true }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = stringResource(R.string.welcome_logout_desc),
                                    tint = contentColor
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
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. GŁÓWNA AKCJA (POMOC) ---
                MenuCard(
                    title = stringResource(R.string.welcome_help_title),
                    subtitle = stringResource(R.string.welcome_help_subtitle),
                    icon = Icons.Default.WavingHand,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = { navController.safeNavigate(AppRoutes.SENIOR_DASHBOARD) }
                )

                Spacer(Modifier.height(16.dp))

                // --- 2. ZLECENIA I ORGANIZER ---
                MenuCard(
                    title = stringResource(R.string.welcome_my_orders_title),
                    subtitle = stringResource(R.string.welcome_my_orders_subtitle),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { navController.safeNavigate(AppRoutes.SENIOR_MOJE_ZLECENIA) }
                )

                Spacer(Modifier.height(16.dp))

                // --- 3. AI ASYSTENT (Wyróżniony) ---
                MenuCard(
                    title = stringResource(R.string.welcome_ai_assistant_title),
                    subtitle = stringResource(R.string.welcome_ai_assistant_subtitle),
                    icon = Icons.Default.AutoAwesome,
                    accentColor = Color(0xFF9C27B0), // Fioletowy
                    onClick = { navController.safeNavigate(AppRoutes.ASSISTANT) }
                )

                Spacer(Modifier.height(16.dp))

                // --- 4. TRENING UMYSŁU (GRA SENIORDLE) ---
                MenuCard(
                    title = stringResource(R.string.welcome_brain_training_title),
                    subtitle = stringResource(R.string.welcome_brain_training_subtitle),
                    icon = Icons.Default.Extension,
                    accentColor = Color(0xFF4CAF50),
                    onClick = { navController.safeNavigate("seniordle") }
                )

                Spacer(Modifier.height(16.dp))

                // --- 5. CZAT ---
                MenuCard(
                    title = stringResource(R.string.welcome_chat_title),
                    subtitle = stringResource(R.string.welcome_chat_subtitle),
                    icon = Icons.AutoMirrored.Filled.Chat,
                    accentColor = Color(0xFF2196F3),
                    onClick = { navController.safeNavigate(AppRoutes.CHAT) }
                )

                Spacer(Modifier.height(16.dp))

                // --- 6. DODATKOWE AKCJE (Z ZADANIAMI CYKLICZNYMI) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmallMenuCard(
                        title = stringResource(R.string.welcome_reminders_title),
                        icon = Icons.Default.Notifications,
                        onClick = { navController.safeNavigate(AppRoutes.SENIOR_HARMONOGRAM) },
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                    SmallMenuCard(
                        title = stringResource(R.string.welcome_calendar_title),
                        icon = Icons.Default.CalendarMonth,
                        onClick = { navController.safeNavigate(AppRoutes.SENIOR_CALENDAR) },
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmallMenuCard(
                        title = stringResource(R.string.welcome_profile_title),
                        icon = Icons.Default.Person,
                        onClick = { navController.safeNavigate(AppRoutes.PROFILE) },
                        modifier = Modifier.weight(1f)
                    )
                    SmallMenuCard(
                        title = stringResource(R.string.welcome_settings_title),
                        icon = Icons.Default.Settings,
                        onClick = { navController.safeNavigate(AppRoutes.SETTINGS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Pozioma, szeroka karta menu (Primary Action) używana do kluczowych funkcji.
 *
 * Komponent zajmuje pełną szerokość ekranu, zawiera tytuł, podtytuł, dużą ikonę akcentującą
 * oraz ikonę strzałki wskazującą na przejście dalej. Karta automatycznie wspiera tryb
 * wysokiego kontrastu oraz odczytuje swój tytuł za pomocą TTS po kliknięciu.
 *
 * @param title Główny tekst wyświetlany na karcie (odczytywany przez TTS).
 * @param subtitle Dodatkowy opis funkcji ukryty pod tytułem.
 * @param icon Ikona wektorowa reprezentująca dany moduł.
 * @param accentColor Kolor akcentujący używany do tła i zabarwienia ikony.
 * @param onClick Funkcja wywoływana po kliknięciu karty (po odtworzeniu TTS).
 */
@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color(0xFF000000)
    val primaryColor = MaterialTheme.colorScheme.primary
    val tts = LocalTtsSpeaker.current
    val cardShape = RoundedCornerShape(24.dp)

    Card(
        onClick = {
            tts.speak(title)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else Color.White
        ),
        border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isHighContrast) Color.Black else accentColor.copy(alpha = 0.15f))
                    .then(if (isHighContrast) Modifier.border(1.dp, accentColor, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighContrast) primaryColor else Color.Black
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isHighContrast) Color.White else Color.Gray
                )
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                modifier = Modifier.size(16.dp),
                tint = if (isHighContrast) primaryColor else Color.LightGray
            )
        }
    }
}

/**
 * Kompaktowa, kwadratowa karta menu (Secondary Action) przeznaczona do użycia w siatce (wierszach).
 *
 * Wykorzystywana do mniej nadrzędnych funkcji aplikacji (np. Profil, Ustawienia, Kalendarz).
 * Prezentuje ikonę nad tytułem (bez podtytułu). Automatycznie wspiera tryb wysokiego
 * kontrastu oraz odczyt głosowy tytułu przy kliknięciu.
 *
 * @param title Etykieta karty, wyśrodkowana na dole (odczytywana przez TTS).
 * @param icon Ikona wektorowa reprezentująca dany moduł.
 * @param onClick Funkcja wywoływana po kliknięciu.
 * @param modifier Pozwala na modyfikację elementu wywołującego (np. przypisanie wag `weight`).
 * @param accentColor Opcjonalny kolor ikony; w przypadku braku, używa [primaryColor].
 */
@Composable
private fun SmallMenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val primaryColor = MaterialTheme.colorScheme.primary

    val containerColor = if (isHighContrast) Color.Black else Color.White
    val textColor = if (isHighContrast) primaryColor else MaterialTheme.colorScheme.onSurface
    val iconTint = accentColor ?: primaryColor
    val border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else null

    val tts = LocalTtsSpeaker.current

    Card(
        onClick = {
            tts.speak(title)
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 4.dp),
        modifier = modifier.height(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isHighContrast) Color.Black else iconTint.copy(alpha = 0.15f))
                    .then(if (isHighContrast) Modifier.border(1.dp, iconTint, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}