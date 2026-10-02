package com.ians.halerchat.core.network.auth

import com.ians.halerchat.core.network.ApiException
import com.ians.halerchat.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test


class AuthApiTest {
    @Test
    fun login_hitsDevPath_andParsesUser() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/dev/auth/login-email", request.url.encodedPath)
            respond(
                """{"accessToken":"a","refreshToken":"r","isNewUser":false,
               "user":{"id":"1","email":"a@b.c","displayName":"Alice","provider":"email"}}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val result = AuthApi(createHttpClient(engine)).login("a@b.c", "secret123")
        assertEquals("Alice", result.user.displayName)
    }

    @Test
    fun login_withInvalidCredentials_throwsApiException() = runTest {
        val engine = MockEngine {
            respond(
                """{"error":"invalid email or password","code":"INVALID_CREDENTIALS"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        try {
            AuthApi(createHttpClient(engine)).login("a@b.c", "wrong")
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(401, e.status)
            assertEquals("INVALID_CREDENTIALS", e.code)
        }
    }

    @Test
    fun register_hitsDevPath_andParsesUser() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/dev/auth/register", request.url.encodedPath)
            respond(
                """{"accessToken":"a","refreshToken":"r","isNewUser":true,
               "user":{"id":"1","email":"a@b.c","displayName":"Alice","provider":"email"}}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val result = AuthApi(createHttpClient(engine)).register("a@b.c", "secret123", "Alice")
        assertEquals("Alice", result.user.displayName)
    }

    @Test
    fun register_withTakenEmail_throwsApiException() = runTest {
        val engine = MockEngine {
            respond(
                """{"error":"email already registered","code":"EMAIL_TAKEN"}""",
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        try {
            AuthApi(createHttpClient(engine)).register("a@b.c", "secret123", "Alice")
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(409, e.status)
            assertEquals("EMAIL_TAKEN", e.code)
        }
    }

}
