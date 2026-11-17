package com.ians.halerchat.core.network.room

import kotlinx.serialization.Serializable

/** POST /rooms — отправляется серверу. Обязателен только name. */
@Serializable
internal data class CreateRoomRequest(
    val name: String,
    val description: String? = null,
    val visibility: String? = null
)

/** Комната из POST /rooms и GET /rooms. inviteToken/accessCode приходят только при создании. */
@Serializable
data class RoomDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val ownerID: String,
    val ownerName: String,
    val createdAt: String,
    val memberCount: Int,
    val visibility: String,
    val inviteToken: String? = null,
    val accessCode: String? = null
)

/** GET /rooms */
@Serializable
data class RoomsResponseDto(val rooms: List<RoomDto>)

@Serializable
data class RoomMemberDto(
    val roomID: String,
    val userID: String,
    val displayName: String,
    val role: String,
    val joinedAt: String
)

/** GET /rooms/{id}. room.members в JSON дублирует members — игнорируем. */
@Serializable
data class RoomDetailDto(
    val room: RoomDto,
    val members: List<RoomMemberDto>
)

/** PATCH /rooms/{id} */
@Serializable
internal data class UpdateVisibilityRequest(val visibility: String)

/** accessCode == "" для public. */
@Serializable
data class UpdateVisibilityResponseDto(
    val id: String,
    val visibility: String,
    val accessCode: String
)

/** POST /rooms/{id}/invite/reset. accessCode только для private. */
@Serializable
data class ResetInviteResponseDto(
    val inviteToken: String,
    val accessCode: String? = null
)

/** GET /rooms/join/{token} (Accept: application/json) */
@Serializable
data class RoomPreviewDto(
    val roomID: String,
    val name: String,
    val visibility: String,
    val memberCount: Int
)

/** POST /rooms/join/{token}. accessCode обязателен для private. */
@Serializable
internal data class JoinRoomRequest(val accessCode: String? = null)

@Serializable
data class JoinRoomResponseDto(val room: JoinedRoomDto)

/** memberCount — значение до вступления. */
@Serializable
data class JoinedRoomDto(
    val id: String,
    val name: String,
    val visibility: String,
    val memberCount: Int,
    val ownerID: String,
    val ownerName: String
)
