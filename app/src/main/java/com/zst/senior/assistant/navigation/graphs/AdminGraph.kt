package com.zst.senior.assistant.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.navigation.safeNavigate
import com.zst.senior.assistant.ui.screens.AdminArchiwumScreen
import com.zst.senior.assistant.ui.screens.AdminManageUsersScreen
import com.zst.senior.assistant.ui.screens.AdminWszystkieZleceniaScreen
import com.zst.senior.assistant.ui.screens.SettingsScreen
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.SettingsViewModel

/**
 * Definicja grafu nawigacyjnego przeznaczonego dla roli Administratora.
 *
 * Zawiera trasy do zarządzania zleceniami, archiwum, ustawień globalnych oraz
 * dynamicznego zarządzania użytkownikami w zależności od ich roli.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia między ekranami.
 * @param authViewModel ViewModel obsługujący autoryzację i zarządzanie użytkownikami.
 * @param helpRequestViewModel ViewModel zarządzający listami zleceń.
 * @param settingsViewModel ViewModel obsługujący globalne ustawienia aplikacji.
 */
fun NavGraphBuilder.adminGraph(
    navController: NavController,
    authViewModel: AuthViewModel,
    helpRequestViewModel: HelpRequestViewModel,
    settingsViewModel: SettingsViewModel
) {
    // Widok wszystkich aktywnych zleceń w systemie
    composable(AppRoutes.ADMIN_WSZYSTKIE_ZLECENIA) {
        AdminWszystkieZleceniaScreen(navController, helpRequestViewModel)
    }

    // Widok archiwalnych (zakończonych/anulowanych) zleceń
    composable(AppRoutes.ADMIN_ARCHIWUM) {
        AdminArchiwumScreen(navController, helpRequestViewModel)
    }

    // Ustawienia systemowe (dostępne dla admina)
    composable(AppRoutes.ADMIN_SETTINGS) {
        SettingsScreen(navController, settingsViewModel, authViewModel)
    }

    // Zarządzanie użytkownikami z przekazanym argumentem roli (np. "senior", "wolontariusz")
    composable(
        route = AppRoutes.ADMIN_MANAGE_USERS,
        arguments = listOf(navArgument(AppRoutes.ADMIN_MANAGE_USERS_ARG) { type = NavType.StringType })
    ) { backStackEntry ->
        val userRole = backStackEntry.arguments?.getString(AppRoutes.ADMIN_MANAGE_USERS_ARG)
        if (userRole != null) {
            AdminManageUsersScreen(navController, authViewModel, userRole)
        }
    }
}
