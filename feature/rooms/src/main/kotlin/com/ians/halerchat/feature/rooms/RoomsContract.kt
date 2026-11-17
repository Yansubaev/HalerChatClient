package com.ians.halerchat.feature.rooms

import com.ians.halerchat.core.data.room.RoomsResult
import com.ians.halerchat.core.model.Room

internal data class RoomsState(
    val rooms: List<Room> = emptyList(),
    val isLoading: Boolean = false,
    val error: RoomsResult.Failure? = null,
)

internal sealed interface RoomsIntent {
    data object Retry: RoomsIntent
}