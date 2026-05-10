package com.zst.senior.assistant.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.zst.senior.assistant.BuildConfig.GEMINI_API_KEY
import com.zst.senior.assistant.model.AiResponse
import com.zst.senior.assistant.model.ParsedResult
import com.zst.senior.assistant.model.ParsedRecurringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate

/**
 * Globalna instancja modelu Gemini.
 * Wykorzystuje model "gemini-2.5-flash" zdefiniowany do zwracania ścisłego formatu JSON,
 * co znacznie ułatwia późniejsze parsowanie odpowiedzi.
 */
private val generativeModel = GenerativeModel(
    modelName = "gemini-2.5-flash",
    apiKey = GEMINI_API_KEY,
    generationConfig = generationConfig {
        responseMimeType = "application/json"
    }
)

/**
 * Przetwarza wejściowy tekst od użytkownika (pochodzący z klawiatury lub dyktowania głosowego)
 * za pomocą modelu Google Gemini, próbując wyodrębnić z niego intencję dodania wydarzenia.
 *
 * Funkcja wstrzykuje do promptu aktualną datę, aby model mógł poprawnie interpretować
 * pojęcia względne (np. "jutro", "za tydzień"). Zwraca ustrukturyzowany obiekt [AiResponse],
 * który klasyfikuje intencję jako jednorazowe wydarzenie, wydarzenie cykliczne lub odpowiedź czatu.
 *
 * @param textInput Surowy tekst wprowadzony przez użytkownika (np. "Przypominaj o lekach w każdy poniedziałek o 18").
 * @param language Kod języka użytkownika (np. "pl", "en").
 * @return [AiResponse] będący wynikiem parsowania i walidacji odpowiedzi z modelu AI.
 */
@RequiresApi(Build.VERSION_CODES.O)
suspend fun processInputWithGemini(textInput: String, language: String): AiResponse = withContext(Dispatchers.IO) {
    val currentDate = LocalDate.now()

    // Instrukcja systemowa dopasowana do języka użytkownika
    val systemInstruction = if (language == "en") {
        """
            You are a smart and very helpful Senior Assistant.
            You MUST ALWAYS respond in JSON format! Never use plain text outside of JSON.
            Today's date: $currentDate (${currentDate.dayOfWeek}).

            RULES:
            1. ONE-TIME EVENT (e.g., "doctor visit tomorrow") -> use status: "success", provide title, time (HH:mm, 24h format, max 23:59), and date (YYYY-MM-DD).
            2. RECURRING (e.g., "remind me on Mondays and Wednesdays") -> use status: "recurring", provide title, time (HH:mm, 24h format), and daysOfWeek as an ARRAY OF NUMBERS (1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun. E.g., [1, 3]).
            3. OTHER (questions, chat) -> use status: "chat", provide message.
            4. LANGUAGE: You MUST respond EXCLUSIVELY in ENGLISH. All values for "title" and "message" MUST be in English.

            JSON RESPONSE FORMAT:
            {
              "status": "success" | "recurring" | "chat",
              "title": "Task Title",
              "date": "YYYY-MM-DD",
              "time": "HH:mm",
              "daysOfWeek": [1, 3],
              "message": "Your response message"
            }
        """.trimIndent()
    } else {
        """
            Jesteś inteligentnym i bardzo pomocnym Asystentem Seniora. 
            MUSISZ ZAWSZE odpowiadać w formacie JSON! Nie używaj nigdy czystego tekstu poza JSONem.
            Dzisiejsza data: $currentDate (${currentDate.dayOfWeek}).
            
            ZASADY:
            1. JEDNORAZOWE WYDARZENIE (np. wizyta u lekarza na jutro) -> użyj status: "success", podaj title, time (HH:mm, format 24h, max 23:59) i date (YYYY-MM-DD).
            2. CYKLICZNE (np. "przypominaj w poniedziałki i środy") -> użyj status: "recurring", podaj title, time (HH:mm, format 24h) oraz daysOfWeek jako TABLICĘ LICZB (1=Pon, 2=Wt, 3=Śr, 4=Czw, 5=Pt, 6=Sob, 7=Nie. Np. [1, 3]).
            3. INNE (pytania, pogawędka) -> użyj status: "chat", podaj message.
            4. JĘZYK: Musisz odpowiadać WYŁĄCZNIE w języku POLSKIM. Wszystkie wartości dla "title" oraz "message" MUSZĄ być po polsku.
            
            FORMAT ODPOWIEDZI JSON:
            {
              "status": "success" | "recurring" | "chat",
              "title": "Tytuł zadania",
              "date": "YYYY-MM-DD",
              "time": "HH:mm",
              "daysOfWeek": [1, 3],
              "message": "Twoja odpowiedź"
            }
        """.trimIndent()
    }

    val fullPrompt = "$systemInstruction\n\nUżytkownik: \"$textInput\"\nTy:"

    try {
        // Wykonanie zapytania z mechanizmem automatycznych ponowień w razie błędu sieciowego
        val jsonText = retryIO(times = 3, initialDelay = 500) {
            val response = generativeModel.generateContent(fullPrompt)
            response.text ?: throw IOException("Pusta odpowiedź API")
        }

        Log.d("GeminiResponse", jsonText)

        // Zabezpieczenie przed halucynacjami znaczników Markdown (pomimo wymuszenia MIME)
        val cleanJsonText = jsonText.replace("```json", "").replace("```", "").trim()
        val jsonObject = try {
            JSONObject(cleanJsonText)
        } catch (e: Exception) {
            return@withContext AiResponse.Failure("Błąd JSON: Asystent zwrócił zły format.")
        }

        // BEZWZGLĘDNA WALIDACJA CZASU (Tylko 00:00 - 23:59)
        val timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]\$".toRegex()

        when (jsonObject.optString("status")) {
            "success" -> {
                val title = jsonObject.optString("title")
                val time = jsonObject.optString("time")
                val dateStr = jsonObject.optString("date")

                if (title.isBlank() || time.isBlank() || dateStr.isBlank()) {
                    return@withContext AiResponse.Failure("Błąd: Brak kompletnych danych w odpowiedzi.")
                }
                if (!time.matches(timeRegex)) {
                    return@withContext AiResponse.Failure("Błąd: Asystent podał nierealną godzinę ($time).")
                }

                val date = try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    return@withContext AiResponse.Failure("Błąd parsowania daty.")
                }

                if (date.isBefore(currentDate)) {
                    return@withContext AiResponse.Failure("Nie mogę zaplanować zadania w przeszłości.")
                }

                AiResponse.Success(ParsedResult(date = date, time = time, taskTitle = title))
            }
            "recurring" -> {
                val title = jsonObject.optString("title")
                val time = jsonObject.optString("time")

                // Parsowanie tablicy dni tygodnia
                val daysArray = jsonObject.optJSONArray("daysOfWeek")
                val daysOfWeek = mutableListOf<Int>()
                if (daysArray != null) {
                    for (i in 0 until daysArray.length()) {
                        val day = daysArray.optInt(i)
                        if (day in 1..7) daysOfWeek.add(day)
                    }
                }

                if (title.isBlank() || time.isBlank() || daysOfWeek.isEmpty()) {
                    return@withContext AiResponse.Failure("Błąd: Asystent nie zrozumiał dni tygodnia.")
                }
                if (!time.matches(timeRegex)) {
                    return@withContext AiResponse.Failure("Błąd: Asystent podał nierealną godzinę ($time).")
                }

                AiResponse.SuccessRecurring(ParsedRecurringResult(title, time, daysOfWeek))
            }
            "chat" -> AiResponse.Failure(jsonObject.optString("message", "Nie do końca zrozumiałem."))
            "failure" -> AiResponse.Failure(jsonObject.optString("message", "Nie zrozumiano polecenia."))
            else -> AiResponse.Failure("Nieznany status od Asystenta: ${jsonObject.optString("status")}")
        }
    } catch (e: IOException) {
        AiResponse.Failure("Problem z internetem. Sprawdź połączenie.")
    } catch (e: Exception) {
        AiResponse.Failure("CRASH: ${e.javaClass.simpleName} -> ${e.message}")
    }
}

/**
 * Generyczna funkcja pomocnicza realizująca wzorzec Exponential Backoff.
 * Automatycznie ponawia wykonanie podanego bloku kodu w przypadku wystąpienia [IOException].
 *
 * @param T Typ zwracany przez blok kodu.
 * @param times Maksymalna liczba prób wykonania kodu.
 * @param initialDelay Początkowe opóźnienie (w milisekundach) przed pierwszą ponowną próbą.
 * @param block Zawartość do wykonania (np. wywołanie API sieciowego).
 * @return Wynik działania bloku kodu, jeśli ostatecznie zakończy się sukcesem.
 */
suspend fun <T> retryIO(times: Int = 3, initialDelay: Long = 500, block: suspend () -> T): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            delay(currentDelay)
            currentDelay *= 2
        }
    }
    // Ostatnia próba, jeśli wyrzuci błąd, to niech wyjdzie na zewnątrz do bloku try-catch w funkcji nadrzędnej
    return block()
}