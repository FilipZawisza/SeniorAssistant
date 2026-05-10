package com.zst.senior.assistant.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.zst.senior.assistant.model.Seniorm2Item
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek

/**
 * Repozytorium obsługujące operacje na danych cyklicznych przypomnień (Seniorm2) w Firebase Firestore.
 *
 * Zapewnia metody do pobierania, dodawania, aktualizacji oraz usuwania przypomnień
 * z kolekcji "Przypomnienia".
 */
class Seniorm2Repository {
    private val db = FirebaseFirestore.getInstance()
    private val collectionPath = "Przypomnienia"

    /**
     * Pobiera listę wszystkich przypomnień cyklicznych z chmury.
     *
     * @return Lista obiektów [Seniorm2Item] pobrana z bazy.
     */
    suspend fun getReminders(): List<Seniorm2Item> {
        val result = db.collection(collectionPath).get().await()
        return result.documents.mapNotNull { doc ->
            val name = doc.getString("name") ?: ""
            val time = doc.getString("time") ?: ""
            val daysStrings = doc.get("daysOfWeek") as? List<String> ?: emptyList()
            val days = daysStrings.map { DayOfWeek.valueOf(it) }.toSet()
            Seniorm2Item(id = doc.id, name = name, time = time, daysOfWeek = days)
        }
    }

    /**
     * Dodaje nowe przypomnienie do kolekcji w Firestore.
     *
     * @param name Nazwa przypomnienia (np. "Leki").
     * @param time Godzina przypomnienia w formacie "HH:mm".
     * @param daysOfWeek Zbiór dni tygodnia, w których przypomnienie ma wystąpić.
     * @return Wygenerowany identyfikator (ID) nowego dokumentu.
     */
    suspend fun addReminder(name: String, time: String, daysOfWeek: Set<DayOfWeek>): String {
        val data = hashMapOf(
            "name" to name,
            "time" to time,
            "daysOfWeek" to daysOfWeek.map { it.name }
        )
        val ref = db.collection(collectionPath).add(data).await()
        return ref.id
    }

    /**
     * Aktualizuje dane istniejącego przypomnienia w Firestore.
     *
     * @param item Obiekt [Seniorm2Item] zawierający zaktualizowane dane.
     */
    suspend fun updateReminder(item: Seniorm2Item) {
        val data = hashMapOf(
            "name" to item.name,
            "time" to item.time,
            "daysOfWeek" to item.daysOfWeek.map { it.name }
        )
        db.collection(collectionPath).document(item.id).set(data).await()
    }

    /**
     * Usuwa wybrane przypomnienie z bazy danych Firestore.
     *
     * @param id Unikalny identyfikator dokumentu do usunięcia.
     */
    suspend fun deleteReminder(id: String) {
        db.collection(collectionPath).document(id).delete().await()
    }
}
