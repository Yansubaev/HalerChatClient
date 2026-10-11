package com.ians.halerchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ians.halerchat.core.data.auth.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(authRepository: AuthRepository) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean?> = authRepository.isLoggedIn.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}