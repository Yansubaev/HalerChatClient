package com.ians.halerchat.core.data.rooms

import com.ians.halerchat.core.model.Room
import com.ians.halerchat.core.model.RoomDetail
import com.ians.halerchat.core.model.RoomMember
import com.ians.halerchat.core.network.room.RoomDetailDto
import com.ians.halerchat.core.network.room.RoomDto
import com.ians.halerchat.core.network.room.RoomMemberDto
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

internal fun RoomMemberDto.toDomain() = RoomMember(
    roomId = roomID,
    userId = userID,
    displayName = displayName,
    role = role,
    joinedAt = joinedAt
)

internal fun RoomDetailDto.toDomain() = RoomDetail(
    room = room.toDomain(),
    members = members.map { it.toDomain() }
)
