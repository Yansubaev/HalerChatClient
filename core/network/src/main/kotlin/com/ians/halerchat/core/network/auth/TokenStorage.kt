package com.ians.halerchat.core.network.auth

data class AuthTokens(val accessToken: String, val refreshToken: String)

interface TokenStorage {
    suspend fun loadTokens(): AuthTokens?
    suspend fun updateTokens(accessToken: String, refreshToken: String)
    suspend fun clear()
}