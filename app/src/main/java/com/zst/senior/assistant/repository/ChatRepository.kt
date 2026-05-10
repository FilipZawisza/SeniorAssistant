package com.zst.senior.assistant.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zst.senior.assistant.viewmodel.ChatMessage
import com.zst.senior.assistant.utils.EncryptionUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

/**
 * Repozytorium obsługujące komunikację z Firebase Firestore w zakresie globalnego czatu.
 *
 * Klasa separuje logikę dostępu do danych (pobieranie, wysyłanie, moderacja) od ViewModelu,
 * zapewniając czystość architektury i łatwiejsze testowanie. Automatycznie szyfruje
 * i deszyfruje treści wiadomości w locie.
 */
class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val chatCollection = db.collection("Czat")
    private val mutedCollection = db.collection("MutedUsers")

    /**
     * Zwraca strumień (Flow) wiadomości z ostatnich 7 dni w czasie rzeczywistym.
     *
     * @return Flow zawierający listę deszyfrowanych obiektów [ChatMessage].
     */
    fun getMessagesFlow(): Flow<List<ChatMessage>> = callbackFlow {
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }

        val listener = chatCollection
            .whereGreaterThan("timestamp", Timestamp(sevenDaysAgo.time))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val messages = snapshots.documents.mapNotNull { doc ->
                        val msg = doc.toObject(ChatMessage::class.java)
                        msg?.copy(
                            id = doc.id,
                            text = EncryptionUtils.decrypt(msg.text)
                        )
                    }
                    trySend(messages)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Szyfruje i zapisuje nową wiadomość w chmurze Firestore.
     *
     * @param text Jawna treść wiadomości.
     * @param userName Nazwa nadawcy.
     * @param userEmail Adres e-mail nadawcy do celów moderacji.
     * @throws Exception W przypadku błędu połączenia z bazą.
     */
    suspend fun sendMessage(text: String, userName: String, userEmail: String) {
        val encryptedText = EncryptionUtils.encrypt(text)
        val messageData = hashMapOf(
            "text" to encryptedText,
            "userName" to userName,
            "userEmail" to userEmail,
            "timestamp" to Timestamp(Date())
        )
        chatCollection.add(messageData).await()
    }

    /**
     * Usuwa wskazaną wiadomość z bazy danych.
     *
     * @param messageId ID dokumentu w Firestore.
     */
    suspend fun deleteMessage(messageId: String) {
        chatCollection.document(messageId).delete().await()
    }

    /**
     * Nakłada czasową blokadę pisania (mute) na użytkownika.
     *
     * @param email E-mail użytkownika do wyciszenia.
     * @param hours Czas trwania blokady w godzinach.
     */
    suspend fun muteUser(email: String, hours: Int) {
        val muteUntil = System.currentTimeMillis() + (hours * 3600000L)
        mutedCollection.document(email).set(hashMapOf("muteUntil" to muteUntil)).await()
    }

    /**
     * Zdejmuje blokadę pisania z użytkownika.
     *
     * @param email E-mail użytkownika do odblokowania.
     */
    suspend fun unmuteUser(email: String) {
        mutedCollection.document(email).delete().await()
    }

    /**
     * Sprawdza, czy dany użytkownik jest aktualnie wyciszony.
     *
     * @param email E-mail sprawdzanego użytkownika.
     * @return true jeśli blokada jest wciąż aktywna, false w przeciwnym razie.
     */
    suspend fun isUserMuted(email: String): Boolean {
        return try {
            val doc = mutedCollection.document(email).get().await()
            val muteUntil = doc.getLong("muteUntil") ?: 0L
            System.currentTimeMillis() < muteUntil
        } catch (e: Exception) {
            false
        }
    }
}
