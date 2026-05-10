package com.zst.senior.assistant.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zst.senior.assistant.viewmodel.LeaderboardEntry
import kotlinx.coroutines.tasks.await

/**
 * Repozytorium obsługujące ranking użytkowników (Leaderboard).
 *
 * Pobiera dane o najlepszych graczach oraz oblicza pozycję zalogowanego użytkownika.
 */
class LeaderboardRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Pobiera listę 10 najlepszych graczy.
     *
     * @return Lista [LeaderboardEntry] z 10 najlepszymi wynikami.
     */
    suspend fun getTopPlayers(): List<LeaderboardEntry> {
        val top10Snapshot = db.collection("Senior")
            .orderBy("punktyGryUmyslowe", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .await()

        return top10Snapshot.documents.mapIndexed { index, doc ->
            val imieZBazy = doc.getString("Imie") ?: ""
            LeaderboardEntry(
                id = doc.id,
                imie = if (imieZBazy.isNotBlank()) imieZBazy else "Anonim",
                punkty = doc.getLong("punktyGryUmyslowe")?.toInt() ?: 0,
                pozycja = index + 1
            )
        }
    }

    /**
     * Pobiera dane punktowe i pozycję zalogowanego użytkownika.
     *
     * @return Para (Punkty, Pozycja) lub null jeśli nie zalogowano.
     */
    suspend fun getCurrentUserRank(): Pair<Int, Int>? {
        val uid = auth.currentUser?.uid ?: return null
        val userDoc = db.collection("Senior").document(uid).get().await()
        val myPoints = userDoc.getLong("punktyGryUmyslowe")?.toInt() ?: 0

        val countQuery = db.collection("Senior")
            .whereGreaterThan("punktyGryUmyslowe", myPoints)
            .count()
            .get(AggregateSource.SERVER)
            .await()

        val rank = countQuery.count.toInt() + 1
        return Pair(myPoints, rank)
    }
}
