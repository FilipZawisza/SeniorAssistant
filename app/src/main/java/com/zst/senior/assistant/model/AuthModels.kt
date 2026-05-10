package com.zst.senior.assistant.model

/**
 * Enum reprezentujący role użytkowników w systemie.
 */
enum class UserRole(val collectionName: String) {
    SENIOR("Senior"),
    WOLONTARIUSZ("Wolontariusz"),
    ADMIN("Admin");

    companion object {
        fun fromString(value: String): UserRole? {
            return entries.find { it.collectionName.equals(value, ignoreCase = true) }
        }
    }
}

/**
 * Reprezentuje różne stany procesu autoryzacji.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val role: String, val userId: String) : AuthState()
    data class LoginError(val message: String) : AuthState()
    object RegisterSuccess : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
}

typealias AuthUiState = AuthState
