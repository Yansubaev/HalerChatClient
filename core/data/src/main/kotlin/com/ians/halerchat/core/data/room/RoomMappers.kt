package com.ians.halerchat.core.data.room

import com.ians.halerchat.core.model.Room
import com.ians.halerchat.core.network.room.RoomDto
import com.ians.halerchat.core.network.room.RoomsResponseDto

internal fun RoomDto.toDomain() = Room(
    id = id,
    name = name,
    description = description,
    ownerId = ownerID,
    ownerName = ownerName,
    createdAt = createdAt,
    memberCount = memberCount,
    visibility = visibility,
    inviteToken = inviteToken,
    accessCode = accessCode
)

internal fun RoomsResponseDto.toDomain() = rooms.map { it.toDomain() }
