package com.ians.halerchat.core.data.room

import com.ians.halerchat.core.network.ApiException
import com.ians.halerchat.core.network.room.RoomsApi
import java.io.IOException

internal class DefaultRoomsRepository(
    private val api: RoomsApi
) : RoomsRepository {
    override suspend fun getRooms(): RoomsResult =
        try {
            val rooms = api.getRooms().toDomain()
            RoomsResult.Success(rooms)
        } catch (e: ApiException) {
            e.toRoomsResult()
        } catch (e: IOException) {
            RoomsResult.Network
        }


    private fun ApiException.toRoomsResult(): RoomsResult = when (status) {
        401 -> RoomsResult.SessionExpired
        429 -> RoomsResult.RateLimited
        else -> RoomsResult.Unknown
    }
}