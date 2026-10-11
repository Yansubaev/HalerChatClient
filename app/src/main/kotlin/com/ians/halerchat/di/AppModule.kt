package com.ians.halerchat.di

import com.ians.halerchat.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module { viewModelOf(::MainViewModel) }