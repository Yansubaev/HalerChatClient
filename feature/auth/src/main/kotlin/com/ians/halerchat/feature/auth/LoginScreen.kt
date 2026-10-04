package com.ians.halerchat.feature.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ians.halerchat.core.data.auth.AuthResult
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LoginRoute(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    LoginScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
internal fun LoginScreen(state: LoginState, onIntent: (LoginIntent) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            TextField(
                value = state.email,
                onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                label = {
                    Text("Email")
                },
                placeholder = {
                    Text("Enter your email")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                value = state.password,
                onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                label = {
                    Text("Password")
                },
                placeholder = {
                    Text("Enter your password")
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            )
            Spacer(Modifier.height(16.dp))

            state.error?.let { error ->
                val errorText = when (error) {
                    AuthResult.EmailTaken -> "Email is already taken"
                    AuthResult.InvalidCredentials -> "Wrong email or password"
                    AuthResult.Network -> "Check your internet connection"
                    AuthResult.RateLimited -> "Hold on, the server is busy"
                    AuthResult.Unknown -> "An unexpected error occurred"
                    is AuthResult.Validation -> error.message ?: "Invalid input"
                }
                Text(errorText, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = { onIntent(LoginIntent.Submit) },
                enabled = state.canSubmit,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Login")
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        state = LoginState(
            email = "email@post.com",
            password = "password",
        ),
        onIntent = {}
    )
}