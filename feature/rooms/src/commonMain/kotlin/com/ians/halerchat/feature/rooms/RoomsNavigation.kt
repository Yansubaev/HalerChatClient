package com.ians.halerchat.feature.rooms

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object RoomsKey : NavKey

val roomsNavSerializers = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(RoomsKey::class, RoomsKey.serializer())
    }
}

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