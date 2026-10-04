package com.ians.halerchat.core.data.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.ians.halerchat.core.data.session.Session
import com.ians.halerchat.core.data.session.SessionStore
import com.ians.halerchat.core.model.User
import com.ians.halerchat.core.network.auth.AuthApi
import com.ians.halerchat.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class DefaultAuthRepositoryTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val sessionStore by lazy {
        SessionStore(
            PreferenceDataStoreFactory.create(
                produceFile = { File(tmp.root, "test.preferences_pb") }
            )
        )
    }

    private val user = User(id = "1", email = "a@b.c", displayName = "Alice")

    private fun authResultJson(isNewUser: Boolean) =
        """{"accessToken":"a","refreshToken":"r","isNewUser":$isNewUser,
           "user":{"id":"1","email":"a@b.c","displayName":"Alice","provider":"email"}}"""

    private fun respondingWith(status: HttpStatusCode, body: String) = MockEngine {
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    private fun repository(engine: MockEngine) = DefaultAuthRepository(
        api = AuthApi(createHttpClient(engine)),
        sessionStore = sessionStore,
    )

    @Test
    fun login_success_returnsUserAndSavesSession() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.OK, authResultJson(isNewUser = false)))

        val result = repository.login(email = "a@b.c", password = "secret123")

        assertEquals(AuthResult.Success(user), result)
        assertEquals(
            Session(accessToken = "a", refreshToken = "r", user = user),
            sessionStore.session.first(),
        )
    }

    @Test
    fun login_invalidCredentials_returnsInvalidCredentialsAndSavesNothing() = runTest {
        val repository = repository(
            respondingWith(
                HttpStatusCode.Unauthorized,
                """{"error":"invalid email or password","code":"INVALID_CREDENTIALS"}""",
            )
        )

        val result = repository.login(email = "a@b.c", password = "wrong123")

        assertEquals(AuthResult.InvalidCredentials, result)
        assertNull(sessionStore.session.first())
    }

    @Test
    fun login_noNetwork_returnsNetwork() = runTest {
        val repository = repository(MockEngine { throw IOException("no network") })

        val result = repository.login(email = "a@b.c", password = "secret123")

        assertEquals(AuthResult.Network, result)
        assertNull(sessionStore.session.first())
    }

    @Test
    fun login_validationFailed_returnsValidationWithServerMessage() = runTest {
        val repository = repository(
            respondingWith(
                HttpStatusCode.BadRequest,
                """{"error":"email: must be a valid email address","code":"VALIDATION_FAILED"}""",
            )
        )

        val result = repository.login(email = "not-an-email", password = "secret123")

        assertEquals(AuthResult.Validation("email: must be a valid email address"), result)
    }

    @Test
    fun login_gatewayThrottlingWithoutCode_returnsRateLimited() = runTest {
        val repository = repository(
            respondingWith(HttpStatusCode.TooManyRequests, """{"message":"Too Many Requests"}""")
        )

        val result = repository.login(email = "a@b.c", password = "secret123")

        assertEquals(AuthResult.RateLimited, result)
    }

    @Test
    fun login_internalError_returnsUnknown() = runTest {
        val repository = repository(
            respondingWith(
                HttpStatusCode.InternalServerError,
                """{"error":"internal error","code":"INTERNAL"}""",
            )
        )

        val result = repository.login(email = "a@b.c", password = "secret123")

        assertEquals(AuthResult.Unknown, result)
    }

    @Test
    fun register_success_returnsUserAndSavesSession() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.Created, authResultJson(isNewUser = true)))

        val result = repository.register(email = "a@b.c", password = "secret123", displayName = "Alice")

        assertEquals(AuthResult.Success(user), result)
        assertEquals(
            Session(accessToken = "a", refreshToken = "r", user = user),
            sessionStore.session.first(),
        )
    }

    @Test
    fun register_emailTaken_returnsEmailTaken() = runTest {
        val repository = repository(
            respondingWith(
                HttpStatusCode.Conflict,
                """{"error":"email already registered","code":"EMAIL_TAKEN"}""",
            )
        )

        val result = repository.register(email = "a@b.c", password = "secret123", displayName = "Alice")

        assertEquals(AuthResult.EmailTaken, result)
        assertNull(sessionStore.session.first())
    }
}
