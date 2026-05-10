package com.zst.senior.assistant

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zst.senior.assistant.navigation.AppNavigator
import com.zst.senior.assistant.repository.SettingsRepository
import com.zst.senior.assistant.ui.components.BatteryOptimizationDialog
import com.zst.senior.assistant.ui.theme.AppTheme
import com.zst.senior.assistant.utils.GlobalTtsProvider
import com.zst.senior.assistant.utils.NotificationHelper
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.FallDetectorViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModel
import com.zst.senior.assistant.viewmodel.HelpRequestViewModelFactory
import com.zst.senior.assistant.viewmodel.SeniorCalendarViewModel
import com.zst.senior.assistant.viewmodel.Seniorm2ViewModel
import com.zst.senior.assistant.viewmodel.SettingsViewModel
import com.zst.senior.assistant.viewmodel.UserProfileViewModel
import java.util.Locale

/**
 * Główny punkt wejścia do aplikacji (Entry Point).
 *
 * Klasa [MainActivity] odpowiada za:
 * - Pełną inicjalizację środowiska Jetpack Compose.
 * - Dynamiczną zmianę języka aplikacji (Locale) bez restartu.
 * - Zarządzanie uprawnieniami systemowymi (Powiadomienia, Bateria).
 * - Tworzenie globalnych instancji ViewModeli.
 * - Konfigurację dostępności (TTS, Kontrast, Rozmiar czcionki).
 */
class MainActivity : ComponentActivity() {

    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Interfejs Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Kanały powiadomień
        NotificationHelper.createNotificationChannels(this)

        setContent {
            val context = LocalContext.current
            val application = context.applicationContext as Application

            // Pobieramy bezpiecznie konfigurację wspieraną przez Compose
            val currentConfiguration = LocalConfiguration.current

            // --- UPRAWNIENIA DO POWIADOMIEŃ ---
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // --- INICJALIZACJA USTAWIEŃ ---
            val settingsRepo = remember { SettingsRepository(context) }
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.SettingsViewModelFactory(settingsRepo)
            )

            val fontSizeScale by settingsViewModel.fontSizeScale.collectAsState()
            val isHighContrast by settingsViewModel.isHighContrast.collectAsState()
            val isTtsEnabled by settingsViewModel.isTtsEnabled.collectAsState()
            val language by settingsViewModel.language.collectAsState()

            // --- DYNAMICZNA ZMIANA JĘZYKA ---
            LaunchedEffect(language) {
                val locale = Locale.forLanguageTag(language)
                Locale.setDefault(locale)
                val resources = context.resources

                // Tworzymy kopię bieżącej konfiguracji, zamiast mutować ją bezpośrednio z contextu
                val config = Configuration(currentConfiguration)
                config.setLocale(locale)

                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            AppTheme(fontSizeScale = fontSizeScale, isHighContrast = isHighContrast) {
                // Składnik do wymuszenia odświeżania zasobów Compose przy zmianie języka
                key(language) {
                    // Przygotowujemy odświeżoną konfigurację dla Compose
                    val updatedConfig = Configuration(currentConfiguration).apply {
                        setLocale(Locale.forLanguageTag(language))
                    }

                    CompositionLocalProvider(
                        // Używamy zaktualizowanej konfiguracji, unikając błędu lintera
                        LocalConfiguration provides updatedConfig
                    ) {
                        // --- INICJALIZACJA BAZOWYCH VIEWMODELI ---
                        val authViewModel: AuthViewModel = viewModel(
                            factory = AuthViewModel.AuthViewModelFactory(application)
                        )

                        val currentUserId by authViewModel.currentUserId.collectAsState()
                        val isLoggedIn = currentUserId != null

                        // --- OPTYMALIZACJA BATERII ---
                        val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
                        val packageName = context.packageName
                        var showBatteryDialog by remember { mutableStateOf(false) }

                        val batteryOptimizationLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartActivityForResult()
                        ) { }

                        LaunchedEffect(isLoggedIn) {
                            if (isLoggedIn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                                    showBatteryDialog = true
                                }
                            } else if (!isLoggedIn) {
                                showBatteryDialog = false
                            }
                        }

                        if (showBatteryDialog) {
                            BatteryOptimizationDialog(
                                onConfirm = {
                                    showBatteryDialog = false
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = "package:$packageName".toUri()
                                    }
                                    batteryOptimizationLauncher.launch(intent)
                                },
                                onDismiss = {
                                    showBatteryDialog = false
                                }
                            )
                        }

                        // --- VIEW MODELE (Zoptymalizowane przy użyciu viewModelFactory DSL) ---
                        val userProfileViewModel: UserProfileViewModel = viewModel(
                            factory = UserProfileViewModel.Factory(authViewModel)
                        )

                        val helpRequestViewModel: HelpRequestViewModel = viewModel(
                            factory = HelpRequestViewModelFactory(application, authViewModel)
                        )

                        val calendarViewModel: SeniorCalendarViewModel = viewModel(
                            factory = SeniorCalendarViewModel.Factory(application, authViewModel)
                        )

                        val fallViewModel: FallDetectorViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer { FallDetectorViewModel(application) }
                            }
                        )

                        val seniorm2ViewModel: Seniorm2ViewModel = viewModel(
                            factory = viewModelFactory {
                                initializer { Seniorm2ViewModel(application) }
                            }
                        )

                        // --- GŁÓWNA NAWIGACJA I TTS ---
                        GlobalTtsProvider(
                            isTtsEnabled = isTtsEnabled,
                            languageCode = language
                        ) {
                            // Zawijamy aplikację w Box, aby dodać testowy przycisk na wierzchu
                            Box(modifier = Modifier.fillMaxSize()) {
                                AppNavigator(
                                    calendarViewModel = calendarViewModel,
                                    fallDetectorViewModel = fallViewModel,
                                    helpRequestViewModel = helpRequestViewModel,
                                    seniorm2ViewModel = seniorm2ViewModel,
                                    settingsViewModel = settingsViewModel,
                                    authViewModel = authViewModel,
                                    userProfileViewModel = userProfileViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}