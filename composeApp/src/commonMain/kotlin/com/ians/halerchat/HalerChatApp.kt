package com.ians.halerchat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.ians.halerchat.core.designsystem.theme.HalerChatTheme
import com.ians.halerchat.feature.auth.LoginKey
import com.ians.halerchat.feature.auth.RegisterKey
import com.ians.halerchat.feature.auth.authEntries
import com.ians.halerchat.feature.auth.authNavSerializers
import com.ians.halerchat.feature.rooms.RoomsKey
import com.ians.halerchat.feature.rooms.roomsEntries
import com.ians.halerchat.feature.rooms.roomsNavSerializers
import kotlinx.serialization.modules.plus
import org.koin.compose.viewmodel.koinViewModel

// iOS не умеет сериализовать NavKey рефлексией: перечисляем ключи всех фич
private val navConfig = SavedStateConfiguration {
    serializersModule = authNavSerializers + roomsNavSerializers
}

@Composable
fun HalerChatApp(viewModel: MainViewModel = koinViewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedIn = isLoggedIn ?: return

    HalerChatTheme {
        val backStack = rememberNavBackStack(navConfig, if (loggedIn) RoomsKey else LoginKey)

        LaunchedEffect(loggedIn) {
            if (!loggedIn && backStack.contains(RoomsKey)) {
                backStack.clear()
                backStack.add(LoginKey)
            }
        }

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                authEntries(
                    onLoggedIn = {
                        backStack.clear()
                        backStack.add(RoomsKey)
                    },
                    onRegisterClick = {
                        backStack.add(RegisterKey)
                    },
                    onLoginClick = {
                        backStack.removeLastOrNull()
                    }
                )
                roomsEntries(
                    onLogout = viewModel::logout,
                    onCreateRoom = {},
                    onJoinRoom = {},
                )
            }
        )
    }
}
