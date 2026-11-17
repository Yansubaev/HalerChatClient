package com.ians.halerchat.feature.rooms

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object RoomsKey : NavKey

fun EntryProviderScope<NavKey>.roomsEntries(
    onLogout: () -> Unit,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
) {
    entry<RoomsKey> {
        RoomsRoute(
            onLogout = onLogout,
            onCreateRoom = onCreateRoom,
            onJoinRoom = onJoinRoom
        )
    }
}