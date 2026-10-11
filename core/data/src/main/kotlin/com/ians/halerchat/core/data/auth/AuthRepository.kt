package com.ians.halerchat.core.data.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>

    suspend fun login(email: String, password: String): AuthResult

    suspend fun register(email: String, password: String, displayName: String): AuthResult
}