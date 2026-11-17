package com.ians.halerchat.core.network.room

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class RoomsApi(private val client: HttpClient) {
    suspend fun getRooms(): RoomsResponseDto =
        client.get("rooms").body()

    suspend fun createRoom(
        name: String,
        description: String?,
        visibility: String?
    ): RoomDto =
        client.post("rooms") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateRoomRequest(
                    name = name,
                    description = description,
                    visibility = visibility
                )
            )
        }.body()

    suspend fun room(roomId: String): RoomDetailDto =
        client.get("rooms/$roomId").body()

    suspend fun deleteRoom(roomId: String) {
        client.delete("rooms/$roomId")
    }

    suspend fun updateVisibility(roomId: String, visibility: String): UpdateVisibilityResponseDto =
        client.patch("rooms/$roomId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateVisibilityRequest(visibility))
        }.body()

    suspend fun resetInvite(roomId: String): ResetInviteResponseDto =
        client.post("rooms/$roomId/invite/reset").body()

    suspend fun leaveRoom(roomId: String) {
        client.post("rooms/$roomId/leave")
    }

    suspend fun roomPreview(inviteToken: String): RoomPreviewDto =
        client.get("rooms/join/$inviteToken") {
            accept(ContentType.Application.Json) // иначе сервер вернёт HTML
        }.body()

    suspend fun joinRoom(inviteToken: String, accessCode: String?): JoinRoomResponseDto =
        client.post("rooms/join/$inviteToken") {
            contentType(ContentType.Application.Json)
            setBody(JoinRoomRequest(accessCode))
        }.body()
}
