package com.ians.halerchat.core.network.room

import kotlinx.serialization.Serializable

/** POST /rooms — отправляется серверу. Обязателен только name. */
@Serializable
data class CreateRoomRequest(
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
