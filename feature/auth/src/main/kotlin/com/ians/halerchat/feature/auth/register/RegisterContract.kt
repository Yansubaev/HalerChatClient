package com.ians.halerchat.feature.auth.register

import com.ians.halerchat.core.data.auth.AuthResult

internal data class RegisterState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val isLoading: Boolean = false,
    val error: AuthResult.Failure? = null,
    val isLoggedIn: Boolean = false
) {
    // Password rule from spec 4.2: >= 8 chars, at least one letter and one digit.
    val isPasswordValid: Boolean
        get() = password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }

    val canSubmit: Boolean
        get() = email.isNotBlank() && isPasswordValid && displayName.isNotBlank() && !isLoading

    companion object {
        const val NAME_MAX_LENGTH = 64
    }
}

internal sealed interface RegisterIntent {
    data class EmailChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class NameChanged(val value: String) : RegisterIntent
    data object Submit : RegisterIntent
}