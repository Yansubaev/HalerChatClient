package com.ians.halerchat.core.data.auth

import com.ians.halerchat.core.model.User

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    sealed interface Failure : AuthResult
    data object InvalidCredentials : Failure
    data object EmailTaken : Failure
    data object RateLimited : Failure
    data class Validation(val message: String?) : Failure
    data object Network : Failure
    data object Unknown : Failure
}