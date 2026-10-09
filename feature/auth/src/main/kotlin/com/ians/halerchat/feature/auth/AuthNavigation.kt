package com.ians.halerchat.feature.auth

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ians.halerchat.feature.auth.login.LoginRoute
import com.ians.halerchat.feature.auth.register.RegisterRoute
import kotlinx.serialization.Serializable

@Serializable
data object LoginKey : NavKey

@Serializable
data object RegisterKey : NavKey

fun EntryProviderScope<NavKey>.authEntries(
    onLoggedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    entry<LoginKey> { LoginRoute(onLoggedIn = onLoggedIn, onRegisterClick = onRegisterClick) }
    entry<RegisterKey> { RegisterRoute(onLoggedIn = onLoggedIn, onLoginClick = onLoginClick) }
}