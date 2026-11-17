package com.ians.halerchat.feature.rooms.di

import com.ians.halerchat.feature.rooms.RoomsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val roomsModule = module {
    viewModelOf(::RoomsViewModel)
}