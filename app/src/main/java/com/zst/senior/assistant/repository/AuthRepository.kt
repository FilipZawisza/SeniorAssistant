package com.zst.senior.assistant.repository

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zst.senior.assistant.model.AuthState
import kotlinx.coroutines.tasks.await

/**
 * Repozytorium obsługujące logikę uwierzytelniania i profilu użytkownika.
 *
 * Klasa zarządza integracją z Firebase Auth oraz Firestore, a także lokalnym
 * przechowywaniem sesji w zaszyfrowanych preferencjach.
 *
 * @property context Kontekst aplikacji wymagany do inicjalizacji EncryptedSharedPreferences.
 */
class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val PREFS_NAME = "senior_assistant_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }

    /**
     * Zwraca aktualnie zalogowanego użytkownika Firebase.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Rejestruje nowego użytkownika i tworzy dokument w Firestore.
     *
     * @param email Email użytkownika.
     * @param pass Hasło.
     * @param imie Imię.
     * @param nazwisko Nazwisko.
     * @param ulica Ulica.
     * @param miasto Miasto.
     * @param rola Rola użytkownika ("Senior", "Wolontariusz", "Admin").
     * @param telefon Numer telefonu.
     * @return [AuthState] reprezentujący wynik operacji.
     */
    suspend fun registerUser(
        email: String,
        pass: String,
        imie: String,
        nazwisko: String,
        ulica: String,
        miasto: String,
        rola: String,
        telefon: String
    ): AuthState {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user!!

            try {
                user.sendEmailVerification().await()
            } catch (e: Exception) {
                // Ignorujemy błąd wysyłki e-maila weryfikacyjnego
            }

            val userProfile = hashMapOf(
                "Imie" to imie,
                "Nazwisko" to nazwisko,
                "Ulica" to ulica,
                "Miasto" to miasto,
                "Rola" to rola,
                "Email" to email,
                "Telefon" to telefon,
                "IsBanned" to false,
                "BanUntil" to null
            )
            db.collection(rola).document(user.uid).set(userProfile).await()

            auth.signOut()
            clearLoginState()
            AuthState.RegisterSuccess
        } catch (e: Exception) {
            AuthState.Error("Błąd rejestracji: ${e.message}")
        }
    }

    /**
     * Loguje użytkownika przy użyciu adresu email i hasła.
     *
     * @param email Email.
     * @param pass Hasło.
     * @return [AuthState] reprezentujący wynik operacji lub błąd.
     */
    suspend fun loginUser(email: String, pass: String): AuthState {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: return AuthState.LoginError("Nie udało się pobrać danych użytkownika.")

            user.reload().await()
            if (!user.isEmailVerified) {
                try {
                    user.sendEmailVerification().await()
                } catch (e: Exception) {}
                auth.signOut()
                clearLoginState()
                AuthState.LoginError("Konto nie zostało zweryfikowane. Wysłano nowy link aktywacyjny na Twój e-mail.")
            } else {
                fetchUserDetails(user)
            }
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> "Nie znaleziono konta z takim adresem e-mail."
                is FirebaseAuthInvalidCredentialsException -> "Nieprawidłowy adres e-mail lub hasło."
                is FirebaseNetworkException -> "Brak połączenia z internetem. Sprawdź swoje połączenie."
                else -> "Wystąpił błąd logowania: ${e.message}"
            }
            AuthState.LoginError(errorMessage)
        }
    }

    /**
     * Pobiera szczegóły profilu użytkownika z Firestore.
     *
     * @param user Użytkownik Firebase.
     * @return [AuthState] wynikowy (Success lub Error).
     */
    suspend fun fetchUserDetails(user: FirebaseUser): AuthState {
        val collections = listOf("Senior", "Wolontariusz", "Admin")
        var userRoleFound: String? = null
        var userDocFound: DocumentSnapshot? = null

        for (collection in collections) {
            val doc = db.collection(collection).document(user.uid).get().await()
            if (doc.exists()) {
                userRoleFound = collection
                userDocFound = doc
                break
            }
        }

        return if (userRoleFound != null && userDocFound != null) {
            val isBanned = userDocFound.getBoolean("IsBanned") ?: false
            val banUntil = userDocFound.getLong("BanUntil")
            if (isBanned) {
                if (banUntil == null || System.currentTimeMillis() < banUntil) {
                    val msg = if (banUntil == null) "Konto zablokowane na stałe."
                    else "Konto zablokowane jeszcze przez ${(banUntil - System.currentTimeMillis()) / 86400000} dni."
                    auth.signOut()
                    clearLoginState()
                    return AuthState.LoginError(msg)
                }
            }

            val name = "${userDocFound.getString("Imie")} ${userDocFound.getString("Nazwisko")}".trim()
            val email = userDocFound.getString("Email") ?: user.email ?: ""
            val displayName = if (name.isNotBlank()) name else "Użytkownik"

            saveLoginState(user.uid, userRoleFound, displayName, email)
            AuthState.LoginSuccess(userRoleFound, user.uid)
        } else {
            auth.signOut()
            clearLoginState()
            AuthState.LoginError("Nie znaleziono profilu.")
        }
    }

    /**
     * Inicjuje procedurę resetowania hasła.
     *
     * @param email Email konta do zresetowania.
     * @return [AuthState] reprezentujący sukces lub błąd.
     */
    suspend fun resetPassword(email: String): AuthState {
        return try {
            val collections = listOf("Senior", "Wolontariusz", "Admin")
            var emailExists = false

            for (collection in collections) {
                val query = db.collection(collection).whereEqualTo("Email", email).limit(1).get().await()
                if (!query.isEmpty) {
                    emailExists = true
                    break
                }
            }

            if (emailExists) {
                auth.sendPasswordResetEmail(email).await()
                AuthState.Success("Link do resetu hasła został wysłany na podany adres.")
            } else {
                AuthState.Error("Nie znaleziono konta powiązanego z tym adresem e-mail.")
            }
        } catch (e: Exception) {
            AuthState.Error("Błąd podczas resetowania hasła: ${e.message}")
        }
    }

    /**
     * Nakłada blokadę na użytkownika o podanym adresie email.
     *
     * @param email Email użytkownika.
     * @param days Liczba dni blokady (null dla permanentnej).
     * @return [AuthState] z informacją o wyniku.
     */
    suspend fun banUserByEmail(email: String, days: Int?): AuthState {
        return try {
            val collections = listOf("Senior", "Wolontariusz", "Admin")
            var found = false
            for (collection in collections) {
                val query = db.collection(collection).whereEqualTo("Email", email).limit(1).get().await()
                if (!query.isEmpty) {
                    val banUntil = if (days != null) System.currentTimeMillis() + (days.toLong() * 86400000) else null
                    query.documents.first().reference.update("IsBanned", true, "BanUntil", banUntil).await()
                    found = true
                    break
                }
            }
            if (found) AuthState.Success("Użytkownik zablokowany.")
            else AuthState.Error("Nie znaleziono użytkownika.")
        } catch (e: Exception) {
            AuthState.Error(e.message ?: "Błąd podczas blokowania użytkownika.")
        }
    }

    /**
     * Odblokowuje użytkownika o podanym adresie email.
     *
     * @param email Email użytkownika.
     * @return [AuthState] z informacją o wyniku.
     */
    suspend fun unbanUserByEmail(email: String): AuthState {
        return try {
            val collections = listOf("Senior", "Wolontariusz", "Admin")
            var found = false
            for (collection in collections) {
                val query = db.collection(collection).whereEqualTo("Email", email).limit(1).get().await()
                if (!query.isEmpty) {
                    query.documents.first().reference.update("IsBanned", false, "BanUntil", null).await()
                    found = true
                    break
                }
            }
            if (found) AuthState.Success("Użytkownik odblokowany.")
            else AuthState.Error("Nie znaleziono użytkownika.")
        } catch (e: Exception) {
            AuthState.Error(e.message ?: "Błąd podczas odblokowywania użytkownika.")
        }
    }

    /**
     * Sprawdza czy użytkownik o danym emailu jest zablokowany.
     *
     * @param email Email użytkownika.
     * @return true jeśli jest zablokowany, false w przeciwnym razie.
     */
    suspend fun isUserBanned(email: String): Boolean {
        val collections = listOf("Senior", "Wolontariusz", "Admin")
        for (collection in collections) {
            val query = db.collection(collection).whereEqualTo("Email", email).limit(1).get().await()
            if (!query.isEmpty) {
                val doc = query.documents.first()
                val isBanned = doc.getBoolean("IsBanned") ?: false
                val banUntil = doc.getLong("BanUntil")
                if (isBanned && (banUntil == null || System.currentTimeMillis() < banUntil)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Wylogowuje użytkownika i czyści dane sesji.
     */
    fun logout() {
        auth.signOut()
        clearLoginState()
    }

    private fun getEncryptedPrefs(): android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun saveLoginState(userId: String, role: String, userName: String, email: String) {
        getEncryptedPrefs().edit {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, role)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_EMAIL, email)
        }
    }

    /**
     * Czyści dane sesji z zaszyfrowanych preferencji.
     */
    fun clearLoginState() {
        getEncryptedPrefs().edit {
            remove(KEY_USER_ID)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
        }
    }

    /**
     * Pobiera zapisane dane sesji.
     * @return Mapa z danymi sesji.
     */
    fun getSavedSession(): Map<String, String?> {
        val prefs = getEncryptedPrefs()
        return mapOf(
            KEY_USER_ID to prefs.getString(KEY_USER_ID, null),
            KEY_USER_ROLE to prefs.getString(KEY_USER_ROLE, null),
            KEY_USER_NAME to prefs.getString(KEY_USER_NAME, null),
            KEY_USER_EMAIL to prefs.getString(KEY_USER_EMAIL, null)
        )
    }
}
