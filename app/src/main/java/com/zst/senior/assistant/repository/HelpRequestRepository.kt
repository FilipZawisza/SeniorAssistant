package com.zst.senior.assistant.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.telephony.SmsManager
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.zst.senior.assistant.model.PelneZlecenie
import com.zst.senior.assistant.model.PelneZlecenieDlaSeniora
import com.zst.senior.assistant.model.PelneZlecenieDlaWolontariusza
import com.zst.senior.assistant.model.ZlecenieRef
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Repozytorium zarządzające zleceniami pomocy (Help Requests).
 *
 * Odpowiada za operacje na kolekcji "Zlecenia" w Firestore, geokodowanie adresów,
 * wysyłanie powiadomień SMS oraz nasłuchiwanie nowych zleceń w czasie rzeczywistym.
 */
class HelpRequestRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Pobiera listę zleceń na podstawie statusów.
     *
     * @param statuses Lista statusów do przefiltrowania.
     * @return Lista obiektów [ZlecenieRef].
     */
    suspend fun getZleceniaByStatus(statuses: List<String>): List<ZlecenieRef> {
        val result = db.collection("Zlecenia")
            .whereIn("Status", statuses)
            .get()
            .await()

        return result.map { doc ->
            ZlecenieRef(
                id = doc.id,
                seniorId = doc.getString("SeniorID"),
                wolontariuszId = doc.getString("WolontariuszID"),
                opis = doc.getString("Zlecenie"),
                status = doc.getString("Status") ?: "Wolne",
                seniorLat = doc.getDouble("seniorLat"),
                seniorLng = doc.getDouble("seniorLng"),
                timestamp = extractTimestamp(doc)
            )
        }
    }

    /**
     * Pobiera publiczne zlecenia (status "Wolne") wraz z miastem seniora.
     *
     * @return Lista wzbogaconych obiektów [ZlecenieRef].
     */
    suspend fun getPublicZlecenia(): List<ZlecenieRef> {
        val result = db.collection("Zlecenia")
            .whereEqualTo("Status", "Wolne")
            .get()
            .await()

        val seniorTasks = mutableMapOf<String, Task<DocumentSnapshot>>()
        for (doc in result.documents) {
            val seniorId = doc.getString("SeniorID")
            if (seniorId != null && !seniorTasks.containsKey(seniorId)) {
                seniorTasks[seniorId] = db.collection("Senior").document(seniorId).get()
            }
        }

        Tasks.whenAllSuccess<DocumentSnapshot>(seniorTasks.values).await()
        val seniorDataMap = seniorTasks.mapValues { it.value.result.getString("Miasto") ?: "Nieznane" }

        return result.documents.map { doc ->
            val sId = doc.getString("SeniorID")
            ZlecenieRef(
                id = doc.id,
                seniorId = sId,
                wolontariuszId = doc.getString("WolontariuszID"),
                opis = doc.getString("Zlecenie"),
                status = doc.getString("Status") ?: "Wolne",
                seniorLat = doc.getDouble("seniorLat"),
                seniorLng = doc.getDouble("seniorLng"),
                miasto = seniorDataMap[sId] ?: "Nieznane",
                timestamp = extractTimestamp(doc)
            )
        }
    }

    /**
     * Tworzy nowe zlecenie pomocy.
     *
     * @param context Kontekst do geokodowania i wysyłki SMS.
     * @param seniorId ID seniora tworzącego zlecenie.
     * @param opis Opis zlecenia.
     * @param location Opcjonalna lokalizacja GPS.
     */
    suspend fun createZlecenie(context: Context, seniorId: String, opis: String, location: Location?) {
        val seniorDoc = db.collection("Senior").document(seniorId).get().await()
        val imie = seniorDoc.getString("Imie") ?: "Senior"
        val nazwisko = seniorDoc.getString("Nazwisko") ?: ""
        val numerOpiekuna = seniorDoc.getString("Telefon")

        var lat: Double? = location?.latitude
        var lng: Double? = location?.longitude

        if (lat == null || lng == null) {
            val ulica = seniorDoc.getString("Ulica") ?: ""
            val miasto = seniorDoc.getString("Miasto") ?: ""
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName("$ulica, $miasto", 1)
                if (!addresses.isNullOrEmpty()) {
                    lat = addresses[0].latitude
                    lng = addresses[0].longitude
                }
            } catch (e: Exception) {
                Log.e("GPS", "Błąd Geokodowania: ${e.message}")
            }
        }

        val noweZlecenie = hashMapOf(
            "SeniorID" to seniorId,
            "Zlecenie" to opis,
            "Status" to "Wolne",
            "WolontariuszID" to null,
            "seniorLat" to lat,
            "seniorLng" to lng,
            "Timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("Zlecenia").add(noweZlecenie).await()

        if (!numerOpiekuna.isNullOrBlank()) {
            sendSms(context, numerOpiekuna, "ASYSTENT SENIORA: $imie $nazwisko poprosił o pomoc. Kategoria: $opis")
        }
    }

    private fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
        } catch (e: Exception) {
            Log.e("SMS", "Błąd wysyłania SMS: ${e.message}")
        }
    }

    /**
     * Pobiera pełne dane o konkretnym zleceniu.
     *
     * @param context Kontekst do geokodowania adresu.
     * @param zlecenieId ID zlecenia.
     * @return Obiekt [PelneZlecenie].
     */
    suspend fun getPelneZlecenie(context: Context, zlecenieId: String): PelneZlecenie {
        val zlecenieDoc = db.collection("Zlecenia").document(zlecenieId).get(Source.SERVER).await()
        if (!zlecenieDoc.exists()) throw Exception("Zlecenie nie istnieje.")

        val seniorId = zlecenieDoc.getString("SeniorID") ?: throw Exception("Brak SeniorID.")
        val sLat = zlecenieDoc.getDouble("seniorLat")
        val sLng = zlecenieDoc.getDouble("seniorLng")

        val seniorDoc = db.collection("Senior").document(seniorId).get().await()
        val seniorImie = seniorDoc.getString("Imie") ?: "Nieznane"
        val seniorNazwisko = seniorDoc.getString("Nazwisko") ?: "Nieznane"

        var adresAktualny: String? = null
        if (sLat != null && sLng != null) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(sLat, sLng, 1)
                adresAktualny = addresses?.firstOrNull()?.getAddressLine(0)
            } catch (e: Exception) {}
        }

        return PelneZlecenie(
            zlecenieId = zlecenieId,
            seniorImieNazwisko = "$seniorImie $seniorNazwisko",
            adresZProfilu = "${seniorDoc.getString("Ulica")}, ${seniorDoc.getString("Miasto")}",
            adresAktualny = adresAktualny ?: "GPS: $sLat, $sLng",
            seniorTelefon = seniorDoc.getString("Telefon") ?: "Brak numeru",
            opisZlecenia = zlecenieDoc.getString("Zlecenie") ?: "Brak opisu",
            status = zlecenieDoc.getString("Status") ?: "Wolne",
            seniorId = seniorId,
            timestamp = extractTimestamp(zlecenieDoc)
        )
    }

    /**
     * Wolontariusz podejmuje zlecenie.
     *
     * @param zlecenieId ID zlecenia.
     * @param wolontariuszId ID wolontariusza.
     */
    suspend fun acceptZlecenie(zlecenieId: String, wolontariuszId: String) {
        val myActiveZlecenia = db.collection("Zlecenia")
            .whereEqualTo("WolontariuszID", wolontariuszId)
            .whereEqualTo("Status", "Aktywne")
            .get()
            .await()

        if (myActiveZlecenia.size() >= 1) {
            throw Exception("Osiągnięto limit 1 aktywnego zlecenia.")
        }

        db.collection("Zlecenia").document(zlecenieId)
            .update("Status", "Aktywne", "WolontariuszID", wolontariuszId)
            .await()
    }

    /**
     * Aktualizuje status zlecenia w bazie danych.
     *
     * @param zlecenieId ID dokumentu zlecenia.
     * @param newStatus Nowy status do ustawienia (np. "Zakonczone").
     */
    suspend fun updateZlecenieStatus(zlecenieId: String, newStatus: String) {
        db.collection("Zlecenia").document(zlecenieId).update("Status", newStatus).await()
    }

    /**
     * Kończy zlecenie z wystawieniem oceny i aktualizacją statystyk wolontariusza.
     *
     * @param zlecenieId ID zlecenia, które zostało ukończone.
     * @param rating Ocena wystawiona przez seniora (skala 1-5).
     */
    suspend fun confirmZlecenieWithRating(zlecenieId: String, rating: Int) {
        val zlecenieDoc = db.collection("Zlecenia").document(zlecenieId).get().await()
        val wolontariuszId = zlecenieDoc.getString("WolontariuszID")
        val seniorId = zlecenieDoc.getString("SeniorID")

        db.collection("Zlecenia").document(zlecenieId).update("Status", "Zakonczone").await()

        if (wolontariuszId != null && seniorId != null) {
            val opinia = hashMapOf(
                "ZlecenieID" to zlecenieId,
                "SeniorID" to seniorId,
                "WolontariuszID" to wolontariuszId,
                "Ocena" to rating,
                "Data" to System.currentTimeMillis()
            )
            db.collection("Opinie").add(opinia).await()

            val wolontariuszRef = db.collection("Wolontariusz").document(wolontariuszId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(wolontariuszRef)
                val count = (snapshot.get("iloscZlecen") as? Number)?.toLong() ?: 0L
                val average = (snapshot.get("sredniaOcena") as? Number)?.toDouble() ?: 0.0

                val newCount = count + 1
                val newAverage = ((average * count) + rating) / newCount

                transaction.update(wolontariuszRef, "iloscZlecen", newCount)
                transaction.update(wolontariuszRef, "sredniaOcena", newAverage)
            }.await()
        }
    }

    /**
     * Wysyła powiadomienie do użytkownika poprzez zapis w kolekcji "Powiadomienia".
     *
     * @param receiverId ID użytkownika odbierającego powiadomienie.
     * @param title Tytuł powiadomienia.
     * @param content Treść powiadomienia.
     */
    suspend fun sendNotification(receiverId: String, title: String, content: String) {
        val notification = hashMapOf(
            "OdbiorcaID" to receiverId,
            "Tytul" to title,
            "Tresc" to content,
            "Data" to System.currentTimeMillis(),
            "Przeczyatne" to false
        )
        db.collection("Powiadomienia").add(notification).await()
    }

    /**
     * Pobiera zlecenia przypisane do konkretnego seniora.
     *
     * @param seniorId ID seniora, dla którego pobierane są zlecenia.
     * @return Lista obiektów [PelneZlecenieDlaSeniora] zawierająca dane o zleceniach i wolontariuszach.
     */
    suspend fun getZleceniaForSenior(seniorId: String): List<PelneZlecenieDlaSeniora> {
        val query = db.collection("Zlecenia")
            .whereEqualTo("SeniorID", seniorId)
            .whereIn("Status", listOf("Wolne", "Aktywne", "DoPotwierdzenia"))
            .get()
            .await()

        return query.documents.map { doc ->
            val wolontariuszId = doc.getString("WolontariuszID")
            val status = doc.getString("Status") ?: "Wolne"
            
            var wolontariuszName = "Oczekuje na wolontariusza"
            var wolontariuszPhone = "---"
            var wolontariuszOcena = 0.0
            var wolontariuszLiczbaZlecen = 0

            if (status != "Wolne" && wolontariuszId != null) {
                val wDoc = db.collection("Wolontariusz").document(wolontariuszId).get().await()
                wolontariuszName = "${wDoc.getString("Imie")} ${wDoc.getString("Nazwisko")}"
                wolontariuszPhone = wDoc.getString("Telefon") ?: "Brak"
                wolontariuszOcena = (wDoc.get("sredniaOcena") as? Number)?.toDouble() ?: 0.0
                wolontariuszLiczbaZlecen = (wDoc.get("iloscZlecen") as? Number)?.toInt() ?: 0
            }

            PelneZlecenieDlaSeniora(
                zlecenieId = doc.id,
                opis = doc.getString("Zlecenie") ?: "",
                status = status,
                wolontariuszImieNazwisko = wolontariuszName,
                wolontariuszTelefon = wolontariuszPhone,
                wolontariuszOcena = wolontariuszOcena,
                wolontariuszLiczbaZlecen = wolontariuszLiczbaZlecen,
                timestamp = extractTimestamp(doc)
            )
        }
    }

    /**
     * Pobiera zlecenia przypisane do wolontariusza wraz z danymi adresowymi seniorów.
     *
     * @param context Kontekst używany do geokodowania adresu na podstawie współrzędnych.
     * @param volunteerId ID wolontariusza, dla którego pobierane są aktywne zlecenia.
     * @return Lista obiektów [PelneZlecenieDlaWolontariusza].
     */
    suspend fun getZleceniaForVolunteer(context: Context, volunteerId: String): List<PelneZlecenieDlaWolontariusza> {
        val query = db.collection("Zlecenia")
            .whereEqualTo("WolontariuszID", volunteerId)
            .whereIn("Status", listOf("Aktywne", "DoPotwierdzenia"))
            .get()
            .await()

        val seniorTasks = mutableMapOf<String, Task<DocumentSnapshot>>()
        for (doc in query.documents) {
            val sId = doc.getString("SeniorID")
            if (sId != null && !seniorTasks.containsKey(sId)) {
                seniorTasks[sId] = db.collection("Senior").document(sId).get()
            }
        }
        Tasks.whenAllSuccess<DocumentSnapshot>(seniorTasks.values).await()

        return query.documents.map { doc ->
            val sId = doc.getString("SeniorID")
            val seniorDoc = seniorTasks[sId]?.result
            val sLat = doc.getDouble("seniorLat")
            val sLng = doc.getDouble("seniorLng")
            
            var currentAddress: String? = null
            if (sLat != null && sLng != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    currentAddress = geocoder.getFromLocation(sLat, sLng, 1)?.firstOrNull()?.getAddressLine(0)
                } catch (e: Exception) {}
            }

            PelneZlecenieDlaWolontariusza(
                zlecenieId = doc.id,
                opis = doc.getString("Zlecenie") ?: "",
                seniorImieNazwisko = "${seniorDoc?.getString("Imie")} ${seniorDoc?.getString("Nazwisko")}",
                seniorAdresStały = "${seniorDoc?.getString("Ulica")}, ${seniorDoc?.getString("Miasto")}",
                seniorAdresAktualny = currentAddress ?: "GPS: $sLat, $sLng",
                seniorTelefon = seniorDoc?.getString("Telefon") ?: "Brak",
                status = doc.getString("Status") ?: "Aktywne",
                timestamp = extractTimestamp(doc)
            )
        }
    }

    /**
     * Zwraca strumień nowych, wolnych zleceń pojawiających się w systemie w czasie rzeczywistym.
     *
     * @return Flow emitujący zmiany w dokumentach (tylko nowe zlecenia o statusie "Wolne").
     */
    fun observeNewZlecenia(): Flow<DocumentChange> = callbackFlow {
        val listener = db.collection("Zlecenia")
            .whereEqualTo("Status", "Wolne")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshots?.documentChanges?.filter { it.type == DocumentChange.Type.ADDED }?.forEach {
                    trySend(it)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Odnotowuje odrzucenie wykonania zlecenia przez seniora.
     * Zlecenie wraca do puli wolnych, a wolontariusz otrzymuje powiadomienie.
     *
     * @param zlecenieId ID zlecenia.
     */
    suspend fun rejectExecution(zlecenieId: String) {
        val doc = db.collection("Zlecenia").document(zlecenieId).get().await()
        val wolontariuszId = doc.getString("WolontariuszID")

        db.collection("Zlecenia").document(zlecenieId).update(
            mapOf("Status" to "Wolne", "WolontariuszID" to null)
        ).await()

        if (wolontariuszId != null) {
            sendNotification(
                wolontariuszId,
                "Odrzucenie wykonania zlecenia",
                "Senior zgłosił, że nie wykonałeś zlecenia, mimo zmiany jego statusu. Oszukiwanie w systemie obniża Twoją reputację!"
            )
        }
    }

    /**
     * Odnotowuje anulowanie zlecenia przez seniora.
     *
     * @param zlecenieId ID zlecenia.
     */
    suspend fun cancelZlecenie(zlecenieId: String) {
        val doc = db.collection("Zlecenia").document(zlecenieId).get().await()
        val status = doc.getString("Status")
        val wolontariuszId = doc.getString("WolontariuszID")

        db.collection("Zlecenia").document(zlecenieId).update("Status", "Anulowane").await()

        if (status == "Aktywne" && wolontariuszId != null) {
            sendNotification(
                wolontariuszId,
                "Zlecenie anulowane",
                "Senior anulował zlecenie, które przyjąłeś do realizacji."
            )
        }
    }

    /**
     * Odnotowuje brak stawienia się wolontariusza.
     *
     * @param zlecenieId ID zlecenia.
     */
    suspend fun reportNoShow(zlecenieId: String) {
        val doc = db.collection("Zlecenia").document(zlecenieId).get().await()
        val wolontariuszId = doc.getString("WolontariuszID")

        db.collection("Zlecenia").document(zlecenieId).update("Status", "Niezrealizowane").await()

        if (wolontariuszId != null) {
            sendNotification(
                wolontariuszId,
                "Ostrzeżenie: Brak realizacji zlecenia",
                "Senior zgłosił, że nie pojawiłeś się, aby pomóc w zleceniu. To obniża Twoją reputację."
            )
        }
    }

    /**
     * Usuwa zlecenie z bazy danych.
     *
     * @param zlecenieId ID zlecenia do usunięcia.
     */
    suspend fun deleteZlecenie(zlecenieId: String) {
        db.collection("Zlecenia").document(zlecenieId).delete().await()
    }

    private fun extractTimestamp(doc: DocumentSnapshot): Long {
        return try {
            val ts = doc.get("Timestamp")
            when (ts) {
                is Timestamp -> ts.toDate().time
                is Long -> ts
                is Double -> ts.toLong()
                else -> 0L
            }
        } catch (e: Exception) { 0L }
    }
}
