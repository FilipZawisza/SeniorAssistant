package com.zst.senior.assistant.navigation.graphs

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zst.senior.assistant.model.UserRole
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.ui.screens.AssistantScreen
import com.zst.senior.assistant.ui.screens.LeaderboardScreen
import com.zst.senior.assistant.ui.screens.SeniorCalendarScreen
import com.zst.senior.assistant.ui.screens.SeniorDashboardScreen
import com.zst.senior.assistant.ui.screens.SeniorHarmonogramScreen
import com.zst.senior.assistant.ui.screens.SeniorMojeZleceniaScreen
import com.zst.senior.assistant.ui.screens.SeniordleScreen
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.SeniorCalendarViewModel
import com.zst.senior.assistant.viewmodel.Seniorm2ViewModel
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Definicja grafu nawigacyjnego przeznaczonego dla roli Seniora.
 *
 * Graf ten udostępnia najważniejsze moduły aplikacji zorientowane na pomoc, organizację dnia
 * oraz rozrywkę dla osoby starszej, w tym: Dashboard (szybkie akcje), Kalendarz, Harmonogram,
 * Asystenta AI oraz Gry.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia między ekranami.
 * @param authViewModel ViewModel przechowujący rolę użytkownika (dla widoku kalendarza).
 * @param helpRequestViewModel ViewModel zarządzający zgłoszeniami pomocy seniora.
 * @param calendarViewModel ViewModel obsługujący wydarzenia w kalendarzu.
 * @param seniorm2ViewModel ViewModel obsługujący inteligentnego asystenta i harmonogram.
 * @param userProfileViewModel ViewModel zarządzający danymi osobowymi seniora.
 */
fun NavGraphBuilder.seniorGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    helpRequestViewModel: HelpRequestViewModel,
    calendarViewModel: SeniorCalendarViewModel,
    seniorm2ViewModel: Seniorm2ViewModel,
    userProfileViewModel: UserProfileViewModel,
    settingsViewModel: com.zst.senior.assistant.viewmodel.SettingsViewModel
) {
    // Panel główny seniora - przyciski szybkiego dostępu (WCAG-friendly)
    composable(AppRoutes.SENIOR_DASHBOARD) {
        SeniorDashboardScreen(navController, helpRequestViewModel, userProfileViewModel)
    }

    // Lista moich zleceń (zgłoszonych przez seniora)
    composable(AppRoutes.SENIOR_MOJE_ZLECENIA) {
        SeniorMojeZleceniaScreen(navController, helpRequestViewModel)
    }

    // Kalendarz wydarzeń - dostępny w widoku dla seniora
    composable(AppRoutes.SENIOR_CALENDAR) {
        val userRole by authViewModel.userRole.collectAsState()
        SeniorCalendarScreen(navController, calendarViewModel, userRole == UserRole.SENIOR.collectionName)
    }

    // Inteligentny asystent komunikacyjny
    composable(AppRoutes.ASSISTANT) {
        AssistantScreen(navController, calendarViewModel, seniorm2ViewModel, settingsViewModel)
    }

    // Stały harmonogram (np. przypomnienia o lekach)
    composable(AppRoutes.SENIOR_HARMONOGRAM) {
        SeniorHarmonogramScreen(navController, seniorm2ViewModel)
    }

    // Edukacyjna gra logiczna typu wordle
    composable(AppRoutes.SENIORDLE) {
        SeniordleScreen(navController)
    }

    // Ranking aktywności i grywalizacji
    composable(AppRoutes.LEADERBOARD) {
        LeaderboardScreen(navController)
    }
}
