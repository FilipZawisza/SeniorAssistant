package com.zst.senior.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.zst.senior.assistant.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Zapieczętowana klasa (Sealed Class) reprezentująca stany interfejsu użytkownika (UI State)
 * dla ekranu zarządzania profilem.
 */
sealed class ProfileState {
    /** Stan początkowy, brak aktywności. */
    object Idle : ProfileState()
    /** Trwa ładowanie danych lub wykonywanie operacji w tle. */
    object Loading : ProfileState()
    /** Pomyślnie załadowano dane profilu. Zawiera mapę z informacjami o użytkowniku. */
    data class Success(val data: Map<String, Any>) : ProfileState()
    /** Wystąpił błąd operacji. Zawiera czytelny komunikat błędu. */
    data class Error(val message: String) : ProfileState()
    /** Pomyślnie wysłano żądanie zmiany adresu e-mail (wymaga weryfikacji). */
    data class EmailUpdateSuccess(val message: String) : ProfileState()
    /** Pomyślnie wysłano link do resetowania hasła. */
    data class PasswordResetSent(val message: String) : ProfileState()
    /** Konto użytkownika zostało bezpowrotnie usunięte. */
    object AccountDeleteSuccess : ProfileState()
    /** Zmiany w profilu zostały pomyślnie zapisane. */
    object ProfileSaved : ProfileState()
}

/**
 * ViewModel zarządzający profilem użytkownika.
 *
 * Odpowiada za odczyt i aktualizację danych osobowych w bazie za pomocą [UserProfileRepository],
 * zarządzanie ustawieniami autoryzacji oraz nasłuchiwanie w czasie rzeczywistym na zmiany punktów.
 *
 * @property authViewModel ViewModel autoryzacji, dostarczający aktualne ID i rolę użytkownika.
 * @property repository Repozytorium obsługujące operacje na profilu.
 */
class UserProfileViewModel(
    private val authViewModel: AuthViewModel,
    private val repository: UserProfileRepository = UserProfileRepository()
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    /** Strumień przechowujący aktualny stan ekranu profilu. */
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    /** Strumień przechowujący liczbę punktów zdobytych przez użytkownika w minigrach. */
    private val _punktyGry = MutableStateFlow(0)
    val punktyGry: StateFlow<Int> = _punktyGry.asStateFlow()

    init {
        // Reaktywne nasłuchiwanie na zmiany zalogowanego użytkownika i jego roli.
        viewModelScope.launch {
            authViewModel.currentUserId.combine(authViewModel.userRole) { userId, role ->
                userId to role
            }.collect { (userId, role) ->
                if (userId != null && role != null) {
                    loadProfile()
                    // Uruchamiamy reaktywne nasłuchiwanie punktów przez repozytorium
                    launch {
                        repository.observePoints(role, userId).collect { points ->
                            _punktyGry.value = points
                        }
                    }
                } else {
                    _profileState.value = ProfileState.Idle
                    _punktyGry.value = 0
                }
            }
        }
    }

    /**
     * Pobiera aktualne dane profilowe użytkownika z repozytorium.
     * Uzupełnia brakujące pola domyślnymi wartościami.
     */
    fun loadProfile() {
        val userId = authViewModel.currentUserId.value
        val role = authViewModel.userRole.value

        if (userId == null || role == null) {
            _profileState.value = ProfileState.Error("Brak zalogowanego użytkownika.")
            return
        }

        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getProfileData(role, userId)
                val email = auth.currentUser?.email ?: ""
                
                if (data != null) {
                    val profileData = data.toMutableMap()
                    profileData["Email"] = email
                    profileData["role"] = role

                    if (!profileData.containsKey("Telefon")) {
                        profileData["Telefon"] = ""
                    }
                    if (role == "Senior" && !profileData.containsKey("OpiekunNumer")) {
                        profileData["OpiekunNumer"] = ""
                    }
                    _profileState.value = ProfileState.Success(profileData)
                } else {
                    val defaultProfileData = mutableMapOf<String, Any>(
                        "Email" to email,
                        "role" to role,
                        "Imie" to "",
                        "Nazwisko" to "",
                        "Ulica" to "",
                        "Miasto" to "",
                        "Telefon" to ""
                    )
                    if (role == "Senior") {
                        defaultProfileData["OpiekunNumer"] = ""
                    }
                    _profileState.value = ProfileState.Success(defaultProfileData)
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Błąd ładowania profilu: ${e.message}")
            }
        }
    }

    /**
     * Zapisuje lub aktualizuje dane osobowe użytkownika przez repozytorium.
     *
     * @param imie Imię użytkownika.
     * @param nazwisko Nazwisko użytkownika.
     * @param ulica Adres (ulica i numer).
     * @param miasto Miasto zamieszkania.
     * @param telefon Numer telefonu bezpośrednio do użytkownika.
     * @param opiekunNumer (Tylko dla ról Senior) Numer kontaktowy w nagłych wypadkach.
     */
    fun saveProfile(
        imie: String,
        nazwisko: String,
        ulica: String,
        miasto: String,
        telefon: String,
        opiekunNumer: String = ""
    ) {
        val userId = authViewModel.currentUserId.value
        val role = authViewModel.userRole.value

        if (userId == null || role == null) {
            showTemporaryState(ProfileState.Error("Błąd zapisu: Brak użytkownika."))
            return
        }

        if (role == "Senior" && opiekunNumer.trim().isEmpty()) {
            showTemporaryState(ProfileState.Error("Ze względów bezpieczeństwa, numer telefonu opiekuna jest wymagany!"))
            return
        }

        _profileState.value = ProfileState.Loading
        viewModelScope.launch {
            try {
                val profileData = mutableMapOf(
                    "Imie" to imie,
                    "Nazwisko" to nazwisko,
                    "Ulica" to ulica,
                    "Miasto" to miasto,
                    "Telefon" to telefon
                )
                if (role == "Senior") {
                    profileData["OpiekunNumer"] = opiekunNumer
                }
                repository.saveProfileData(role, userId, profileData)
                showTemporaryState(ProfileState.ProfileSaved)
            } catch (e: Exception) {
                showTemporaryState(ProfileState.Error("Błąd zapisu: ${e.message}"))
            }
        }
    }

    /**
     * Rozpoczyna proces zmiany adresu e-mail przypisanego do konta.
     *
     * @param newEmail Nowy adres e-mail.
     */
    fun updateEmailWithVerification(newEmail: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.updateEmail(newEmail)
                _profileState.value = ProfileState.EmailUpdateSuccess("Wysłano link weryfikacyjny na stary adres e-mail w celu potwierdzenia zmiany.")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Błąd podczas aktualizacji adresu e-mail: ${e.message}")
            }
        }
    }

    /**
     * Inicjuje wysyłkę e-maila z linkiem pozwalającym na ustanowienie nowego hasła.
     */
    fun sendPasswordResetEmail() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.sendPasswordReset()
                _profileState.value = ProfileState.PasswordResetSent("Wysłano link do resetowania hasła na Twój e-mail.")
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error("Błąd podczas wysyłania linku: ${e.message}")
            }
        }
    }

    /**
     * Całkowicie usuwa konto z usługi Firebase Authentication przez repozytorium.
     */
    fun deleteUserAccount() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                repository.deleteAccount()
                _profileState.value = ProfileState.AccountDeleteSuccess
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthRecentLoginRequiredException ->
                        "Ta operacja wymaga niedawnego zalogowania. Zaloguj się ponownie i spróbuj jeszcze raz."
                    else ->
                        "Wystąpił błąd podczas usuwania konta: ${e.localizedMessage}"
                }
                _profileState.value = ProfileState.Error(errorMessage)
            }
        }
    }

    /**
     * Wyświetla dany stan przez określony czas (5 sekund), a następnie automatycznie
     * wraca do podglądu danych z bazy (przeładowuje profil).
     *
     * @param state Tymczasowy stan do wyświetlenia.
     */
    private fun showTemporaryState(state: ProfileState) {
        _profileState.value = state
        viewModelScope.launch {
            kotlinx.coroutines.delay(5000)
            loadProfile()
        }
    }

    /**
     * Fabryka umożliwiająca stworzenie [UserProfileViewModel] i przekazanie
     * mu [AuthViewModel] jako zależności przez konstruktor.
     */
    class Factory(private val authViewModel: AuthViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserProfileViewModel(authViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
