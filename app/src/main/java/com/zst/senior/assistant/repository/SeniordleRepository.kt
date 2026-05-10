package com.zst.senior.assistant.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Repozytorium obsługujące system punktacji w grze Seniordle.
 *
 * Klasa odpowiada za aktualizację wyników użytkownika w bazie danych Firebase Firestore
 * po zakończeniu rozgrywki w grę słowną.
 */
class SeniordleRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Dodaje punkty za wygraną grę do profilu Seniora w Firestore.
     *
     * @param points Liczba punktów do dodania do aktualnego wyniku użytkownika.
     */
    suspend fun addSeniordlePoints(points: Long) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("Senior").document(uid)
                .update("punktyGryUmyslowe", FieldValue.increment(points))
                .await()
        } catch (e: Exception) {
            Log.e("SeniordleRepository", "Błąd aktualizacji punktów: ${e.message}")
        }
    }
}
