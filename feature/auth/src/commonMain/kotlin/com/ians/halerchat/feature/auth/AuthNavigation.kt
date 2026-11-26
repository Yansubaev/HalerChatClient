package com.ians.halerchat.feature.auth

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.ians.halerchat.feature.auth.login.LoginRoute
import com.ians.halerchat.feature.auth.register.RegisterRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
data object LoginKey : NavKey

@Serializable
data object RegisterKey : NavKey

val authNavSerializers = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(LoginKey::class, LoginKey.serializer())
        subclass(RegisterKey::class, RegisterKey.serializer())
    }
}

fun EntryProviderScope<NavKey>.authEntries(
    onLoggedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    entry<LoginKey> { LoginRoute(onLoggedIn = onLoggedIn, onRegisterClick = onRegisterClick) }
    entry<RegisterKey> { RegisterRoute(onLoggedIn = onLoggedIn, onLoginClick = onLoginClick) }
}