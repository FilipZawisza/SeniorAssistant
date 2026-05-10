package com.zst.senior.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zst.senior.assistant.model.AuthState
import com.zst.senior.assistant.model.AuthUiState
import com.zst.senior.assistant.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel zarządzający procesami autoryzacji oraz profilem użytkownika.
 *
 * Odpowiada za logowanie, rejestrację, resetowanie hasła, weryfikację e-mail oraz sprawdzanie
 * i zarządzanie stanem konta (np. blokady/bany). Korzysta z [AuthRepository] do operacji na danych.
 *
 * @param application Kontekst aplikacji.
 * @property authRepository Repozytorium obsługujące logikę uwierzytelniania.
 */
class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository = AuthRepository(application)
) : AndroidViewModel(application) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    val authUiState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isRestoringSession = MutableStateFlow(true)
    val isRestoringSession: StateFlow<Boolean> = _isRestoringSession.asStateFlow()

    private val _currentUserRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _currentUserRole.asStateFlow()

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    init {
        checkAndRestoreSession()
    }

    /**
     * Rejestruje nowego użytkownika w usłudze Firebase Auth i tworzy jego profil w odpowiedniej kolekcji Firestore.
     *
     * @param email Adres e-mail użytkownika.
     * @param pass Hasło użytkownika.
     * @param imie Imię użytkownika.
     * @param nazwisko Nazwisko użytkownika.
     * @param ulica Ulica zamieszkania.
     * @param miasto Miasto zamieszkania.
     * @param rola Rola w systemie (np. "Senior", "Wolontariusz").
     * @param telefon Numer telefonu kontaktowego.
     */
    fun registerUser(email: String, pass: String, imie: String, nazwisko: String, ulica: String, miasto: String, rola: String, telefon: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.registerUser(email, pass, imie, nazwisko, ulica, miasto, rola, telefon)
            if (result is AuthState.RegisterSuccess) {
                resetFlows()
            }
            _authState.value = result
        }
    }

    /**
     * Loguje użytkownika na podstawie adresu e-mail i hasła.
     *
     * @param email Adres e-mail.
     * @param pass Hasło.
     */
    fun loginUser(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.LoginError("Email i hasło nie mogą być puste.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.loginUser(email, pass)
            if (result is AuthState.LoginSuccess) {
                updateUserFlows()
            }
            _authState.value = result
        }
    }

    /**
     * Blokuje dostęp (banuje) wskazanemu użytkownikowi po adresie e-mail.
     *
     * @param email Adres e-mail użytkownika do zablokowania.
     * @param days Opcjonalny czas trwania blokady w dniach. Jeśli `null`, blokada jest permanentna.
     */
    fun banUserByEmail(email: String, days: Int?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = authRepository.banUserByEmail(email, days)
        }
    }

    /**
     * Odblokowuje użytkownika wyszukując go po adresie e-mail.
     *
     * @param email Adres e-mail zablokowanego użytkownika.
     */
    fun unbanUserByEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = authRepository.unbanUserByEmail(email)
        }
    }

    /**
     * Sprawdza synchronicznie, czy podany adres e-mail posiada aktywną blokadę.
     *
     * @param email Adres e-mail do weryfikacji.
     * @return `true` jeśli użytkownik jest aktualnie zbanowany, `false` w przeciwnym wypadku.
     */
    suspend fun isUserBanned(email: String): Boolean {
        return authRepository.isUserBanned(email)
    }

    /**
     * Inicjuje proces resetowania hasła.
     *
     * @param email E-mail konta, dla którego ma zostać zresetowane hasło.
     */
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            _authState.value = authRepository.resetPassword(email)
        }
    }

    /** Resetuje stan autoryzacji z powrotem do [AuthState.Idle]. */
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Całkowicie wylogowuje użytkownika.
     */
    fun logout() {
        authRepository.logout()
        resetFlows()
        _authState.value = AuthState.Idle
    }

    private fun resetFlows() {
        _currentUserId.value = null
        _currentUserRole.value = null
        _currentUserName.value = null
        _currentUserEmail.value = null
    }

    private fun updateUserFlows() {
        val session = authRepository.getSavedSession()
        _currentUserId.value = session["user_id"]
        _currentUserRole.value = session["user_role"]
        _currentUserName.value = session["user_name"]
        _currentUserEmail.value = session["user_email"]
    }

    private fun checkAndRestoreSession() {
        val user = authRepository.currentUser
        val session = authRepository.getSavedSession()
        val savedUserId = session["user_id"]

        if (user != null && user.uid == savedUserId) {
            viewModelScope.launch {
                try {
                    user.reload().await()
                    if (user.isEmailVerified) {
                        val result = authRepository.fetchUserDetails(user)
                        if (result is AuthState.LoginSuccess) {
                            updateUserFlows()
                        } else {
                            logout()
                        }
                    } else {
                        logout()
                    }
                } catch (e: Exception) {
                    logout()
                } finally {
                    _isRestoringSession.value = false
                }
            }
        } else {
            if (user != null || savedUserId != null) {
                logout()
            }
            _isRestoringSession.value = false
        }
    }

    /**
     * Klasa fabryki ułatwiająca tworzenie instancji [AuthViewModel].
     */
    class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
