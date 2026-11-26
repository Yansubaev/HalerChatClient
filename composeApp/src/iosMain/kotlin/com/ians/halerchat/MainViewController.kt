package com.ians.halerchat

import androidx.compose.ui.window.ComposeUIViewController
import com.ians.halerchat.di.initKoin

fun MainViewController() = ComposeUIViewController(configure = { initKoin() }) {
    HalerChatApp()
}
