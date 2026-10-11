package com.ians.halerchat

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.ians.halerchat.feature.auth.LoginKey
import com.ians.halerchat.feature.auth.RegisterKey
import com.ians.halerchat.feature.auth.authEntries

@Composable
fun HalerChatApp(viewModel: MainViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val loggedIn = isLoggedIn ?: return
    val backStack = rememberNavBackStack(if (loggedIn) HomeKey else LoginKey)

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
                    backStack.add(HomeKey)
                },
                onRegisterClick = {
                    backStack.add(RegisterKey)
                },
                onLoginClick = {
                    backStack.removeLastOrNull()
                }
            )
            entry<HomeKey> { Text("Home") }
        }
    )
}