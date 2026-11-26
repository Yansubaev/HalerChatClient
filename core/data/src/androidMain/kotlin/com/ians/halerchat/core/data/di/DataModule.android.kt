package com.ians.halerchat.core.data.di

import com.ians.halerchat.core.data.session.SESSION_FILE
import com.ians.halerchat.core.data.session.createSessionDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal actual val platformDataModule = module {
    single {
        // Тот же файл, что создавал preferencesDataStore(name = "session"):
        // уже залогиненный пользователь после обновления останется в сессии
        createSessionDataStore(
            androidContext().filesDir.resolve("datastore/$SESSION_FILE").absolutePath
        )
    }
}