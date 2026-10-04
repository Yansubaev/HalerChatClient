package com.ians.halerchat

import android.app.Application
import com.ians.halerchat.core.data.di.dataModule
import com.ians.halerchat.feature.auth.di.authModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class HalerChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@HalerChatApplication)
            modules(dataModule, authModule)
        }
    }
}