package com.ians.halerchat.core.network

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiErrorDto(
    val error: String? = null,
    val code: String? = null,
    val message: String? = null
)