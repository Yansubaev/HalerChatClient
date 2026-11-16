package com.ians.halerchat.feature.auth.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ians.halerchat.core.data.auth.AuthResult
import com.ians.halerchat.core.designsystem.theme.HalerChatTheme
import com.ians.halerchat.feature.auth.AuthErrorRow
import com.ians.halerchat.feature.auth.AuthHeader
import com.ians.halerchat.feature.auth.PasswordField
import com.ians.halerchat.feature.auth.R
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LoginRoute(
    onLoggedIn: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    LoginScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onRegisterClick = onRegisterClick
    )
}

@Composable
internal fun LoginScreen(
    state: LoginState,
    onIntent: (LoginIntent) -> Unit,
    onRegisterClick: () -> Unit
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            AuthHeader(large = true, title = "HalerChat")
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                modifier = Modifier
                    .semantics { contentType = ContentType.EmailAddress }
                    .fillMaxWidth(),
                value = state.email,
                onValueChange = { onIntent(LoginIntent.EmailChanged(it)) },
                enabled = !state.isLoading,
                label = { Text(stringResource(R.string.auth_email_label)) },
                placeholder = { Text(stringResource(R.string.auth_email_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(16.dp))
            PasswordField(
                modifier = Modifier.semantics { contentType = ContentType.Password },
                value = state.password,
                onValueChange = { onIntent(LoginIntent.PasswordChanged(it)) },
                onDone = { onIntent(LoginIntent.Submit) },
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(16.dp))

            state.error?.let { error ->
                AuthErrorRow(error)
                Spacer(Modifier.height(16.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onIntent(LoginIntent.Submit) },
                enabled = state.canSubmit,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.auth_login_loading))
                } else {
                    Text(stringResource(R.string.auth_login_button))
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onRegisterClick, enabled = !state.isLoading) {
                Text(stringResource(R.string.auth_create_account))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    HalerChatTheme {
        LoginScreen(
            state = LoginState(email = "email@post.com", password = "password"),
            onIntent = {},
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenErrorPreview() {
    HalerChatTheme {
        LoginScreen(
            state = LoginState(
                email = "email@post.com",
                password = "password",
                error = AuthResult.InvalidCredentials
            ),
            onIntent = {},
            onRegisterClick = {}
        )
    }
}
