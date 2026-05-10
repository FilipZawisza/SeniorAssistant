package com.zst.senior.assistant.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repozytorium obsługujące operacje na profilu użytkownika w Firebase Auth i Firestore.
 */
class UserProfileRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Pobiera dokument profilu z Firestore.
     *
     * @param role Rola użytkownika (kolekcja).
     * @param userId ID użytkownika.
     * @return Mapa danych profilu lub null.
     */
    suspend fun getProfileData(role: String, userId: String): Map<String, Any>? {
        val doc = db.collection(role).document(userId).get().await()
        return doc.data
    }

    /**
     * Zapisuje dane profilu w Firestore (merge).
     *
     * @param role Rola użytkownika.
     * @param userId ID użytkownika.
     * @param data Mapa danych do zapisu.
     */
    suspend fun saveProfileData(role: String, userId: String, data: Map<String, Any>) {
        db.collection(role).document(userId).set(data, SetOptions.merge()).await()
    }

    /**
     * Aktualizuje adres e-mail (wymaga weryfikacji).
     *
     * @param newEmail Nowy e-mail.
     */
    suspend fun updateEmail(newEmail: String) {
        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
    }

    /**
     * Wysyła link do resetowania hasła na aktualny adres e-mail.
     */
    suspend fun sendPasswordReset() {
        val email = auth.currentUser?.email ?: throw Exception("Brak e-maila")
        auth.sendPasswordResetEmail(email).await()
    }

    /**
     * Usuwa konto zalogowanego użytkownika.
     */
    suspend fun deleteAccount() {
        auth.currentUser?.delete()?.await()
    }

    /**
     * Tworzy Flow nasłuchujący na zmiany punktów w profilu użytkownika.
     *
     * @param role Rola użytkownika.
     * @param userId ID użytkownika.
     * @return Flow z aktualną liczbą punktów.
     */
    fun observePoints(role: String, userId: String): Flow<Int> = callbackFlow {
        val registration = db.collection(role).document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val points = snapshot.getLong("punktyGryUmyslowe")?.toInt() ?: 0
                    trySend(points)
                }
            }
        awaitClose { registration.remove() }
    }
}
