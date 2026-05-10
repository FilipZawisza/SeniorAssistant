// Import dla Properties niezbędny do odczytywania ukrytych kluczy z local.properties
@file:Suppress("DEPRECATION")

import java.util.Properties
import java.io.FileInputStream

/**
 * Zestaw wtyczek (plugins) wykorzystywanych w projekcie.
 * Definiują one główne możliwości procesu budowania, takie jak wsparcie dla Androida,
 * języka Kotlin, interfejsu Compose oraz integrację z usługami Google (Firebase).
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services) // Wtyczka wymagana do działania Firebase

    /**
     * DODANE: Wtyczka Firebase Crashlytics.
     * Umożliwia automatyczne przesyłanie raportów o błędach (crashach) do konsoli Firebase
     * oraz mapowanie zaciemnionego kodu (obfuscated code) na czytelne logi.
     */
    id("com.google.firebase.crashlytics")

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") // Bezpieczne wstrzykiwanie kluczy API
}

/**
 * Główny blok konfiguracji środowiska Android.
 */
android {
    namespace = "com.zst.senior.assistant"
    compileSdk = 37 // Najnowsza wersja SDK używana do kompilacji kodu

    defaultConfig {
        applicationId = "com.zst.senior.assistant"
        minSdk = 26 // Aplikacja wymaga minimum Androida 8.0 (Oreo) - dobre dla nowszych API (np. LocalTime)
        //noinspection OldTargetApi
        targetSdk = 35 // Wersja docelowa, pod którą aplikacja była optymalizowana
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- ZARZĄDZANIE KLUCZAMI API (BEZPIECZEŃSTWO) ---
        /**
         * Wczytywanie klucza Gemini z local.properties, aby uniknąć hardcodowania go w kodzie.
         */
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        } else {
            logger.warn("Plik local.properties nie znaleziony. Klucz API nie zostanie wczytany.")
        }

        val apiKey = localProperties.getProperty("GEMINI_API_KEY") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")

        // --- OBSŁUGA ARCHITEKTUR CPU ---
        /**
         * Ograniczenie kompilowanych bibliotek natywnych tylko do najpopularniejszych architektur ARM.
         */
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    /**
     * Konfiguracja typów budowania (np. debug, release).
     */
    buildTypes {
        release {
            isMinifyEnabled = true // Włączenie zaciemniania kodu (R8)
            isShrinkResources = true // Usuwanie nieużywanych zasobów
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    /**
     * Kompatybilność z wersją języka Java.
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    /**
     * Włączenie dodatkowych funkcji procesu budowania (Build Features).
     */
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

/**
 * Konfiguracja kompilatora Kotlina.
 */
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

/**
 * Zależności (biblioteki zewnętrzne) wykorzystywane w projekcie.
 */
dependencies {
    // --- Główne biblioteki AndroidX i Jetpack Compose ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.security.crypto)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.animation.core.lint)
    implementation(libs.androidx.material.icons.extended)

    // --- Firebase (Baza danych, Autoryzacja i Diagnostyka) ---
    /**
     * Wykorzystanie Firebase BOM pozwala na automatyczne dopasowanie wersji bibliotek Firebase,
     * aby były ze sobą w pełni kompatybilne.
     */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)      // Uwierzytelnianie
    implementation(libs.firebase.firestore) // Baza danych NoSQL
    implementation(libs.firebase.sessions)  // Zarządzanie sesjami

    /**
     * DODANE: Biblioteki diagnostyczne Firebase.
     * Crashlytics odpowiada za raportowanie błędów, a Analytics dostarcza kontekst (np. co użytkownik
     * robił przed błędem), co jest kluczowe w debugowaniu aplikacji dla seniorów.
     */
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // --- Pozostałe narzędzia (Sztuczna Inteligencja, Lokalizacja, Usługi w tle) ---
    implementation(libs.generativeai) // Google Gemini AI
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.core.splashscreen)

    // --- Narzędzia testowe ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}