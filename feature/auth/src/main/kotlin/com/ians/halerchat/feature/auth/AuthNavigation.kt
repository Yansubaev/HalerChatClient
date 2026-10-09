package com.ians.halerchat.feature.auth

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ians.halerchat.feature.auth.login.LoginRoute
import kotlinx.serialization.Serializable

@Serializable
data object LoginKey : NavKey

fun EntryProviderScope<NavKey>.authEntries(onLoggedIn: () -> Unit) {
    entry<LoginKey> { LoginRoute(onLoggedIn = onLoggedIn) }
}