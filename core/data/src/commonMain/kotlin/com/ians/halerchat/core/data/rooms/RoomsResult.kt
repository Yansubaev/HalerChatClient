package com.ians.halerchat.core.data.rooms

import com.ians.halerchat.core.model.Room

sealed interface RoomsResult {
    data class Success(val rooms: List<Room>) : RoomsResult
    sealed interface Failure : RoomsResult
    data object SessionExpired : Failure
    data object RateLimited : Failure
    data object Network : Failure
    data object Unknown : Failure
}