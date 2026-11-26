package com.ians.halerchat.di

import com.ians.halerchat.core.data.di.dataModule
import com.ians.halerchat.feature.auth.di.authModule
import com.ians.halerchat.feature.rooms.di.roomsModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

// Один список модулей на обе платформы. Android добавляет своё через config
fun initKoin(config: KoinAppDeclaration = {}) {
    startKoin {
        config()
        modules(appModule, dataModule, authModule, roomsModule)
    }
}
