package com.ians.halerchat.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.halerchat.core.data.auth.AuthRepository
import com.ians.halerchat.core.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.EmailChanged -> _state.update {
                it.copy(email = intent.value, error = null)
            }

            is LoginIntent.PasswordChanged -> _state.update {
                it.copy(password = intent.value, error = null)
            }

            LoginIntent.Submit -> submit()
        }
    }

    private fun submit() {
        if (!_state.value.canSubmit) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val stateValues = _state.value
            when (val authResult = authRepository.login(stateValues.email, stateValues.password)) {
                is AuthResult.Failure -> {
                    _state.update { it.copy(isLoading = false, error = authResult) }
                }

                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false, error = null, isLoggedIn = true) }
                }
            }
        }
    }
}