package com.ians.halerchat.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine = OkHttp.create()) = HttpClient(engine) {
    expectSuccess = true
    defaultRequest { url(BuildConfig.BASE_URL) }
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
}