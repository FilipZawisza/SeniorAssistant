package com.zst.senior.assistant.navigation

import android.Manifest
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.zst.senior.assistant.model.UserRole // DODANO IMPORT ROLI
import com.zst.senior.assistant.navigation.graphs.adminGraph
import com.zst.senior.assistant.navigation.graphs.authGraph
import com.zst.senior.assistant.navigation.graphs.commonGraph
import com.zst.senior.assistant.navigation.graphs.helperGraph
import com.zst.senior.assistant.navigation.graphs.seniorGraph
import com.zst.senior.assistant.repository.FallDetectionState
import com.zst.senior.assistant.ui.components.FallCountdownDialog
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.FallDetectorViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.SeniorCalendarViewModel
import com.zst.senior.assistant.viewmodel.Seniorm2ViewModel
import com.zst.senior.assistant.viewmodel.SettingsViewModel
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Główny komponent zarządzający nawigacją w aplikacji za pomocą Jetpack Compose Navigation.
 *
 * Funkcja odpowiada za inicjalizację [NavHost], definicję grafów nawigacyjnych (Auth, Senior, Wolontariusz, Admin, Common)
 * oraz obsługę globalnych nakładek, takich jak system wykrywania upadków.
 * Dodatkowo realizuje funkcję automatycznego logowania (Session Persistence).
 *
 * @param calendarViewModel ViewModel obsługujący kalendarz seniora.
 * @param fallDetectorViewModel ViewModel monitorujący stan detektora upadków.
 * @param helpRequestViewModel ViewModel zarządzający zgłoszeniami pomocy (zlecenia).
 * @param seniorm2ViewModel ViewModel obsługujący harmonogram i asystenta AI.
 * @param settingsViewModel ViewModel przechowujący ustawienia aplikacji (WCAG, TTS).
 * @param authViewModel ViewModel zarządzający sesją i rolą użytkownika.
 * @param userProfileViewModel ViewModel obsługujący dane profilowe użytkownika.
 */
@Composable
fun AppNavigator(
    calendarViewModel: SeniorCalendarViewModel,
    fallDetectorViewModel: FallDetectorViewModel,
    helpRequestViewModel: HelpRequestViewModel,
    seniorm2ViewModel: Seniorm2ViewModel,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Pobieranie aktualnego stanu autoryzacji (zalogowany użytkownik i jego rola)
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val userRole by authViewModel.userRole.collectAsState()

    // Określenie punktu startowego na podstawie statusu onboardingu
    val startDestination = remember {
        val sharedPrefs = context.getSharedPreferences("OnboardingPrefs", Context.MODE_PRIVATE)
        if (sharedPrefs.getBoolean("hasSeenOnboarding", false)) AppRoutes.LOGIN else AppRoutes.ONBOARDING
    }

    // --- AUTOMATYCZNE WZNAWIANIE SESJI (AUTO-LOGIN) ---
    // Reagujemy na zmiany currentUserId i userRole. Jeśli użytkownik włącza aplikację
    // i ma aktywną sesję Firebase, system natychmiast przeniesie go na odpowiedni ekran główny.
    LaunchedEffect(currentUserId, userRole) {
        // Upewniamy się, że użytkownik jest zalogowany i pobrano jego rolę z bazy
        if (currentUserId != null && userRole != null) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route ?: startDestination

            // Przekierowujemy TYLKO jeśli jesteśmy na ekranie logowania,
            // aby nie przeszkadzać, gdy użytkownik jest już głębiej w aplikacji
            if (currentRoute == AppRoutes.LOGIN) {
                when (userRole) {
                    UserRole.SENIOR.collectionName -> {
                        navController.navigate(AppRoutes.SENIOR_WELCOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true } // Usuwamy Login ze stosu
                        }
                    }
                    UserRole.WOLONTARIUSZ.collectionName -> {
                        navController.navigate(AppRoutes.WOLONTARIUSZ_WELCOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    }
                    UserRole.ADMIN.collectionName -> {
                        navController.navigate(AppRoutes.ADMIN_WELCOME) {
                            popUpTo(AppRoutes.LOGIN) { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    // Prośba o uprawnienia do powiadomień na Androidzie 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> Log.d("AppNavigator", "Powiadomienia: $isGranted") }
        LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
    }

    // GŁÓWNY KONTENER APLIKACJI
    // UWAGA a11y: Nigdy nie dodajemy modifier = Modifier.verticalScroll() do tego Boxa!
    // Wszelkie zabezpieczenia przed ucinaniem dużego tekstu (fontSizeScale) muszą
    // znajdować się wewnątrz konkretnych ekranów (np. w Column wewnątrz LoginScreen).
    Box(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = startDestination) {
            authGraph(navController, authViewModel, userProfileViewModel, settingsViewModel)
            seniorGraph(navController, authViewModel, helpRequestViewModel, calendarViewModel, seniorm2ViewModel, userProfileViewModel, settingsViewModel)
            helperGraph(navController, helpRequestViewModel)
            adminGraph(navController, authViewModel, helpRequestViewModel, settingsViewModel)
            commonGraph(navController, settingsViewModel, authViewModel, userProfileViewModel, fallDetectorViewModel, helpRequestViewModel)
        }

        // Globalna nakładka obsługująca stany alarmowe detekcji upadku.
        // Wyświetlana nad wszystkimi innymi ekranami.
        FallDetectionOverlay(navController, fallDetectorViewModel)
    }
}

/**
 * Komponent nakładki monitorujący stan detekcji upadku.
 *
 * W przypadku przejścia w stan [FallDetectionState.Countdown], wyświetla dialog odliczania.
 * W przypadku przejścia w stan [FallDetectionState.Alarm], automatycznie nawiguje do ekranu SOS.
 *
 * @param navController Kontroler nawigacji do przekierowania na ekran SOS.
 * @param fallDetectorViewModel ViewModel dostarczający stan detekcji.
 */
@Composable
private fun FallDetectionOverlay(
    navController: NavController,
    fallDetectorViewModel: FallDetectorViewModel
) {
    val fallState by fallDetectorViewModel.fallDetectionState.collectAsState()

    // Reakcja na zmianę stanu na ALARM - automatyczne przejście do ekranu SOS
    LaunchedEffect(fallState) {
        if (fallState is FallDetectionState.Alarm) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != AppRoutes.SOS_EMERGENCY) {
                navController.safeNavigate(AppRoutes.SOS_EMERGENCY)
            }
        }
    }

    // Wyświetlenie dialogu odliczania przed wysłaniem alarmu
    if (fallState is FallDetectionState.Countdown) {
        val secondsLeft = (fallState as FallDetectionState.Countdown).secondsLeft
        FallCountdownDialog(
            secondsLeft = secondsLeft,
            onCancel = { fallDetectorViewModel.cancelCountdown() }
        )
    }
}