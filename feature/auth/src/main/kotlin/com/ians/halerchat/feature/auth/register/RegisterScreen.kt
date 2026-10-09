package com.ians.halerchat.feature.auth.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ians.halerchat.core.designsystem.theme.HalerChatTheme
import com.ians.halerchat.feature.auth.R
import com.ians.halerchat.feature.auth.toErrorText
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun RegisterRoute(
    onLoggedIn: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    RegisterScreen(
        state = state,
        onIntent = viewModel::onIntent,
        onLoginClick = onLoginClick
    )
}

@Composable
internal fun RegisterScreen(
    state: RegisterState,
    onIntent: (RegisterIntent) -> Unit,
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column {
            TextField(
                modifier = Modifier
                    .semantics { contentType = ContentType.PersonFullName },
                value = state.displayName,
                onValueChange = { onIntent(RegisterIntent.NameChanged(it)) },
                label = {
                    Text(stringResource(R.string.auth_name_label))
                },
                placeholder = {
                    Text(stringResource(R.string.auth_name_placeholder))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.PersonName,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                modifier = Modifier.semantics { contentType = ContentType.EmailAddress },
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                label = {
                    Text(stringResource(R.string.auth_email_label))
                },
                placeholder = {
                    Text(stringResource(R.string.auth_email_placeholder))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(16.dp))
            TextField(
                modifier = Modifier.semantics { contentType = ContentType.NewPassword },
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                label = {
                    Text(stringResource(R.string.auth_password_label))
                },
                placeholder = {
                    Text(stringResource(R.string.auth_password_placeholder))
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    onIntent(RegisterIntent.Submit)
                })
            )
            Spacer(Modifier.height(16.dp))

            state.error?.let { error ->
                Text(error.toErrorText(), color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
            }

            Button(
                onClick = { onIntent(RegisterIntent.Submit) },
                enabled = state.canSubmit,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.auth_register_button))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.auth_have_account))
                TextButton(
                    onClick = onLoginClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(stringResource(R.string.auth_login_button))
                }
            }

        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    HalerChatTheme {
        RegisterScreen(
            state = RegisterState(
                displayName = "Alice",
                email = "email@post.com",
                password = "password",
            ),
            onIntent = {},
            onLoginClick = {}
        )
    }
}