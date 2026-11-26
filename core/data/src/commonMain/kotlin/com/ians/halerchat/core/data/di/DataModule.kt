package com.ians.halerchat.core.data.di

import com.ians.halerchat.core.data.auth.AuthRepository
import com.ians.halerchat.core.data.auth.DefaultAuthRepository
import com.ians.halerchat.core.data.rooms.DefaultRoomsRepository
import com.ians.halerchat.core.data.rooms.RoomsRepository
import com.ians.halerchat.core.data.session.SessionStore
import com.ians.halerchat.core.network.auth.TokenStorage
import com.ians.halerchat.core.network.di.networkModule
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

// Даёт DataStore<Preferences> сессии: у каждой платформы свой путь к файлу
internal expect val platformDataModule: Module

val dataModule = module {
    includes(networkModule, platformDataModule)
    singleOf(::SessionStore) bind TokenStorage::class
    singleOf(::DefaultAuthRepository) bind AuthRepository::class
    singleOf(::DefaultRoomsRepository) bind RoomsRepository::class
}