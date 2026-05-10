package com.zst.senior.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified
import androidx.core.view.WindowInsetsControllerCompat

// --- NOWOCZESNA PALETA GRADIENTOWA (Logo Brand) ---

/**
 * 1. PRIMARY: "Electric Blue Gradient"
 * Bazowy kolor aplikacji - nowoczesny, elektryczny błękit.
 * Używany do głównych akcji, przycisków i nagłówków.
 */
val BrandBlue = Color(0xFF2962FF)         // Główny kolor (Start gradientu)
val BrandBlueLight = Color(0xFF448AFF)    // Jaśniejszy koniec gradientu
val BrandBlueDark = Color(0xFF0039CB)     // Ciemny granat (tekst)

/**
 * 2. SECONDARY: "Solar Orange Gradient"
 * Energetyczny pomarańcz, nawiązujący do serca/laski w logo.
 * Używany do akcentowania, powiadomień lub drugorzędnych akcji.
 */
val BrandOrange = Color(0xFFFF6D00)       // Główny pomarańcz (Start)
val BrandOrangeLight = Color(0xFFFF9E40)  // Jaśniejszy koniec

/**
 * 3. TŁA I POWIERZCHNIE (Modern Clean)
 * Jasne, czytelne kolory tła zapewniające odpowiedni kontrast w standardowym motywie.
 */
val BrandCream = Color(0xFFF5F7FA)        // "Ice White" - ultra nowoczesne, chłodne tło
val BrandSurface = Color(0xFFFFFFFF)      // Czysta biel dla kart

/**
 * 4. KOLORY TECHNICZNE
 * Używane do komunikatów systemowych, statusów i akcji krytycznych.
 */
val SuccessGreen = Color(0xFF00C853)      // Soczysta zieleń
val SosRed = Color(0xFFD50000)            // Alarmowa czerwień

// --- PALETA WCAG (HIGH CONTRAST) ---

/**
 * Specjalistyczna paleta dla słabowidzących (Czarny + Neon).
 * Zapewnia maksymalny współczynnik kontrastu zgodny z wytycznymi WCAG 2.1 AAA.
 */
val WcagBlack = Color(0xFF000000)         // Absolutna czerń
val WcagYellow = Color(0xFFFFFF00)        // Neonowa żółć (najlepszy kontrast z czarnym)
val WcagNeonGreen = Color(0xFF00FF00)     // Neonowa zieleń
val WcagWhite = Color(0xFFFFFFFF)

// --- DEFINICJA GRADIENTÓW ---
// Używaj tych gradientów w przyciskach (GradientButton) i tłach nagłówków

/** Gradient Główny (Niebieski) - Budzący zaufanie, profesjonalny. */
val AppGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
)

/** Gradient dedykowany dla modułu wolontariatu. */
val volunteerGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF42A5F5), Color(0xFF1976D2))
)

/** Gradient Akcentu (Pomarańczowy) - Pobudzający do akcji, pełen energii. */
val AccentGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFB74D), // Góra: Jasny, ciepły "brzoskwiniowy" pomarańcz (Orange 200)
        Color(0xFFE65100)  // Dół: Bardzo głęboki, ceglasty pomarańcz (Orange 900)
    )
)

/** Gradient tła dla ekranów uwierzytelniania (logowanie, rejestracja). */
val AuthBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF4EBF7), // Jasny, pudrowy fiolet/róż (góra)
        Color(0xFFFDF0E6)  // Ciepły brzoskwiniowy (dół)
    )
)

// --- SCHEMATY KOLORÓW ---

/**
 * Standardowy, jasny schemat kolorów Material 3.
 * Łączy kolory marki z odpowiednimi kolorami dla powierzchni i tekstów.
 */
private val DefaultLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = BrandBlueDark,

    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Color(0xFFBF360C),

    tertiary = Color(0xFF00BFA5),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE0F2F1),

    background = BrandCream,
    onBackground = Color(0xFF1C1B1F),

    surface = BrandSurface,
    onSurface = Color(0xFF1C1B1F),

    outline = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF616161),

    error = SosRed,
    onError = Color.White
)

/**
 * Schemat wysokiego kontrastu (WCAG 2.1 AAA).
 * Całkowicie zmienia paletę aplikacji, wymuszając czarne tło i neonowe elementy (żółte/zielone/białe)
 * dla zapewnienia maksymalnej widoczności i czytelności dla seniorów z wada wzroku.
 */
private val HighContrastColorScheme = darkColorScheme(
    primary = WcagYellow,
    onPrimary = WcagBlack,

    secondary = WcagNeonGreen,
    onSecondary = WcagBlack,

    tertiary = WcagWhite,
    onTertiary = WcagBlack,

    background = WcagBlack,
    onBackground = WcagYellow,

    surface = Color(0xFF121212),
    onSurface = WcagWhite,

    error = Color(0xFFFF5252),
    onError = WcagBlack,

    outline = WcagYellow,
    primaryContainer = WcagBlack,
    onPrimaryContainer = WcagYellow
)

/**
 * Funkcja pomocnicza do skalowania stylu tekstu.
 * Skaluje zarówno rozmiar czcionki, jak i wysokość linii, aby uniknąć nakładania się tekstu.
 */
private fun TextStyle.scale(factor: Float): TextStyle {
    return this.copy(
        fontSize = this.fontSize * factor,
        lineHeight = if (this.lineHeight.isSpecified) this.lineHeight * factor else this.lineHeight
    )
}

/**
 * Główny komponent zarządzający wyglądem i motywem aplikacji (AppTheme).
 *
 * Konfiguruje odpowiedni schemat kolorów (zwykły lub o wysokim kontraście), dynamicznie modyfikuje rozmiar
 * czcionek (typografię) w oparciu o preferencje użytkownika oraz kontroluje wygląd pasków systemowych (status bar).
 * Całość zawijana jest w [Surface] z ustaloną modyfikacją tła.
 *
 * @param fontSizeScale Współczynnik powiększenia dla wszystkich stylów tekstu (np. 1.0f to domyślny, 1.5f to tekst powiększony).
 * @param isHighContrast Flaga określająca, czy należy zaaplikować ciemny schemat o skrajnie wysokim kontraście (WCAG).
 * @param content Funkcja Composable zawierająca strukturę UI aplikacji, która zostanie objęta tym motywem.
 */
@Composable
fun AppTheme(
    fontSizeScale: Float = 1.0f,
    isHighContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (isHighContrast) {
        HighContrastColorScheme
    } else {
        DefaultLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(isHighContrast) {
            val window = (view.context as Activity).window
            val insetsController = WindowInsetsControllerCompat(window, view)
            // Ustawienie ciemnego tekstu na paskach w jasnym motywie i jasnego w ciemnym
            insetsController.isAppearanceLightStatusBars = !isHighContrast
            insetsController.isAppearanceLightNavigationBars = !isHighContrast
            onDispose {}
        }
    }

    // Dynamiczne skalowanie typografii mnożąc rozmiar każdego wariantu przez fontSizeScale
    // DODANO: Skalowanie lineHeight, aby zapobiec ucinaniu i nakładaniu się tekstu przy dużych czcionkach.
    val defaultTypography = MaterialTheme.typography
    val scaledTypography = defaultTypography.copy(
        displayLarge = defaultTypography.displayLarge.scale(fontSizeScale),
        displayMedium = defaultTypography.displayMedium.scale(fontSizeScale),
        displaySmall = defaultTypography.displaySmall.scale(fontSizeScale),
        headlineLarge = defaultTypography.headlineLarge.scale(fontSizeScale),
        headlineMedium = defaultTypography.headlineMedium.scale(fontSizeScale),
        headlineSmall = defaultTypography.headlineSmall.scale(fontSizeScale),
        titleLarge = defaultTypography.titleLarge.scale(fontSizeScale),
        titleMedium = defaultTypography.titleMedium.scale(fontSizeScale),
        titleSmall = defaultTypography.titleSmall.scale(fontSizeScale),
        bodyLarge = defaultTypography.bodyLarge.scale(fontSizeScale),
        bodyMedium = defaultTypography.bodyMedium.scale(fontSizeScale),
        bodySmall = defaultTypography.bodySmall.scale(fontSizeScale),
        labelLarge = defaultTypography.labelLarge.scale(fontSizeScale),
        labelMedium = defaultTypography.labelMedium.scale(fontSizeScale),
        labelSmall = defaultTypography.labelSmall.scale(fontSizeScale)
    )

    MaterialTheme(
        colorScheme = colors,
        typography = scaledTypography
    ) {
        // Tło zależne od wybranego motywu - gradient dla wersji jasnej, czyste czarne tło dla wysokiego kontrastu
        val appBackgroundModifier = Modifier
            .fillMaxSize()
            .then(
                if (isHighContrast) {
                    Modifier.background(Color.Black)
                } else {
                    Modifier.background(AuthBackgroundGradient)
                }
            )

        Surface(
            modifier = appBackgroundModifier,
            color = Color.Transparent // Oznaczone jako Transparent by wyeksponować tło z appBackgroundModifier
        ) {
            content()
        }
    }
}