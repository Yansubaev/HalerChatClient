package com.ians.halerchat.feature.auth.register

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
class RegisterViewModelTest {
    private val repository = FakeAuthRepository()
    private val viewModel by lazy { RegisterViewModel(repository) }

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun enterFields() {
        viewModel.onIntent(RegisterIntent.NameChanged("Alice"))
        viewModel.onIntent(RegisterIntent.EmailChanged("a@b.c"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("secret123"))
    }

    @Test
    fun submit_whileRequestInFlight_showsLoading() = runTest {
        enterFields()

        viewModel.onIntent(RegisterIntent.Submit)

        assertEquals(
            RegisterState(displayName = "Alice", email = "a@b.c", password = "secret123", isLoading = true),
            viewModel.state.value,
        )
    }

    @Test
    fun submit_success_setsLoggedInWithEnteredFields() = runTest {
        enterFields()

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(
            RegisterState(displayName = "Alice", email = "a@b.c", password = "secret123", isLoggedIn = true),
            viewModel.state.value,
        )
        assertEquals(listOf(Triple("a@b.c", "secret123", "Alice")), repository.registerCalls)
    }

    @Test
    fun submit_trimsEmailAndName_butNotPassword() = runTest {
        viewModel.onIntent(RegisterIntent.NameChanged(" Alice "))
        viewModel.onIntent(RegisterIntent.EmailChanged(" a@b.c "))
        viewModel.onIntent(RegisterIntent.PasswordChanged(" secret123 "))

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(listOf(Triple("a@b.c", " secret123 ", "Alice")), repository.registerCalls)
    }

    @Test
    fun submit_failure_showsErrorAndStopsLoading() = runTest {
        repository.result = AuthResult.EmailTaken
        enterFields()

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(
            RegisterState(displayName = "Alice", email = "a@b.c", password = "secret123", error = AuthResult.EmailTaken),
            viewModel.state.value,
        )
    }

    @Test
    fun typing_afterError_clearsError() = runTest {
        repository.result = AuthResult.EmailTaken
        enterFields()
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        viewModel.onIntent(RegisterIntent.EmailChanged("b@b.c"))

        assertEquals(
            RegisterState(displayName = "Alice", email = "b@b.c", password = "secret123"),
            viewModel.state.value,
        )
    }

    @Test
    fun submit_withBlankName_doesNotCallRepository() = runTest {
        viewModel.onIntent(RegisterIntent.EmailChanged("a@b.c"))
        viewModel.onIntent(RegisterIntent.PasswordChanged("secret123"))
        viewModel.onIntent(RegisterIntent.NameChanged("   "))

        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(emptyList<Triple<String, String, String>>(), repository.registerCalls)
        assertEquals(
            RegisterState(displayName = "   ", email = "a@b.c", password = "secret123"),
            viewModel.state.value,
        )
    }

    @Test
    fun submit_twiceWhileLoading_callsRepositoryOnce() = runTest {
        enterFields()

        viewModel.onIntent(RegisterIntent.Submit)
        viewModel.onIntent(RegisterIntent.Submit)
        advanceUntilIdle()

        assertEquals(1, repository.registerCalls.size)
    }
}
