package com.ians.halerchat.feature.auth.di

import com.ians.halerchat.feature.auth.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    viewModelOf(::LoginViewModel)
}