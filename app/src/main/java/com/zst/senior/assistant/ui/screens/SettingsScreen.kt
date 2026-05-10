package com.zst.senior.assistant.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zst.senior.assistant.R
import com.zst.senior.assistant.navigation.safePopBackStack
import com.zst.senior.assistant.ui.theme.AccentGradient
import com.zst.senior.assistant.viewmodel.AuthViewModel
import com.zst.senior.assistant.viewmodel.SettingsViewModel

// WAŻNE: Import głośnika
import com.zst.senior.assistant.utils.LocalTtsSpeaker

/**
 * Klasa pomocnicza (Data Class) przechowująca konfigurację kolorów dla ekranu ustawień.
 * Zapewnia czystość kodu (Clean Code) poprzez centralizację palety barw w zależności
 * od aktywnego motywu (np. tryb wysokiego kontrastu, tryb administratora).
 *
 * @property content Główny kolor zawartości (tekst nagłówków, ikony).
 * @property cardBackground Kolor tła dla kart ustawień.
 * @property cardText Główny kolor tekstu wewnątrz kart.
 * @property cardSubText Kolor tekstu pomocniczego (podtytułów) wewnątrz kart.
 * @property primaryHighContrast Kolor akcentujący używany w trybie wysokiego kontrastu (często jaskrawy/neonowy).
 * @property border Opcjonalne obramowanie kart stosowane w trybach specyficznych (np. HC lub jasnym).
 */
private data class ScreenColors(
    val content: Color,
    val cardBackground: Color,
    val cardText: Color,
    val cardSubText: Color,
    val primaryHighContrast: Color,
    val border: BorderStroke?
)

/**
 * Główny ekran ustawień aplikacji, dostosowany przede wszystkim do potrzeb seniorów.
 *
 * Pozwala użytkownikowi na zarządzanie preferencjami dostępności (Accessibility),
 * w tym włączaniem asystenta głosowego (TTS), trybu wysokiego kontrastu oraz zmianą
 * rozmiaru czcionki. Ekran dynamicznie dostosowuje swój wygląd (tło, kolory) na
 * podstawie roli zalogowanego użytkownika (senior, wolontariusz, admin) oraz
 * wybranych ustawień kontrastu.
 *
 * @param navController Kontroler służący do nawigacji (np. powrót do poprzedniego ekranu).
 * @param settingsViewModel ViewModel zarządzający stanem ustawień (czcionka, kontrast, TTS).
 * @param authViewModel ViewModel autoryzacji używany do określenia roli użytkownika.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel
) {
    // --- POBIERAMY GŁOŚNIK ---
    val tts = LocalTtsSpeaker.current

    // --- WCZEŚNIEJSZE POBRANIE TEKSTÓW TTS ---
    val msgBack = stringResource(R.string.settings_back)
    val msgTtsEnabled = stringResource(R.string.settings_tts_enabled)
    val msgTtsDisabled = stringResource(R.string.settings_tts_disabled)
    val msgContrastEnabled = stringResource(R.string.settings_contrast_enabled)
    val msgContrastDisabled = stringResource(R.string.settings_contrast_disabled)

    // 1. Pobieranie Stanu
    val fontSizeScale by settingsViewModel.fontSizeScale.collectAsState()
    val isHighContrastSwitch by settingsViewModel.isHighContrast.collectAsState()
    val isTtsEnabled by settingsViewModel.isTtsEnabled.collectAsState() // NOWE: Stan TTS
    val userRole by authViewModel.userRole.collectAsState(initial = "senior")

    // 2. Logika kolorów i tła (wyciągnięta przed UI)
    val isThemeHighContrast = MaterialTheme.colorScheme.background == Color.Black || isHighContrastSwitch
    val hcColor = MaterialTheme.colorScheme.primary // Zwykle Neon Yellow w trybie HC

    val isBackgroundWhite = userRole?.lowercase() == "admin"

    // Konfiguracja palety kolorów w jednym miejscu
    val colors = if (isThemeHighContrast) {
        ScreenColors(
            content = hcColor,
            cardBackground = Color.Black,
            cardText = hcColor,
            cardSubText = Color.White,
            primaryHighContrast = hcColor,
            border = BorderStroke(2.dp, hcColor)
        )
    } else if (isBackgroundWhite) {
        ScreenColors(
            content = Color.Black,
            cardBackground = Color.White,
            cardText = Color.Black,
            cardSubText = Color.Gray,
            primaryHighContrast = MaterialTheme.colorScheme.primary,
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        )
    } else {
        ScreenColors(
            content = Color.White,
            cardBackground = Color.White,
            cardText = MaterialTheme.colorScheme.onSurface,
            cardSubText = MaterialTheme.colorScheme.onSurfaceVariant,
            primaryHighContrast = MaterialTheme.colorScheme.primary,
            border = null
        )
    }

    // Definicja tła (Gradient vs Czerń vs Biel)
    val backgroundModifier = Modifier.fillMaxSize().then(
        if (isThemeHighContrast) {
            Modifier.background(Color.Black)
        } else if (isBackgroundWhite) {
            Modifier.background(Color.White)
        } else {
            val brush = when (userRole?.lowercase()) {
                "wolontariusz", "volunteer" -> Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1976D2)))
                else -> AccentGradient // Senior (Domyślny gradient)
            }
            Modifier.background(brush)
        }
    )

    Scaffold(
        containerColor = Color.Transparent, // Ważne: przezroczysty Scaffold, by widzieć tło Boxa
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = colors.content
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        tts.speak(msgBack) // CZYTAMY PRZYCISK WSTECZ
                        navController.safePopBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = msgBack)
                    }
                }
            )
        }
    ) { paddingValues ->
        // Główny kontener tła
        Box(
            modifier = backgroundModifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                SettingsHeader(colors)

                Spacer(Modifier.height(32.dp))

                // NOWE: Karta Asystenta Głosowego (TTS)
                SettingsToggleCard(
                    title = stringResource(R.string.settings_tts_title),
                    subtitle = stringResource(R.string.settings_tts_subtitle),
                    icon = Icons.Default.RecordVoiceOver,
                    isChecked = isTtsEnabled,
                    onToggle = {
                        settingsViewModel.setTtsEnabled(it)
                        // CZYTAMY ZMIANĘ STANU
                        tts.speak(if (it) msgTtsEnabled else msgTtsDisabled)
                    },
                    colors = colors,
                    isHighContrastTheme = isThemeHighContrast
                )

                Spacer(Modifier.height(24.dp))

                // Karta Kontrastu (Używa teraz uniwersalnej funkcji)
                SettingsToggleCard(
                    title = stringResource(R.string.settings_contrast_title),
                    subtitle = stringResource(R.string.settings_contrast_subtitle),
                    icon = Icons.Default.Contrast,
                    isChecked = isHighContrastSwitch,
                    onToggle = {
                        settingsViewModel.setHighContrast(it)
                        // CZYTAMY ZMIANĘ STANU
                        tts.speak(if (it) msgContrastEnabled else msgContrastDisabled)
                    },
                    colors = colors,
                    isHighContrastTheme = isThemeHighContrast
                )

                Spacer(Modifier.height(24.dp))

                // NOWE: Wybór języka
                LanguageSelectionCard(
                    currentLanguage = settingsViewModel.language.collectAsState().value,
                    onLanguageChange = { settingsViewModel.setLanguage(it) },
                    colors = colors,
                    isHighContrast = isThemeHighContrast
                )

                Spacer(Modifier.height(24.dp))

                // Karta Rozmiaru Tekstu
                FontSizeCard(
                    fontSizeScale = fontSizeScale,
                    onScaleChange = { settingsViewModel.setFontSizeScale(it) },
                    colors = colors,
                    isHighContrast = isThemeHighContrast
                )

                Spacer(Modifier.height(48.dp))
            }
        }
    }
}

// --- KOMPONENTY WYDZIELONE (Lepsza czytelność i reużywalność) ---

/**
 * Komponent wyświetlający nagłówek ekranu ustawień.
 * Zawiera centralną ikonę w okrągłym kontenerze oraz tytuł i opis ekranu.
 *
 * @param colors Konfiguracja kolorów determinująca wygląd nagłówka.
 */
@Composable
private fun SettingsHeader(colors: ScreenColors) {
    Surface(
        shape = CircleShape,
        color = if (colors.border != null && colors.content != Color.Black) Color.Black else if (colors.content == Color.Black) Color.LightGray.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.2f),
        border = colors.border?.let { BorderStroke(if (colors.content == Color.Black) 1.dp else 3.dp, it.brush) },
        modifier = Modifier.size(80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings_icon_desc),
                tint = colors.content,
                modifier = Modifier.size(40.dp)
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = colors.content,
        textAlign = TextAlign.Center
    )

    Text(
        text = stringResource(R.string.settings_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.content.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp)
    )
}

/**
 * Komponent karty pozwalający na wybór języka aplikacji.
 *
 * @param currentLanguage Aktualnie wybrany kod języka.
 * @param onLanguageChange Funkcja wywoływana przy wyborze nowego języka.
 * @param colors Konfiguracja kolorów dla karty.
 * @param isHighContrast Określa, czy włączony jest tryb wysokiego kontrastu.
 */
@Composable
private fun LanguageSelectionCard(
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    colors: ScreenColors,
    isHighContrast: Boolean
) {
    SettingsCard(colors = colors) {
        SettingsCardHeader(
            icon = Icons.Default.Language,
            title = stringResource(R.string.settings_language_title),
            colors = colors,
            isHighContrast = isHighContrast
        )
        Text(
            text = stringResource(R.string.settings_language_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = colors.cardSubText,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LanguageButton(
                label = stringResource(R.string.settings_language_pl),
                isSelected = currentLanguage == "pl",
                onClick = { onLanguageChange("pl") },
                modifier = Modifier.weight(1f),
                colors = colors,
                isHighContrast = isHighContrast
            )
            LanguageButton(
                label = stringResource(R.string.settings_language_en),
                isSelected = currentLanguage == "en",
                onClick = { onLanguageChange("en") },
                modifier = Modifier.weight(1f),
                colors = colors,
                isHighContrast = isHighContrast
            )
        }
    }
}

/**
 * Przycisk wyboru konkretnego języka.
 */
@Composable
private fun LanguageButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ScreenColors,
    isHighContrast: Boolean
) {
    val containerColor = if (isSelected) {
        if (isHighContrast) colors.primaryHighContrast else MaterialTheme.colorScheme.primary
    } else {
        if (isHighContrast) Color.Black else Color.Transparent
    }
    val contentColor = if (isSelected) {
        if (isHighContrast) Color.Black else Color.White
    } else {
        if (isHighContrast) Color.White else MaterialTheme.colorScheme.primary
    }
    val border = if (isHighContrast) {
        BorderStroke(2.dp, colors.primaryHighContrast)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border
    ) {
        Text(text = label, fontWeight = FontWeight.Bold)
    }
}

/**
 * Komponent karty pozwalający na regulację rozmiaru tekstu w aplikacji.
 * Zawiera wizualny podgląd aktualnie wybranego rozmiaru oraz suwak do płynnej (krokowej) zmiany.
 * Po zakończeniu przesuwania suwaka, nowa wartość odczytywana jest przez system TTS.
 *
 * @param fontSizeScale Aktualny mnożnik skali czcionki.
 * @param onScaleChange Funkcja wywoływana przy przesunięciu suwaka.
 * @param colors Konfiguracja kolorów dla karty.
 * @param isHighContrast Określa, czy włączony jest tryb wysokiego kontrastu.
 */
@Composable
private fun FontSizeCard(
    fontSizeScale: Float,
    onScaleChange: (Float) -> Unit,
    colors: ScreenColors,
    isHighContrast: Boolean
) {
    val tts = LocalTtsSpeaker.current // Pobieramy głośnik dla suwaka

    // Pobieramy szkielet z miejscem na zmienną, np. "Rozmiar %d procent"
    val fontTtsFormat = stringResource(R.string.settings_font_tts_format)

    SettingsCard(colors = colors) {
        // Tytuł Sekcji
        SettingsCardHeader(
            icon = Icons.Default.FormatSize,
            title = stringResource(R.string.settings_font_size_title),
            colors = colors,
            isHighContrast = isHighContrast
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp),
            color = colors.cardText.copy(alpha = 0.2f)
        )

        // Podgląd (Preview Box)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Stała wysokość, aby box nie skakał przy zmianie czcionki
                .background(
                    if (isHighContrast) Color.DarkGray else if (colors.content == Color.Black) Color.LightGray.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .then(if (isHighContrast) Modifier.border(1.dp, colors.primaryHighContrast, RoundedCornerShape(12.dp)) else if (colors.content == Color.Black) Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp)) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.settings_font_preview),
                fontSize = (20 * fontSizeScale).sp, // Bazowe 20sp * skala
                lineHeight = (28 * fontSizeScale).sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = if (isHighContrast) Color.White else Color.Black
            )
        }

        Spacer(Modifier.height(24.dp))

        // Slider (Suwak)
        Slider(
            value = fontSizeScale,
            onValueChange = onScaleChange,
            // CZYTAMY ZMIANĘ DOPIERO GDY UŻYTKOWNIK PUŚCI SUWAK
            onValueChangeFinished = {
                val percentage = (fontSizeScale * 100).toInt()
                tts.speak(fontTtsFormat.format(percentage))
            },
            valueRange = 0.8f..1.5f, // Zakres od 80% do 150%
            steps = 7, // Skok co 10%, teraz nieparzysta ilosc
            colors = SliderDefaults.colors(
                thumbColor = if (isHighContrast) colors.primaryHighContrast else MaterialTheme.colorScheme.primary,
                activeTrackColor = if (isHighContrast) colors.primaryHighContrast else MaterialTheme.colorScheme.primary,
                inactiveTrackColor = (if (isHighContrast) colors.primaryHighContrast else MaterialTheme.colorScheme.primary).copy(alpha = 0.2f)
            )
        )

        // Etykiety pod suwakiem
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.settings_font_small), style = MaterialTheme.typography.labelSmall, color = colors.cardSubText)
            Text(stringResource(R.string.settings_font_default), style = MaterialTheme.typography.labelSmall, color = colors.cardSubText)
            Text(stringResource(R.string.settings_font_large), style = MaterialTheme.typography.labelSmall, color = colors.cardSubText)
        }
    }
}

/**
 * Uniwersalny komponent karty ustawień wyposażony w przełącznik (Switch).
 * Używany do aktywacji/dezaktywacji funkcji binarnych (np. TTS, wysoki kontrast).
 *
 * @param title Główny tytuł przełącznika.
 * @param subtitle Opcjonalny opis funkcji.
 * @param icon Ikona wektorowa reprezentująca ustawienie.
 * @param isChecked Aktualny stan przełącznika.
 * @param onToggle Funkcja wywoływana po zmianie stanu przełącznika.
 * @param colors Konfiguracja kolorów dla karty.
 * @param isHighContrastTheme Określa, czy aktywny jest motyw wysokiego kontrastu.
 */
@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    colors: ScreenColors,
    isHighContrastTheme: Boolean
) {
    SettingsCard(colors = colors) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = stringResource(R.string.settings_card_icon_desc, title),
                    tint = if (isHighContrastTheme) colors.primaryHighContrast else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.cardText
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.cardSubText
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (isHighContrastTheme) colors.primaryHighContrast else MaterialTheme.colorScheme.primary,
                    checkedTrackColor = (if (isHighContrastTheme) colors.primaryHighContrast else MaterialTheme.colorScheme.primary).copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

/**
 * Bazowy komponent (Wrapper) definiujący podstawowy wygląd karty ustawień.
 * Wprowadza zasadę DRY (Don't Repeat Yourself), standaryzując zaokrąglenia, cienie i marginesy
 * dla wszystkich sekcji konfiguracyjnych na tym ekranie.
 *
 * @param colors Konfiguracja kolorów determinująca tło i obramowanie.
 * @param content Funkcja Composable definiująca zawartość wewnątrz karty.
 */
@Composable
private fun SettingsCard(
    colors: ScreenColors,
    content: @Composable ColumnScope.() -> Unit
) {
    val isHighContrast = colors.content != Color.White && colors.content != Color.Black
    val isWhiteTheme = colors.content == Color.Black

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (colors.border != null) Modifier.border(colors.border, RoundedCornerShape(24.dp)) else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.cardBackground),
        elevation = CardDefaults.cardElevation(if (isHighContrast || isWhiteTheme) 4.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            content = content
        )
    }
}

/**
 * Podkomponent wyświetlający wewnętrzny nagłówek w obrębie karty ustawień.
 *
 * @param icon Ikona przypisana do danej sekcji.
 * @param title Tytuł nagłówka.
 * @param colors Konfiguracja kolorów określająca barwę tekstu.
 * @param isHighContrast Przełącza kolor ikony na wariant wysokokontrastowy.
 */
@Composable
private fun SettingsCardHeader(
    icon: ImageVector,
    title: String,
    colors: ScreenColors,
    isHighContrast: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = stringResource(R.string.settings_section_icon_desc, title),
            tint = if (isHighContrast) colors.primaryHighContrast else MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.cardText
        )
    }
}