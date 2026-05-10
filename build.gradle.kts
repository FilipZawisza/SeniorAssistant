// Główny plik budowania na poziomie projektu.
// Tutaj definiujemy wtyczki, które będą używane w modułach aplikacji.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false

    // Wtyczka do bezpiecznego zarządzania kluczami API (np. do Map Google lub Gemini)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1" apply false

    // DODANE: Wtyczka Firebase Crashlytics - odpowiada za przesyłanie raportów o błędach
    id("com.google.firebase.crashlytics") version "3.0.7" apply false
}