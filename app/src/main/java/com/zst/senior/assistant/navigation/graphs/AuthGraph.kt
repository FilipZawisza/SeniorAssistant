package com.zst.senior.assistant.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.ui.screens.AdminWelcomeScreen
import com.zst.senior.assistant.ui.screens.ForgotPasswordScreen
import com.zst.senior.assistant.ui.screens.LoginScreen
import com.zst.senior.assistant.ui.screens.OnboardingScreen
import com.zst.senior.assistant.ui.screens.RegisterScreen
import com.zst.senior.assistant.ui.screens.SeniorWelcomeScreen
import com.zst.senior.assistant.ui.screens.WolontariuszWelcomeScreen
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.SettingsViewModel
import com.zst.senior.assistant.viewmodel.UserProfileViewModel

/**
 * Definicja grafu nawigacyjnego obsługującego procesy uwierzytelniania i powitalne.
 *
 * Obejmuje ekrany onboardingu, logowania, rejestracji, odzyskiwania hasła
 * oraz główne ekrany powitalne (Welcome Screens) dla poszczególnych ról użytkowników.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia między ekranami.
 * @param authViewModel ViewModel zarządzający procesem logowania i rejestracji.
 * @param userProfileViewModel ViewModel dostarczający dane profilowe (używany m.in. na ekranie wolontariusza).
 * @param settingsViewModel ViewModel do zarządzania językiem podczas onboardingu.
 */
fun NavGraphBuilder.authGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Pierwsze uruchomienie - wprowadzenie do aplikacji
    composable(AppRoutes.ONBOARDING) { OnboardingScreen(navController, settingsViewModel) }

    // Logowanie do istniejącego konta
    composable(AppRoutes.LOGIN) { LoginScreen(navController, authViewModel) }

    // Rejestracja nowego użytkownika
    composable(AppRoutes.REGISTER) { RegisterScreen(navController, authViewModel) }

    // Procedura resetowania zapomnianego hasła
    composable(AppRoutes.FORGOT_PASSWORD) { ForgotPasswordScreen(navController, authViewModel) }

    // Ekran powitalny dedykowany dla Seniora
    composable(AppRoutes.SENIOR_WELCOME) { SeniorWelcomeScreen(navController, authViewModel) }

    // Ekran powitalny dedykowany dla Wolontariusza
    composable(AppRoutes.WOLONTARIUSZ_WELCOME) {
        WolontariuszWelcomeScreen(navController, authViewModel, userProfileViewModel)
    }

    // Ekran powitalny dedykowany dla Administratora
    composable(AppRoutes.ADMIN_WELCOME) { AdminWelcomeScreen(navController, authViewModel) }
}
