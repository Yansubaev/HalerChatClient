package com.ians.halerchat.core.data.auth

import com.ians.halerchat.core.data.session.SessionStore
import com.ians.halerchat.core.network.ApiException
import com.ians.halerchat.core.network.auth.AuthApi
import com.ians.halerchat.core.network.auth.AuthResultDto
import java.io.IOException

internal class DefaultAuthRepository internal constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
) : AuthRepository {
    override suspend fun login(email: String, password: String): AuthResult =
        authenticate { api.login(email, password) }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): AuthResult =
        authenticate { api.register(email, password, displayName) }

    private suspend fun authenticate(call: suspend () -> AuthResultDto): AuthResult =
        try {
            val session = call().toSession()
            sessionStore.save(session)
            AuthResult.Success(session.user)
        } catch (e: ApiException) {
            e.toAuthResult()
        } catch (e: IOException) {
            AuthResult.Network
        }

    private fun ApiException.toAuthResult(): AuthResult = when {
        code == "INVALID_CREDENTIALS" -> AuthResult.InvalidCredentials
        code == "EMAIL_TAKEN" -> AuthResult.EmailTaken
        code == "RATE_LIMITED" || status == 429 -> AuthResult.RateLimited
        code == "VALIDATION_FAILED" || code == "INVALID_BODY" -> AuthResult.Validation(message)
        else -> AuthResult.Unknown
    }
}