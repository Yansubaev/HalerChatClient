package com.ians.halerchat.core.network.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.plugins.auth.clearAuthTokens
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(private val client: HttpClient) {
    suspend fun login(email: String, password: String): AuthResultDto =
        client.post("auth/login-email") {
            attributes.put(AuthCircuitBreaker, Unit)
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun register(email: String, password: String, displayName: String): AuthResultDto =
        client.post("auth/register") {
            attributes.put(AuthCircuitBreaker, Unit)
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, displayName))
        }.body()

    suspend fun logout(refreshToken: String) {
        client.post("auth/logout") {
            attributes.put(AuthCircuitBreaker, Unit)
            contentType(ContentType.Application.Json)
            setBody(LogoutRequest(refreshToken))
        }
    }

    suspend fun refresh(refreshToken: String): RefreshResultDto =
        client.post("auth/refresh") {
            attributes.put(AuthCircuitBreaker, Unit)
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()

    fun clearTokens() = client.clearAuthTokens()
}