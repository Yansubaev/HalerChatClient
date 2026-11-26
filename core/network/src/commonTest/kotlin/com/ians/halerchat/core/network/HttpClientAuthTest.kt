package com.ians.halerchat.core.network

import com.ians.halerchat.core.network.auth.AuthTokens
import com.ians.halerchat.core.network.auth.FakeTokenStorage
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail
import kotlin.test.Test

class HttpClientAuthTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private val HttpRequestData.isRefresh get() = url.encodedPath.endsWith("auth/refresh")

    /** Сервер, у которого валиден только access "a2"; refresh выдаёт пару a2/r2. */
    private fun MockRequestHandleScope.server(request: HttpRequestData) = when {
        request.isRefresh ->
            respond("""{"accessToken":"a2","refreshToken":"r2"}""", headers = json)
        request.headers[HttpHeaders.Authorization] == "Bearer a2" ->
            respond("{}", headers = json)
        else ->
            respond("""{"error":"unauthorized"}""", HttpStatusCode.Unauthorized, json)
    }

    @Test
    fun request_hasBearerHeader() = runTest {
        val engine = MockEngine { respond("{}", headers = json) }

        createHttpClient(FakeTokenStorage(AuthTokens("a1", "r1")), engine).get("rooms")

        assertEquals(
            "Bearer a1",
            engine.requestHistory.single().headers[HttpHeaders.Authorization]
        )
    }

    @Test
    fun expiredAccess_refreshesSavesAndRetries() = runTest {
        val storage = FakeTokenStorage(AuthTokens("a1", "r1"))
        val engine = MockEngine { server(it) }

        createHttpClient(storage, engine).get("rooms")

        assertEquals(AuthTokens("a2", "r2"), storage.tokens)
        // rooms(401) → refresh → rooms(200)
        assertEquals(3, engine.requestHistory.size)
        val refresh = engine.requestHistory[1]
        assertEquals(true, refresh.isRefresh)
        assertNull(refresh.headers[HttpHeaders.Authorization])
    }

    @Test
    fun refreshRejected_clearsStorage_andThrows401() = runTest {
        val storage = FakeTokenStorage(AuthTokens("a1", "r1"))
        val engine = MockEngine {
            respond("""{"error":"invalid refresh token"}""", HttpStatusCode.Unauthorized, json)
        }

        try {
            createHttpClient(storage, engine).get("rooms")
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(401, e.status)
        }
        assertNull(storage.tokens)
        // rooms(401) → refresh(401), без повторов
        assertEquals(2, engine.requestHistory.size)
    }

    @Test
    fun parallel401_singleRefresh() = runTest {
        val storage = FakeTokenStorage(AuthTokens("a1", "r1"))
        val engine = MockEngine { request ->
            // держим refresh «в полёте», пока второй запрос тоже получает 401
            if (request.isRefresh) delay(100)
            server(request)
        }
        val client = createHttpClient(storage, engine)

        coroutineScope {
            launch { client.get("rooms") }
            launch { client.get("rooms") }
        }

        assertEquals(1, engine.requestHistory.count { it.isRefresh })
        assertEquals(AuthTokens("a2", "r2"), storage.tokens)
    }
}
