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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.zst.senior.assistant.model.UserRole
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.ui.theme.BrandBlue
import com.zst.senior.assistant.ui.theme.BrandOrange
import com.zst.senior.assistant.ui.theme.SuccessGreen
import com.zst.senior.assistant.utils.LocalTtsSpeaker
import com.zst.senior.assistant.viewmodel.AuthViewModel

/**
 * Główny ekran panelu administratora (Dashboard).
 *
 * Ekran wyświetla powitanie z imieniem zalogowanego użytkownika oraz interfejs kafelkowy (karty akcji),
 * który umożliwia szybką nawigację do kluczowych modułów zarządzania systemem, takich jak:
 * - Zarządzanie użytkownikami (Seniorzy i Wolontariusze)
 * - Przegląd i zarządzanie zleceniami
 * - Komunikator (Czat ogólny)
 * - Archiwum zakończonych zleceń
 * - Ustawienia i profil użytkownika
 *
 * Komponent jest w pełni zoptymalizowany pod kątem dostępności:
 * - Reaguje na globalny motyw i obsługuje tryb wysokiego kontrastu (WCAG).
 * - Wykorzystuje [LocalTtsSpeaker] do syntetyzowania mowy (TTS) i odczytywania nazw
 * sekcji przed ich otwarciem lub podczas akcji, takich jak wylogowanie.
 *
 * @param navController Kontroler nawigacji Jetpack Compose używany do przejść między ekranami.
 * Nawigacja realizowana jest poprzez bezpieczne rozszerzenie `safeNavigate`.
 * @param authViewModel ViewModel autoryzacji obsługujący stan zalogowanego użytkownika (np. pobieranie imienia)
 * oraz logikę procesu wylogowywania z aplikacji.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminWelcomeScreen(navController: NavController, authViewModel: AuthViewModel) {

    val tts = LocalTtsSpeaker.current
    val currentUserName by authViewModel.currentUserName.collectAsState()
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val primaryColor = MaterialTheme.colorScheme.primary

    val backgroundColor = if (isHighContrast) Color.Black else Color.White
    val contentColor = if (isHighContrast) primaryColor else Color.Black

    // Wcześniejsze pobranie stringów dla akcji TTS (odciąża lambdy onClick)
    val infoTtsText = stringResource(R.string.welcome_info_tts)
    val logoutTtsText = stringResource(R.string.welcome_logout_tts)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = if (!currentUserName.isNullOrBlank()) stringResource(R.string.admin_welcome_greeting, currentUserName!!) else stringResource(R.string.admin_welcome_title),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = contentColor
                        )
                        Text(
                            text = stringResource(R.string.admin_welcome_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.7f),
                        )
                    }
                },
                actions = {
                    Row(modifier = Modifier.padding(end = 16.dp)) {
                        // PRZYCISK INFORMACJI
                        Surface(
                            color = if (isHighContrast) Color.Black else Color.LightGray.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = if (isHighContrast) BorderStroke(1.dp, contentColor) else null,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    tts.speak(infoTtsText)
                                    navController.safeNavigate(AppRoutes.ABOUT)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = stringResource(R.string.welcome_about_desc),
                                    tint = contentColor
                                )
                            }
                        }

                        // PRZYCISK WYLOGOWANIA
                        Surface(
                            color = if (isHighContrast) Color.Black else Color.LightGray.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = if (isHighContrast) BorderStroke(1.dp, contentColor) else null
                        ) {
                            IconButton(
                                onClick = {
                                    tts.speak(logoutTtsText)
                                    authViewModel.logout()
                                    navController.safeNavigate(AppRoutes.LOGIN) {
                                        popUpTo(AppRoutes.ADMIN_WELCOME) { inclusive = true }
                                    }
                                }
                            ) {
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
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AdminActionCard(
                    title = stringResource(R.string.admin_welcome_seniors),
                    subtitle = stringResource(R.string.admin_welcome_seniors_sub),
                    icon = Icons.Default.Person,
                    accentColor = BrandOrange,
                    onClick = {
                        navController.safeNavigate("${AppRoutes.ADMIN_MANAGE_USERS_ROUTE}/${UserRole.SENIOR.collectionName}")
                    }
                )

                AdminActionCard(
                    title = stringResource(R.string.admin_welcome_volunteers),
                    subtitle = stringResource(R.string.admin_welcome_volunteers_sub),
                    icon = Icons.Default.VolunteerActivism,
                    accentColor = BrandBlue,
                    onClick = {
                        navController.safeNavigate("${AppRoutes.ADMIN_MANAGE_USERS_ROUTE}/${UserRole.WOLONTARIUSZ.collectionName}")
                    }
                )

                AdminActionCard(
                    title = stringResource(R.string.admin_welcome_orders),
                    subtitle = stringResource(R.string.admin_welcome_orders_sub),
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    accentColor = SuccessGreen,
                    onClick = {
                        navController.safeNavigate(AppRoutes.ADMIN_WSZYSTKIE_ZLECENIA)
                    }
                )

                AdminActionCard(
                    title = stringResource(R.string.admin_welcome_chat),
                    subtitle = stringResource(R.string.admin_welcome_chat_sub),
                    icon = Icons.AutoMirrored.Filled.Chat,
                    accentColor = Color(0xFF2196F3),
                    onClick = {
                        navController.safeNavigate(AppRoutes.CHAT)
                    }
                )

                AdminActionCard(
                    title = stringResource(R.string.admin_welcome_archive),
                    subtitle = stringResource(R.string.admin_welcome_archive_sub),
                    icon = Icons.Default.Inventory,
                    accentColor = Color(0xFF8E24AA),
                    onClick = {
                        navController.safeNavigate(AppRoutes.ADMIN_ARCHIWUM)
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmallAdminCard(
                        title = stringResource(R.string.welcome_profile_title),
                        icon = Icons.Default.Person,
                        onClick = {
                            navController.safeNavigate(AppRoutes.PROFILE)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    SmallAdminCard(
                        title = stringResource(R.string.welcome_settings_title),
                        icon = Icons.Default.Settings,
                        onClick = {
                            navController.safeNavigate(AppRoutes.ADMIN_SETTINGS)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Pozioma, pełno-szerokościowa karta akcji używana jako główny element nawigacyjny
 * w panelu administratora.
 *
 * Posiada zaokrąglone rogi i składa się z wyróżniającej się ikony po lewej stronie,
 * tytułu oraz podtytułu opisującego akcję, a także z wskaźnika nawigacji (strzałki) na końcu.
 * Integruje odczyt głosowy nazwy sekcji przy każdym kliknięciu.
 *
 * @param title Główny nagłówek karty (np. "Seniorzy").
 * @param subtitle Tekst poboczny opisujący szczegóły sekcji.
 * @param icon Ikona wektorowa (Material Design) reprezentująca dany moduł.
 * @param accentColor Kolor akcentu używany jako podkład i odcień ikony (w standardowym motywie).
 * @param onClick Funkcja wywoływana przy wciśnięciu karty.
 */
@Composable
fun AdminActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val primaryColor = MaterialTheme.colorScheme.primary
    val tts = LocalTtsSpeaker.current
    val cardShape = RoundedCornerShape(24.dp)
    val border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))

    Card(
        onClick = {
            tts.speak(title)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = cardShape,
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighContrast) 0.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighContrast) Color.Black else Color.White
        )
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
 * Mniejszy, kwadratowy wariant karty akcji, przeznaczony do układania w rzędzie
 * obok innych podobnych elementów.
 *
 * Służy zwykle do reprezentowania narzędzi pobocznych, takich jak "Ustawienia"
 * czy "Profil". Odczytuje swoją funkcję przez TTS po kliknięciu.
 *
 * @param title Tytuł karty wyświetlany pod ikoną.
 * @param icon Ikona reprezentująca opcję ustawień/profilu.
 * @param onClick Akcja wywoływana po naciśnięciu karty.
 * @param modifier Pozwala modyfikować wymiary lub wagi elementu (np. `Modifier.weight(1f)`).
 * @param accentColor Kolor opcjonalny dla ikony. Domyślnie dopasowuje się do motywu.
 */
@Composable
private fun SmallAdminCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    val isHighContrast = MaterialTheme.colorScheme.background == Color.Black
    val primaryColor = MaterialTheme.colorScheme.primary

    val containerColor = if (isHighContrast) Color.Black else Color.White
    val textColor = if (isHighContrast) primaryColor else Color.Black
    val iconTint = accentColor ?: (if (isHighContrast) primaryColor else Color(0xFF6200EE)) // Domyślny fiolet dla ikon na białym tle
    val border = if (isHighContrast) BorderStroke(2.dp, primaryColor) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))

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
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}