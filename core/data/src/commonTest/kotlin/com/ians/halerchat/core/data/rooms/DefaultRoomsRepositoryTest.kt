package com.ians.halerchat.core.data.rooms

import com.ians.halerchat.core.data.session.InMemoryDataStore
import com.ians.halerchat.core.data.session.Session
import com.ians.halerchat.core.data.session.SessionStore
import com.ians.halerchat.core.model.Room
import com.ians.halerchat.core.model.User
import com.ians.halerchat.core.network.createHttpClient
import com.ians.halerchat.core.network.room.RoomsApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DefaultRoomsRepositoryTest {

    private val sessionStore by lazy { SessionStore(InMemoryDataStore()) }

    private fun respondingWith(status: HttpStatusCode, body: String) = MockEngine {
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
    }

    private fun repository(engine: MockEngine) = DefaultRoomsRepository(
        api = RoomsApi(createHttpClient(sessionStore, engine)),
    )

    @Test
    fun getRooms_success_mapsDtoToDomain() = runTest {
        // description отсутствует — как у комнаты, созданной без описания (omitempty)
        val repository = repository(
            respondingWith(
                HttpStatusCode.OK,
                """{"rooms":[{"id":"r1","name":"General","ownerID":"u1","ownerName":"Alice",
                   "createdAt":"2026-09-30T12:00:00Z","memberCount":3,"visibility":"public"}]}""",
            )
        )

        val result = repository.getRooms()

        val expected = Room(
            id = "r1",
            name = "General",
            description = null,
            ownerId = "u1",
            ownerName = "Alice",
            createdAt = "2026-09-30T12:00:00Z",
            memberCount = 3,
            visibility = "public",
            inviteToken = null,
            accessCode = null,
        )
        assertEquals(RoomsResult.Success(listOf(expected)), result)
    }

    @Test
    fun getRooms_emptyList_returnsSuccessWithEmptyList() = runTest {
        val repository = repository(respondingWith(HttpStatusCode.OK, """{"rooms":[]}"""))

        assertEquals(RoomsResult.Success(emptyList()), repository.getRooms())
    }

    @Test
    fun getRooms_refreshRejected_returnsSessionExpired_andClearsSession() = runTest {
        sessionStore.save(
            Session(
                accessToken = "a",
                refreshToken = "r",
                user = User(id = "u1", email = "a@b.c", displayName = "Alice"),
            )
        )
        // и /rooms, и /auth/refresh отвечают 401
        val repository = repository(
            respondingWith(HttpStatusCode.Unauthorized, """{"error":"unauthorized"}""")
        )

        assertEquals(RoomsResult.SessionExpired, repository.getRooms())
        assertNull(sessionStore.session.first())
    }

    @Test
    fun getRooms_tooManyRequests_returnsRateLimited() = runTest {
        // throttle API Gateway: другой формат тела, без code
        val repository = repository(
            respondingWith(HttpStatusCode.TooManyRequests, """{"message":"Too Many Requests"}""")
        )

        assertEquals(RoomsResult.RateLimited, repository.getRooms())
    }

    @Test
    fun getRooms_serverError_returnsUnknown() = runTest {
        val repository = repository(
            respondingWith(
                HttpStatusCode.InternalServerError,
                """{"error":"failed to list rooms"}"""
            )
        )

        assertEquals(RoomsResult.Unknown, repository.getRooms())
    }

    @Test
    fun getRooms_noNetwork_returnsNetwork() = runTest {
        val repository = repository(MockEngine { throw IOException("no network") })

        assertEquals(RoomsResult.Network, repository.getRooms())
    }
}
