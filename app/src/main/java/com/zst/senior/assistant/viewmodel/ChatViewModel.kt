package com.zst.senior.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.zst.senior.assistant.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Model danych reprezentujący pojedynczą wiadomość na czacie.
 *
 * @property id Unikalny identyfikator wiadomości w bazie Firestore.
 * @property text Treść wiadomości.
 * @property userName Nazwa wyświetlana (imię i nazwisko) nadawcy.
 * @property userEmail Adres e-mail nadawcy.
 * @property timestamp Znacznik czasu wysłania wiadomości.
 */
data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val userName: String = "Anonim",
    val userEmail: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

/**
 * ViewModel zarządzający globalnym czatem w aplikacji.
 *
 * Deleguje operacje na danych do [ChatRepository], skupiając się wyłącznie na zarządzaniu
 * stanem interfejsu użytkownika (UI State). Obsługuje strumień wiadomości w czasie rzeczywistym
 * oraz akcje moderacyjne.
 *
 * @property repository Repozytorium obsługujące komunikację z bazą danych i szyfrowanie.
 */
class ChatViewModel(private val repository: ChatRepository = ChatRepository()) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    /** Zewnętrzny, publiczny strumień wiadomości obserwowany przez UI. */
    val messages = _messages.asStateFlow()

    init {
        loadMessages()
    }

    /**
     * Subskrybuje się do strumienia wiadomości z repozytorium.
     */
    private fun loadMessages() {
        viewModelScope.launch {
            repository.getMessagesFlow().collect { messageList ->
                _messages.value = messageList
            }
        }
    }

    /**
     * Wysyła nową wiadomość na czat po uprzedniej weryfikacji wyciszenia użytkownika.
     *
     * @param text Jawna treść wiadomości.
     * @param userName Nazwa wyświetlana nadawcy.
     * @param userEmail Adres e-mail nadawcy.
     */
    fun sendMessage(text: String, userName: String, userEmail: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            if (!repository.isUserMuted(userEmail)) {
                try {
                    repository.sendMessage(text, userName, userEmail)
                } catch (e: Exception) {
                    // Logowanie błędu lub obsługa stanu błędu w UI
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Usuwa wskazaną wiadomość z czatu.
     *
     * @param messageId ID dokumentu wiadomości.
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                repository.deleteMessage(messageId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Wycisza użytkownika na określony czas.
     *
     * @param email E-mail użytkownika.
     * @param hours Liczba godzin blokady.
     */
    fun muteUser(email: String, hours: Int) {
        viewModelScope.launch {
            try {
                repository.muteUser(email, hours)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Zdejmuje wyciszenie z użytkownika.
     *
     * @param email E-mail użytkownika.
     */
    fun unmuteUser(email: String) {
        viewModelScope.launch {
            try {
                repository.unmuteUser(email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Sprawdza, czy użytkownik o podanym adresie e-mail jest obecnie wyciszony.
     * Funkcja deleguje zapytanie do repozytorium.
     *
     * @param email E-mail użytkownika.
     * @return True jeśli jest wyciszony, w przeciwnym wypadku False.
     */
    suspend fun isUserMuted(email: String): Boolean {
        return try {
            repository.isUserMuted(email)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}