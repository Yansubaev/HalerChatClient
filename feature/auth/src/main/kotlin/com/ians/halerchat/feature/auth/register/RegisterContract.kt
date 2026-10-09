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
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank() && !isLoading
}

internal sealed interface RegisterIntent {
    data class EmailChanged(val value: String) : RegisterIntent
    data class PasswordChanged(val value: String) : RegisterIntent
    data class NameChanged(val value: String) : RegisterIntent
    data object Submit : RegisterIntent
}