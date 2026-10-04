package com.ians.halerchat.core.data.auth

import com.ians.halerchat.core.data.session.Session
import com.ians.halerchat.core.model.User
import com.ians.halerchat.core.network.auth.AuthResultDto
import com.ians.halerchat.core.network.auth.UserDto

internal fun UserDto.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName
)

internal fun AuthResultDto.toSession() = Session(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toDomain()
)