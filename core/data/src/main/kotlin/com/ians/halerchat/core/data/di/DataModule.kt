package com.ians.halerchat.core.data.di

import com.ians.halerchat.core.data.auth.AuthRepository
import com.ians.halerchat.core.data.auth.DefaultAuthRepository
import com.ians.halerchat.core.data.session.SessionStore
import com.ians.halerchat.core.data.session.sessionDataStore
import com.ians.halerchat.core.network.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    includes(networkModule)
    single { SessionStore(androidContext().sessionDataStore) }
    singleOf(::DefaultAuthRepository) bind AuthRepository::class
}