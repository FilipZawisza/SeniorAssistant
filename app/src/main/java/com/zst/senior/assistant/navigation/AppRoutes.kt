package com.zst.senior.assistant.navigation

/**
 * Centralny rejestr wszystkich tras (ścieżek) nawigacyjnych używanych w aplikacji.
 * Wykorzystywany w połączeniu z Jetpack Compose Navigation do zarządzania przejściami między ekranami.
 */
object AppRoutes {
    // ============================================================================================
    //                                     START APLIKACJI
    // ============================================================================================

    /** Ścieżka do ekranu powitalnego (Splash Screen), wyświetlanego podczas inicjalizacji aplikacji. */
    const val SPLASH = "splash"

    // ============================================================================================
    //                                     UWIERZYTELNIANIE
    // ============================================================================================

    /** Ścieżka do ekranów wprowadzających (Onboarding) dla nowych użytkowników. */
    const val ONBOARDING = "onboarding"

    /** Ścieżka do ekranu logowania użytkownika. */
    const val LOGIN = "login"

    /** Ścieżka do ekranu rejestracji nowego konta. */
    const val REGISTER = "register"

    /** Ścieżka do ekranu resetowania/odzyskiwania hasła. */
    const val FORGOT_PASSWORD = "forgot_password"

    // ============================================================================================
    //                                     HUBY STARTOWE
    // ============================================================================================

    /** Główny ekran powitalny po zalogowaniu dla konta typu Senior. */
    const val SENIOR_WELCOME = "senior_welcome"

    /** Główny ekran powitalny po zalogowaniu dla konta typu Wolontariusz. */
    const val WOLONTARIUSZ_WELCOME = "wolontariusz_welcome"

    /** Główny ekran powitalny po zalogowaniu dla konta typu Administrator. */
    const val ADMIN_WELCOME = "admin_welcome"

    // ============================================================================================
    //                                     MODUŁY SENIORA
    // ============================================================================================

    /** Ścieżka do panelu głównego (dashboard) Seniora, z szybkimi akcjami. */
    const val SENIOR_DASHBOARD = "senior_dashboard"

    /** Ścieżka do kalendarza zaplanowanych wizyt i wydarzeń Seniora. */
    const val SENIOR_CALENDAR = "senior_calendar"

    /** Ścieżka do listy zgłoszonych zleceń i próśb o pomoc Seniora. */
    const val SENIOR_MOJE_ZLECENIA = "senior_moje_zlecenia"

    /** Ścieżka do stałego harmonogramu Seniora (np. przyjmowanie leków). */
    const val SENIOR_HARMONOGRAM = "senior_harmonogram"

    /** Ścieżka do zarządzania konkretnymi pozycjami modułu Seniorm2 (przypomnienia cykliczne). */
    const val SENIORM2_ITEMS = "seniorm2_items"

    /** Ścieżka do inteligentnego asystenta (np. czatbota) dla Seniora. */
    const val ASSISTANT = "assistant"

    // ============================================================================================
    //                                     MODUŁY WOLONTARIUSZA
    // ============================================================================================

    /** Ścieżka do panelu głównego Wolontariusza, gdzie może przeglądać dostępne zlecenia. */
    const val HELPER_DASHBOARD = "helper_dashboard"

    /** Ścieżka do listy zleceń aktualnie przypisanych do Wolontariusza. */
    const val WOLONTARIUSZ_MOJE_ZLECENIA = "wolontariusz_moje_zlecenia"

    // ============================================================================================
    //                                     MODUŁY ADMINISTRATORA
    // ============================================================================================

    /** Ścieżka widoku dla Administratora ze wszystkimi aktywnymi zleceniami w systemie. */
    const val ADMIN_WSZYSTKIE_ZLECENIA = "admin_wszystkie_zlecenia"

    /** Ścieżka do archiwum zakończonych lub anulowanych zleceń. */
    const val ADMIN_ARCHIWUM = "admin_archiwum"

    /** Ścieżka do globalnych ustawień systemu z poziomu Administratora. */
    const val ADMIN_SETTINGS = "admin_settings"

    // ============================================================================================
    //                                     TRASY DYNAMICZNE (Z ARGUMENTAMI)
    // ============================================================================================

    // --- Szczegóły Zlecenia ---

    /** Bazowa nazwa trasy dla ekranu szczegółów zlecenia. */
    const val ZLECENIE_DETAILS_ROUTE = "zlecenie_details"

    /** Nazwa argumentu przekazującego unikalne ID zlecenia. */
    const val ZLECENIE_DETAILS_ARG = "zlecenieId"

    /** Pełna trasa ze zdefiniowanym formatem argumentu, używana do rejestracji w NavGraph: "zlecenie_details/{zlecenieId}". */
    const val ZLECENIE_DETAILS = "$ZLECENIE_DETAILS_ROUTE/{$ZLECENIE_DETAILS_ARG}"

    // --- Zarządzanie Użytkownikami (Admin) ---

    /** Bazowa nazwa trasy dla ekranu zarządzania użytkownikami. */
    const val ADMIN_MANAGE_USERS_ROUTE = "admin_manage_users"

    /** Nazwa argumentu przekazującego rolę użytkowników do wyśietlenia (np. "senior", "wolontariusz"). */
    const val ADMIN_MANAGE_USERS_ARG = "userRole"

    /** Pełna trasa ze zdefiniowanym formatem argumentu: "admin_manage_users/{userRole}". */
    const val ADMIN_MANAGE_USERS = "$ADMIN_MANAGE_USERS_ROUTE/{$ADMIN_MANAGE_USERS_ARG}"

    // ============================================================================================
    //                                     WSPÓLNE / GLOBALNE
    // ============================================================================================

    /** Ścieżka do ekranu alarmowego SOS (wywoływanego np. po wykryciu upadku). */
    const val SOS_EMERGENCY = "sos_emergency"

    /** Ścieżka do ustawień konta/aplikacji użytkownika. */
    const val SETTINGS = "settings"

    /** Ścieżka do widoku profilu użytkownika. */
    const val PROFILE = "profile"

    /** Ścieżka do wbudowanego czatu/komunikatora. */
    const val CHAT = "chat"

    /** Ścieżka do ekranu informacyjnego "O aplikacji". */
    const val ABOUT = "about"

    /** Ścieżka do dokumentu Polityki Prywatności (RODO). */
    const val PRIVACY_POLICY = "privacy_policy"

    /** Ścieżka do Regulaminu korzystania z usług aplikacji. */
    const val TERMS_OF_SERVICE = "terms_of_service"

    // ============================================================================================
    //                                     GRY I GRYWALIZACJA
    // ============================================================================================

    /** Ścieżka do gry/rozrywki intelektualnej "Seniordle". */
    const val SENIORDLE = "seniordle"

    /** Ścieżka do rankingu wyników/punktacji użytkowników. */
    const val LEADERBOARD = "leaderboard"
}