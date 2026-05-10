package com.zst.senior.assistant.model

/**
 * Model reprezentujący pojedyncze wydarzenie w kalendarzu użytkownika.
 *
 * @property firebaseDocId Unikalny identyfikator dokumentu w bazie danych Firebase Firestore.
 * @property time Godzina wydarzenia w formacie tekstowym (np. "10:00").
 * @property title Nazwa lub krótki opis wydarzenia.
 */
data class CalendarEvent(val firebaseDocId: String, val time: String, val title: String)
