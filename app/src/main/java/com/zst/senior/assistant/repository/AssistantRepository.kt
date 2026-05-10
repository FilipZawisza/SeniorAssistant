package com.zst.senior.assistant.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.zst.senior.assistant.model.AiResponse
import com.zst.senior.assistant.utils.processInputWithGemini

/**
 * Repozytorium obsługujące komunikację z asystentem AI (Gemini).
 *
 * Klasa stanowi warstwę pośrednią między ViewModel a narzędziami AI,
 * izolując logikę przetwarzania tekstu.
 */
class AssistantRepository {

    /**
     * Przetwarza tekst wejściowy użytkownika za pomocą silnika AI.
     *
     * @param text Surowy tekst komendy.
     * @param language Kod języka użytkownika (np. "pl", "en").
     * @return Obiekt [AiResponse] zawierający ustrukturyzowany wynik analizy.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun processAssistantInput(text: String, language: String): AiResponse {
        return processInputWithGemini(text, language)
    }
}
