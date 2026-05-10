package com.zst.senior.assistant.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.ui.screens.AboutScreen
import com.zst.senior.assistant.ui.screens.ChatScreen
import com.zst.senior.assistant.ui.screens.PrivacyPolicyScreen
import com.zst.senior.assistant.ui.screens.ProfileScreen
import com.zst.senior.assistant.ui.screens.SOSEmergencyScreen
import com.zst.senior.assistant.ui.screens.SettingsScreen
import com.zst.senior.assistant.ui.screens.TermsOfServiceScreen
import com.zst.senior.assistant.ui.screens.ZlecenieDetailsScreen
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.FallDetectorViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.SettingsViewModel
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Definicja grafu nawigacyjnego wspólnego dla wszystkich ról użytkowników (Senior, Wolontariusz, Admin).
 *
 * Zawiera uniwersalne ekrany takie jak: Ustawienia, Profil, SOS, Szczegóły Zlecenia, Czat,
 * Informacje o aplikacji oraz polityki prawne.
 *
 * @param navController Kontroler nawigacji do zarządzania przejściami.
 * @param settingsViewModel ViewModel zarządzający globalnymi preferencjami i motywami.
 * @param authViewModel ViewModel przechowujący stan logowania i role.
 * @param userProfileViewModel ViewModel zarządzający danymi osobowymi użytkownika.
 * @param fallDetectorViewModel ViewModel detektora upadków (dla ekranu SOS).
 * @param helpRequestViewModel ViewModel zarządzający operacjami na zleceniach.
 */
fun NavGraphBuilder.commonGraph(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    fallDetectorViewModel: FallDetectorViewModel,
    helpRequestViewModel: HelpRequestViewModel
) {
    // Ekran preferencji i ustawień (m.in. HC, TTS)
    composable(AppRoutes.SETTINGS) {
        SettingsScreen(navController, settingsViewModel, authViewModel)
    }

    // Edycja i podgląd profilu użytkownika
    composable(AppRoutes.PROFILE) {
        ProfileScreen(navController, userProfileViewModel)
    }

    // Ekran alarmowy wezwania pomocy
    composable(AppRoutes.SOS_EMERGENCY) {
        SOSEmergencyScreen(navController, fallDetectorViewModel, userProfileViewModel)
    }

    // Dynamiczny widok szczegółów konkretnego zlecenia
    composable(
        route = AppRoutes.ZLECENIE_DETAILS,
        arguments = listOf(navArgument(AppRoutes.ZLECENIE_DETAILS_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val zlecenieId = backStackEntry.arguments?.getString(AppRoutes.ZLECENIE_DETAILS_ARG)
        if (zlecenieId != null) {
            ZlecenieDetailsScreen(zlecenieId, navController, helpRequestViewModel, authViewModel)
        }
    }

    // System komunikacji wewnątrz aplikacji
    composable(AppRoutes.CHAT) {
        ChatScreen(navController, authViewModel)
    }

    // Informacje o projekcie
    composable(AppRoutes.ABOUT) {
        AboutScreen(navController)
    }

    // Dokument Polityki Prywatności (RODO)
    composable(AppRoutes.PRIVACY_POLICY) {
        PrivacyPolicyScreen(navController)
    }

    // Dokument Regulaminu Świadczenia Usług
    composable(AppRoutes.TERMS_OF_SERVICE) {
        TermsOfServiceScreen(navController)
    }
}
