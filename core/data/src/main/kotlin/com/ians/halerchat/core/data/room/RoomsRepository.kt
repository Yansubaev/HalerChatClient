package com.ians.halerchat.core.data.room

interface RoomsRepository {
    suspend fun getRooms(): RoomsResult
}