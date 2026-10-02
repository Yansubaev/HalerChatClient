package com.ians.halerchat.core.network

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val error: String? = null,
    val code: String? = null,
    val message: String? = null
)