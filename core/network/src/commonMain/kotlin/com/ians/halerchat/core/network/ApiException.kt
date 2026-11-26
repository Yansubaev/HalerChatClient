package com.ians.halerchat.core.network

class ApiException(
    val status: Int,
    val code: String?,
    message: String?
) : Exception(message)