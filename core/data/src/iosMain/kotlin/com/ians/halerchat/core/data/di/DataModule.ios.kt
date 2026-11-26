package com.ians.halerchat.core.data.di

import com.ians.halerchat.core.data.session.SESSION_FILE
import com.ians.halerchat.core.data.session.createSessionDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual val platformDataModule = module {
    single {
        val documents = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        createSessionDataStore(requireNotNull(documents).path + "/$SESSION_FILE")
    }
}