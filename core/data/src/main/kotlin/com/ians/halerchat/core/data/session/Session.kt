package com.ians.halerchat.core.data.session

import com.ians.halerchat.core.model.User

internal data class Session(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)