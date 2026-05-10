package com.zst.senior.assistant.utils

import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Klasa pomocnicza (Wrapper) dla systemowego mechanizmu Text-To-Speech (TTS).
 *
 * Odpowiada za bezpośrednie zlecanie odczytu tekstu przez syntezator mowy.
 * Klasa ta gwarantuje bezpieczeństwo wywołań – ignoruje żądania odczytu, jeśli
 * użytkownik wyłączył tę funkcję w ustawieniach aplikacji lub jeśli systemowy
 * silnik TTS nie został poprawnie zainicjowany.
 *
 * @property tts Instancja systemowego [TextToSpeech]. Może być null, jeśli inicjalizacja się nie powiodła.
 * @property isEnabled Flaga określająca, czy asystent głosowy jest włączony w ustawieniach aplikacji.
 */
class TtsSpeaker(
    private val tts: TextToSpeech?,
    private val isEnabled: Boolean
) {
    /**
     * Odtwarza przekazany tekst na głos.
     *
     * Używa flagi [TextToSpeech.QUEUE_FLUSH], co oznacza, że wywołanie tej metody
     * natychmiast przerywa aktualnie odtwarzany komunikat i zaczyna czytać nowy.
     *
     * @param text Tekst, który ma zostać przeczytany przez lektora.
     */
    fun speak(text: String) {
        if (isEnabled && tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }
}

/**
 * Globalny punkt dostępu (CompositionLocal) dla [TtsSpeaker].
 */
val LocalTtsSpeaker = compositionLocalOf<TtsSpeaker> {
    error("Brak GlobalTtsProvider w drzewie Compose! Upewnij się, że aplikacja jest owinięta w ten Provider.")
}

/**
 * Główny komponent zarządzający cyklem życia i dostępnością Text-To-Speech w aplikacji.
 *
 * Komponent ten:
 * 1. Inicjalizuje silnik [TextToSpeech].
 * 2. Dynamicznie zmienia język lektora w oparciu o stan aplikacji.
 * 3. Dba o brak wycieków pamięci (zamykanie w [DisposableEffect]).
 * 4. Udostępnia głośnik w dół drzewa widoków.
 *
 * @param isTtsEnabled Aktualny stan z preferencji użytkownika (czy czytanie na głos jest aktywne).
 * @param languageCode Aktualny kod języka z ustawień (np. "pl" dla polskiego, "en" dla angielskiego).
 * @param content Reszta interfejsu aplikacji.
 */
@Composable
fun GlobalTtsProvider(
    isTtsEnabled: Boolean,
    languageCode: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Śledzimy, czy asynchroniczna inicjalizacja TTS zakończyła się sukcesem
    var isInitialized by remember { mutableStateOf(false) }

    // Inicjalizacja Text-To-Speech (zapamiętana, żeby nie resetowała się przy każdej rekompozycji)
    val tts = remember {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            }
        }
        ttsInstance
    }

    LaunchedEffect(languageCode, isInitialized) {
        if (isInitialized && tts != null) {
            val locale = Locale.forLanguageTag(languageCode)
            val result = tts.setLanguage(locale)
        }
    }

    // Tworzymy "głośnik" przekazując mu aktualny stan z ustawień (włączony/wyłączony)
    val speaker = remember(tts, isTtsEnabled) {
        TtsSpeaker(tts, isTtsEnabled)
    }

    // Bezpieczne zamykanie procesu TTS
    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Przekazanie głośnika w dół do całej aplikacji
    CompositionLocalProvider(LocalTtsSpeaker provides speaker) {
        content()
    }
}