package com.ians.halerchat.core.network.auth

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
internal data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class AuthResultDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val displayName: String
)

