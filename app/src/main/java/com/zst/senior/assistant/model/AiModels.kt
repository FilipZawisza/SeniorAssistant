package com.zst.senior.assistant.model

import java.time.LocalDate
import java.util.UUID

/**
 * Model danych reprezentujący pojedynczą wiadomość w module czatu.
 *
 * @property id Unikalny identyfikator wiadomości (domyślnie generowany losowo).
 * @property text Treść wiadomości.
 * @property isUser Flaga określająca, czy autorem wiadomości jest użytkownik (true), czy asystent AI (false).
 */
data class ChatMessage(val id: UUID = UUID.randomUUID(), val text: String, val isUser: Boolean)

/**
 * Wynik analizy (parsowania) tekstu przez AI dla zadań jednorazowych.
 *
 * Klasa ta przechowuje dane wyekstrahowane z polecenia głosowego lub tekstowego,
 * które odnoszą się do konkretnego punktu w czasie.
 *
 * @property date Data wydarzenia wywnioskowana przez AI.
 * @property time Godzina wydarzenia w formacie HH:mm.
 * @property taskTitle Tytuł zadania lub opis wydarzenia.
 */
data class ParsedResult(val date: LocalDate, val time: String, val taskTitle: String)

/**
 * Wynik analizy (parsowania) tekstu przez AI dla zadań cyklicznych.
 *
 * Przechowuje informacje o przypomnieniach powtarzających się w określone dni tygodnia.
 *
 * @property taskTitle Tytuł przypomnienia.
 * @property time Godzina przypomnienia w formacie HH:mm.
 * @property daysOfWeek Lista dni tygodnia (1-7, gdzie 1 to Poniedziałek), w które przypomnienie ma być aktywne.
 */
data class ParsedRecurringResult(val taskTitle: String, val time: String, val daysOfWeek: List<Int>)

/**
 * Klasa reprezentująca możliwe stany odpowiedzi asystenta AI po przetworzeniu zapytania.
 */
sealed class AiResponse {
    /**
     * Reprezentuje udane sparsowanie zadania jednorazowego.
     * @property result Dane wyekstrahowanego zadania.
     */
    data class Success(val result: ParsedResult) : AiResponse()

    /**
     * Reprezentuje udane sparsowanie zadania cyklicznego.
     * @property result Dane wyekstrahowanego zadania cyklicznego.
     */
    data class SuccessRecurring(val result: ParsedRecurringResult) : AiResponse()

    /**
     * Reprezentuje błąd podczas przetwarzania lub niepowodzenie w interpretacji intencji użytkownika.
     * @property message Komunikat o błędzie lub wyjaśnienie problemu.
     */
    data class Failure(val message: String) : AiResponse()
}
