package com.ians.halerchat.feature.auth.login

import com.ians.halerchat.core.data.auth.AuthResult
import com.ians.halerchat.feature.auth.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val repository = FakeAuthRepository()
    private val viewModel by lazy { LoginViewModel(repository) }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun enterCredentials() {
        viewModel.onIntent(LoginIntent.EmailChanged("a@b.c"))
        viewModel.onIntent(LoginIntent.PasswordChanged("secret123"))
    }

    @Test
    fun submit_whileRequestInFlight_showsLoading() = runTest {
        enterCredentials()

        viewModel.onIntent(LoginIntent.Submit)

        assertEquals(
            LoginState(email = "a@b.c", password = "secret123", isLoading = true),
            viewModel.state.value,
        )
    }

    @Test
    fun submit_success_setsLoggedInWithEnteredCredentials() = runTest {
        enterCredentials()

        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(
            LoginState(email = "a@b.c", password = "secret123", isLoggedIn = true),
            viewModel.state.value,
        )
        assertEquals(listOf("a@b.c" to "secret123"), repository.loginCalls)
    }

    @Test
    fun submit_failure_showsErrorAndStopsLoading() = runTest {
        repository.result = AuthResult.InvalidCredentials
        enterCredentials()

        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(
            LoginState(email = "a@b.c", password = "secret123", error = AuthResult.InvalidCredentials),
            viewModel.state.value,
        )
    }

    @Test
    fun typing_afterError_clearsError() = runTest {
        repository.result = AuthResult.InvalidCredentials
        enterCredentials()
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        viewModel.onIntent(LoginIntent.PasswordChanged("secret1234"))

        assertEquals(
            LoginState(email = "a@b.c", password = "secret1234"),
            viewModel.state.value,
        )
    }

    @Test
    fun submit_withBlankFields_doesNotCallRepository() = runTest {
        viewModel.onIntent(LoginIntent.EmailChanged("a@b.c"))

        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(emptyList<Pair<String, String>>(), repository.loginCalls)
        assertEquals(LoginState(email = "a@b.c"), viewModel.state.value)
    }

    @Test
    fun submit_twiceWhileLoading_callsRepositoryOnce() = runTest {
        enterCredentials()

        viewModel.onIntent(LoginIntent.Submit)
        viewModel.onIntent(LoginIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.loginCalls.size)
    }
}
