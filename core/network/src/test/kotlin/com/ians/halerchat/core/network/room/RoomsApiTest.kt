package com.ians.halerchat.core.network.room

import com.ians.halerchat.core.network.ApiException
import com.ians.halerchat.core.network.auth.FakeTokenStorage
import com.ians.halerchat.core.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class RoomsApiTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun engine(
        body: String = "",
        status: HttpStatusCode = HttpStatusCode.OK
    ) = MockEngine { respond(body, status, json) }

    private fun api(engine: MockEngine) = RoomsApi(createHttpClient(FakeTokenStorage(), engine))

    private fun MockEngine.request(): HttpRequestData = requestHistory.single()

    private suspend fun HttpRequestData.bodyText() = body.toByteArray().decodeToString()

    private suspend fun assertApiError(status: Int, message: String, block: suspend () -> Unit) {
        try {
            block()
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(status, e.status)
            assertEquals(message, e.message)
        }
    }

    // GET /rooms

    @Test
    fun getRooms_parsesList_withoutInviteSecrets() = runTest {
        val engine = engine(
            """{"rooms":[{"id":"a1","name":"General","description":"d","ownerID":"u1",
            "ownerName":"Alice","createdAt":"2026-09-30T12:00:00.123456789Z",
            "memberCount":3,"visibility":"public"}]}"""
        )

        val rooms = api(engine).getRooms().rooms

        assertEquals(HttpMethod.Get, engine.request().method)
        assertEquals("/dev/rooms", engine.request().url.encodedPath)
        assertEquals("General", rooms.single().name)
        assertEquals("u1", rooms.single().ownerID)
        assertEquals(3, rooms.single().memberCount)
        assertNull(rooms.single().inviteToken)
        assertNull(rooms.single().accessCode)
    }

    @Test
    fun getRooms_emptyList() = runTest {
        assertEquals(emptyList<RoomDto>(), api(engine("""{"rooms":[]}""")).getRooms().rooms)
    }

    @Test
    fun getRooms_unauthorized_throwsApiException() = runTest {
        val engine = engine("""{"error":"unauthorized"}""", HttpStatusCode.Unauthorized)
        assertApiError(401, "unauthorized") { api(engine).getRooms() }
    }

    // POST /rooms

    @Test
    fun createRoom_postsBody_andParsesInviteSecrets() = runTest {
        val engine = engine(
            """{"id":"a1","name":"General","ownerID":"u1","ownerName":"Alice",
            "createdAt":"2026-09-30T12:00:00Z","memberCount":1,"visibility":"private",
            "inviteToken":"tok","accessCode":"code"}""",
            HttpStatusCode.Created
        )

        val room = api(engine).createRoom("General", "desc", "private")

        assertEquals(HttpMethod.Post, engine.request().method)
        assertEquals("/dev/rooms", engine.request().url.encodedPath)
        assertEquals(
            """{"name":"General","description":"desc","visibility":"private"}""",
            engine.request().bodyText()
        )
        assertEquals("tok", room.inviteToken)
        assertEquals("code", room.accessCode)
        assertEquals(1, room.memberCount)
    }

    @Test
    fun createRoom_onlyName_omitsOptionalFields() = runTest {
        val engine = engine(
            """{"id":"a1","name":"G","ownerID":"u1","ownerName":"A","createdAt":"t",
            "memberCount":1,"visibility":"public","inviteToken":"tok"}""",
            HttpStatusCode.Created
        )

        val room = api(engine).createRoom("G", null, null)

        assertEquals("""{"name":"G"}""", engine.request().bodyText())
        assertNull(room.accessCode)
        assertNull(room.description)
    }

    @Test
    fun createRoom_validationFailed_throwsApiException() = runTest {
        val engine = engine(
            """{"error":"name: is required","code":"VALIDATION_FAILED"}""",
            HttpStatusCode.BadRequest
        )
        try {
            api(engine).createRoom("", null, null)
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(400, e.status)
            assertEquals("VALIDATION_FAILED", e.code)
        }
    }

    // GET /rooms/{roomId}

    @Test
    fun room_parsesRoomAndMembers_ignoringDuplicatedMembersInsideRoom() = runTest {
        val member = """{"roomID":"a1","userID":"u1","displayName":"Alice","role":"admin",
            "joinedAt":"2026-09-30T12:00:00.1Z"}"""
        val engine = engine(
            """{"room":{"id":"a1","name":"General","ownerID":"u1","ownerName":"Alice",
            "createdAt":"t","memberCount":2,"visibility":"private",
            "inviteToken":"tok","accessCode":"code","members":[$member]},
            "members":[$member,{"roomID":"a1","userID":"u2","displayName":"Bob",
            "role":"member","joinedAt":"t2"}]}"""
        )

        val detail = api(engine).room("a1")

        assertEquals(HttpMethod.Get, engine.request().method)
        assertEquals("/dev/rooms/a1", engine.request().url.encodedPath)
        assertEquals("tok", detail.room.inviteToken)
        assertEquals(listOf("admin", "member"), detail.members.map { it.role })
        assertEquals("u2", detail.members[1].userID)
        assertEquals("Bob", detail.members[1].displayName)
    }

    @Test
    fun room_withoutInviteSecrets_forNonOwner() = runTest {
        val engine = engine(
            """{"room":{"id":"a1","name":"G","ownerID":"u1","ownerName":"A","createdAt":"t",
            "memberCount":1,"visibility":"public"},"members":[]}"""
        )

        val detail = api(engine).room("a1")

        assertNull(detail.room.inviteToken)
        assertNull(detail.room.accessCode)
    }

    @Test
    fun room_forbidden_throwsApiException() = runTest {
        val engine = engine(
            """{"error":"access denied","code":"FORBIDDEN"}""",
            HttpStatusCode.Forbidden
        )
        assertApiError(403, "access denied") { api(engine).room("a1") }
    }

    @Test
    fun room_notFound_throwsApiException() = runTest {
        val engine = engine(
            """{"error":"room not found","code":"NOT_FOUND"}""",
            HttpStatusCode.NotFound
        )
        assertApiError(404, "room not found") { api(engine).room("a1") }
    }

    // DELETE /rooms/{roomId}

    @Test
    fun deleteRoom_sendsDelete_andAcceptsEmptyResponse() = runTest {
        val engine = engine(status = HttpStatusCode.NoContent)

        api(engine).deleteRoom("a1")

        assertEquals(HttpMethod.Delete, engine.request().method)
        assertEquals("/dev/rooms/a1", engine.request().url.encodedPath)
    }

    @Test
    fun deleteRoom_notOwner_throwsApiException() = runTest {
        val engine = engine(
            """{"error":"only the room owner can delete it"}""",
            HttpStatusCode.Forbidden
        )
        assertApiError(403, "only the room owner can delete it") { api(engine).deleteRoom("a1") }
    }

    // PATCH /rooms/{roomId}

    @Test
    fun updateVisibility_patchesBody_andParsesAccessCode() = runTest {
        val engine = engine("""{"id":"a1","visibility":"private","accessCode":"code"}""")

        val result = api(engine).updateVisibility("a1", "private")

        assertEquals(HttpMethod.Patch, engine.request().method)
        assertEquals("/dev/rooms/a1", engine.request().url.encodedPath)
        assertEquals("""{"visibility":"private"}""", engine.request().bodyText())
        assertEquals("private", result.visibility)
        assertEquals("code", result.accessCode)
    }

    @Test
    fun updateVisibility_toPublic_accessCodeIsEmptyString() = runTest {
        val engine = engine("""{"id":"a1","visibility":"public","accessCode":""}""")

        assertEquals("", api(engine).updateVisibility("a1", "public").accessCode)
    }

    @Test
    fun updateVisibility_notOwner_throwsApiException() = runTest {
        val engine = engine(
            """{"error":"only the owner can update this room"}""",
            HttpStatusCode.Forbidden
        )
        assertApiError(403, "only the owner can update this room") {
            api(engine).updateVisibility("a1", "public")
        }
    }

    // POST /rooms/{roomId}/invite/reset

    @Test
    fun resetInvite_postsWithoutBody_andParsesNewSecrets() = runTest {
        val engine = engine("""{"inviteToken":"tok2","accessCode":"code2"}""")

        val result = api(engine).resetInvite("a1")

        assertEquals(HttpMethod.Post, engine.request().method)
        assertEquals("/dev/rooms/a1/invite/reset", engine.request().url.encodedPath)
        assertEquals("tok2", result.inviteToken)
        assertEquals("code2", result.accessCode)
    }

    @Test
    fun resetInvite_publicRoom_accessCodeIsNull() = runTest {
        val engine = engine("""{"inviteToken":"tok2"}""")

        assertNull(api(engine).resetInvite("a1").accessCode)
    }

    @Test
    fun resetInvite_notOwner_throwsApiException() = runTest {
        val engine = engine(
            """{"error":"only the owner can reset the invite"}""",
            HttpStatusCode.Forbidden
        )
        assertApiError(403, "only the owner can reset the invite") { api(engine).resetInvite("a1") }
    }

    // POST /rooms/{roomId}/leave

    @Test
    fun leaveRoom_postsToLeavePath() = runTest {
        val engine = engine("""{"status":"left","roomID":"a1"}""")

        api(engine).leaveRoom("a1")

        assertEquals(HttpMethod.Post, engine.request().method)
        assertEquals("/dev/rooms/a1/leave", engine.request().url.encodedPath)
    }

    @Test
    fun leaveRoom_serverError_throwsApiException() = runTest {
        val engine = engine("""{"error":"boom"}""", HttpStatusCode.InternalServerError)
        assertApiError(500, "boom") { api(engine).leaveRoom("a1") }
    }

    // GET /rooms/join/{inviteToken}

    @Test
    fun roomPreview_sendsAcceptJson_andParsesPreview() = runTest {
        val engine = engine(
            """{"roomID":"a1","name":"General","visibility":"private","memberCount":3}"""
        )

        val preview = api(engine).roomPreview("tok")

        assertEquals(HttpMethod.Get, engine.request().method)
        assertEquals("/dev/rooms/join/tok", engine.request().url.encodedPath)
        assertEquals("application/json", engine.request().headers[HttpHeaders.Accept])
        assertEquals("a1", preview.roomID)
        assertEquals("General", preview.name)
        assertEquals("private", preview.visibility)
        assertEquals(3, preview.memberCount)
    }

    @Test
    fun roomPreview_invalidInvite_throwsApiException() = runTest {
        val engine = engine("""{"error":"invalid invite link"}""", HttpStatusCode.NotFound)
        assertApiError(404, "invalid invite link") { api(engine).roomPreview("tok") }
    }

    // POST /rooms/join/{inviteToken}

    @Test
    fun joinRoom_private_sendsAccessCode_andParsesRoom() = runTest {
        val engine = engine(
            """{"room":{"id":"a1","name":"General","visibility":"private","memberCount":3,
            "ownerID":"u1","ownerName":"Alice"}}"""
        )

        val room = api(engine).joinRoom("tok", "code").room

        assertEquals(HttpMethod.Post, engine.request().method)
        assertEquals("/dev/rooms/join/tok", engine.request().url.encodedPath)
        assertEquals("""{"accessCode":"code"}""", engine.request().bodyText())
        assertEquals("a1", room.id)
        assertEquals("u1", room.ownerID)
        assertEquals("Alice", room.ownerName)
        assertEquals(3, room.memberCount)
    }

    @Test
    fun joinRoom_public_sendsEmptyObject() = runTest {
        val engine = engine(
            """{"room":{"id":"a1","name":"G","visibility":"public","memberCount":1,
            "ownerID":"u1","ownerName":"A"}}"""
        )

        api(engine).joinRoom("tok", null)

        assertEquals("{}", engine.request().bodyText())
    }

    @Test
    fun joinRoom_wrongAccessCode_throwsApiException() = runTest {
        val engine = engine("""{"error":"wrong access code"}""", HttpStatusCode.Forbidden)
        assertApiError(403, "wrong access code") { api(engine).joinRoom("tok", "bad") }
    }

    @Test
    fun joinRoom_invalidInvite_throwsApiException() = runTest {
        val engine = engine("""{"error":"invalid invite link"}""", HttpStatusCode.NotFound)
        assertApiError(404, "invalid invite link") { api(engine).joinRoom("tok", null) }
    }
}
