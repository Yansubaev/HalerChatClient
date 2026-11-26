package com.ians.halerchat.core.data.rooms

interface RoomsRepository {
    suspend fun getRooms(): RoomsResult
}