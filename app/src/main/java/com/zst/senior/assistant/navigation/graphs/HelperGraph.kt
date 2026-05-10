package com.zst.senior.assistant.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zst.senior.assistant.navigation.AppRoutes
import com.zst.senior.assistant.ui.screens.HelperDashboardScreen
import com.zst.senior.assistant.ui.screens.WolontariuszMojeZleceniaScreen
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel

/**
 * Definicja grafu nawigacyjnego przeznaczonego dla roli Wolontariusza.
 *
 * Zawiera trasy umożliwiające wolontariuszom przeglądanie wszystkich dostępnych
 * zleceń w systemie oraz zarządzanie zadaniami, które już zostały przez nich zaakceptowane.
 *
 * @param navController Kontroler nawigacji służący do przechodzenia między ekranami.
 * @param helpRequestViewModel ViewModel zarządzający listami i akcjami na zleceniach pomocy.
 */
fun NavGraphBuilder.helperGraph(
    navController: NavController,
    helpRequestViewModel: HelpRequestViewModel
) {
    // Panel główny wolontariusza - wyszukiwanie nowych próśb o pomoc
    composable(AppRoutes.HELPER_DASHBOARD) {
        HelperDashboardScreen(navController, helpRequestViewModel)
    }

    // Lista zadań aktualnie przypisanych do tego wolontariusza
    composable(AppRoutes.WOLONTARIUSZ_MOJE_ZLECENIA) {
        WolontariuszMojeZleceniaScreen(navController, helpRequestViewModel)
    }
}
