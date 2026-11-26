package com.ians.halerchat

import android.app.Application
import com.ians.halerchat.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class HalerChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@HalerChatApplication)
        }
    }
}
