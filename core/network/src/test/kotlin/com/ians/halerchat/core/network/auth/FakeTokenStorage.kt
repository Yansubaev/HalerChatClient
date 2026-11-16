package com.ians.halerchat.core.network.auth

class FakeTokenStorage(var tokens: AuthTokens? = null) : TokenStorage {

    override suspend fun loadTokens() = tokens

    override suspend fun updateTokens(accessToken: String, refreshToken: String) {
        tokens = AuthTokens(accessToken, refreshToken)
    }

    override suspend fun clear() {
        tokens = null
    }
}