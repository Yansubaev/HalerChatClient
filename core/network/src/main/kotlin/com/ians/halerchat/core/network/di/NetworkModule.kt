package com.ians.halerchat.core.network.di

import com.ians.halerchat.core.network.auth.AuthApi
import com.ians.halerchat.core.network.createHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val networkModule = module {
    single { createHttpClient() }
    singleOf(::AuthApi)
}