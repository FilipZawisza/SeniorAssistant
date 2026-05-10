package com.zst.senior.assistant.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.zst.senior.assistant.model.CalendarEvent
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Repozytorium obsługujące operacje na kalendarzu użytkownika w Firestore.
 */
class CalendarRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Pobiera wydarzenia dla danego użytkownika i roli.
     *
     * @param userId ID użytkownika.
     * @param role Rola użytkownika ("Senior" lub "Wolontariusz").
     * @return Mapa wydarzeń pogrupowana datami.
     */
    suspend fun getEvents(userId: String, role: String): Map<LocalDate, List<CalendarEvent>> {
        val collectionName = if (role == "Senior") "WydarzeniaSenior" else "WydarzeniaWolontariusz"
        val idField = if (role == "Senior") "SeniorID" else "WolontariuszID"

        val result = db.collection(collectionName)
            .whereEqualTo(idField, userId)
            .get()
            .await()

        val eventsMap = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()
        for (doc in result.documents) {
            try {
                val timestamp = doc.getTimestamp("Data") ?: continue
                val title = doc.getString("Tytul") ?: "Brak tytułu"
                val docId = doc.id

                val localDateTime = timestamp.toDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime()

                val date = localDateTime.toLocalDate()
                val time = localDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

                val event = CalendarEvent(firebaseDocId = docId, time = time, title = title)
                eventsMap.getOrPut(date) { mutableListOf() }.add(event)
            } catch (e: Exception) {
                Log.e("CalendarRepo", "Błąd parsowania: ${e.message}")
            }
        }
        eventsMap.values.forEach { it.sortBy { e -> e.time } }
        return eventsMap
    }

    /**
     * Dodaje nowe wydarzenie do bazy.
     *
     * @param userId ID użytkownika.
     * @param role Rola użytkownika.
     * @param date Data.
     * @param time Czas (HH:mm).
     * @param title Tytuł.
     * @return ID nowo utworzonego dokumentu.
     */
    suspend fun addEvent(userId: String, role: String, date: LocalDate, time: String, title: String): String {
        val collectionName = if (role == "Senior") "WydarzeniaSenior" else "WydarzeniaWolontariusz"
        val idField = if (role == "Senior") "SeniorID" else "WolontariuszID"

        val localTime = LocalTime.parse(time)
        val dateTime = date.atTime(localTime)
        val firebaseTimestamp = Timestamp(dateTime.atZone(ZoneId.systemDefault()).toInstant())

        val newEventData = hashMapOf(
            "Tytul" to title,
            "Data" to firebaseTimestamp,
            idField to userId
        )

        val ref = db.collection(collectionName).add(newEventData).await()
        return ref.id
    }

    /**
     * Usuwa wydarzenie z bazy.
     *
     * @param role Rola użytkownika.
     * @param docId ID dokumentu w Firestore.
     */
    suspend fun deleteEvent(role: String, docId: String) {
        val collectionName = if (role == "Senior") "WydarzeniaSenior" else "WydarzeniaWolontariusz"
        db.collection(collectionName).document(docId).delete().await()
    }
}
