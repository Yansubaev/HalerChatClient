package com.ians.halerchat.core.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun defaultEngine(): HttpClientEngine = OkHttp.create()