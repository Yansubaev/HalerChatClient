package com.ians.halerchat.feature.auth

import com.ians.halerchat.core.data.auth.AuthRepository
import com.ians.halerchat.core.data.auth.AuthResult
import com.ians.halerchat.core.model.User

internal class FakeAuthRepository : AuthRepository {
    var result: AuthResult = AuthResult.Success(User(id = "1", email = "a@b.c", displayName = "Alice"))
    val loginCalls = mutableListOf<Pair<String, String>>()

    override suspend fun login(email: String, password: String): AuthResult {
        loginCalls += email to password
        return result
    }

    override suspend fun register(email: String, password: String, displayName: String): AuthResult =
        error("register is not faked yet")
}
