package com.ians.halerchat.core.network

import com.ians.halerchat.core.network.auth.AuthApi
import com.ians.halerchat.core.network.auth.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal expect fun defaultEngine(): HttpClientEngine

fun createHttpClient(
    storage: TokenStorage,
    engine: HttpClientEngine = defaultEngine()
) = HttpClient(engine) {
    expectSuccess = true
    defaultRequest { url(BASE_URL) }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    HttpResponseValidator {
        handleResponseExceptionWithRequest { cause, _ ->
            val response =
                (cause as? ResponseException)?.response ?: return@handleResponseExceptionWithRequest
            val err = runCatching { response.body<ApiErrorDto>() }.getOrNull()
            throw ApiException(response.status.value, err?.code, err?.error ?: err?.message)
        }
    }
    install(Auth) {
        bearer {
            nonCancellableRefresh = true

            loadTokens {
                storage.loadTokens()?.let {
                    BearerTokens(
                        accessToken = it.accessToken,
                        refreshToken = it.refreshToken
                    )
                }
            }

            refreshTokens {
                val refreshToken = oldTokens?.refreshToken ?: return@refreshTokens null
                val result = try {
                    AuthApi(client).refresh(refreshToken)
                } catch (e: ApiException) {
                    if (e.status != 401) throw e
                    storage.clear()
                    return@refreshTokens null
                }
                storage.updateTokens(result.accessToken, result.refreshToken)
                BearerTokens(result.accessToken, result.refreshToken)
            }
        }
    }

}