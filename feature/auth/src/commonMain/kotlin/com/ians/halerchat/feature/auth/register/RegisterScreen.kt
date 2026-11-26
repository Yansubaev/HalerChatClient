package com.ians.halerchat.feature.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import org.jetbrains.compose.resources.stringResource
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
import com.ians.halerchat.feature.auth.resources.*
import org.koin.compose.viewmodel.koinViewModel
import com.ians.halerchat.feature.auth.fieldErrors

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
    // Per spec 3.4: EMAIL_TAKEN belongs to the email field, VALIDATION_FAILED text is "<field>: <message>; ...".
    val validation = (state.error as? AuthResult.Validation)?.fieldErrors().orEmpty()
    val emailError = if (state.error == AuthResult.EmailTaken) {
        stringResource(Res.string.auth_error_email_taken)
    } else {
        validation["email"]
    }
    val passwordError = validation["password"]
    val nameError = validation["displayName"]
    val formError = state.error.takeIf { emailError == null && passwordError == null && nameError == null }

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
            Spacer(Modifier.height(16.dp))
            AuthHeader(large = false, title = stringResource(Res.string.auth_register_title))
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier
                    .semantics { contentType = ContentType.PersonFullName }
                    .fillMaxWidth(),
                value = state.displayName,
                onValueChange = { onIntent(RegisterIntent.NameChanged(it)) },
                enabled = !state.isLoading,
                isError = nameError != null,
                label = { Text(stringResource(Res.string.auth_name_label)) },
                supportingText = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(nameError.orEmpty())
                        Text(
                            stringResource(
                                Res.string.auth_name_counter,
                                state.displayName.length,
                                RegisterState.NAME_MAX_LENGTH
                            )
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier
                    .semantics { contentType = ContentType.EmailAddress }
                    .fillMaxWidth(),
                value = state.email,
                onValueChange = { onIntent(RegisterIntent.EmailChanged(it)) },
                enabled = !state.isLoading,
                isError = emailError != null,
                label = { Text(stringResource(Res.string.auth_email_label)) },
                placeholder = { Text(stringResource(Res.string.auth_email_placeholder)) },
                supportingText = emailError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
            )
            Spacer(Modifier.height(8.dp))
            PasswordField(
                modifier = Modifier.semantics { contentType = ContentType.NewPassword },
                value = state.password,
                onValueChange = { onIntent(RegisterIntent.PasswordChanged(it)) },
                onDone = { onIntent(RegisterIntent.Submit) },
                enabled = !state.isLoading,
                isError = passwordError != null,
                supportingText = passwordError ?: stringResource(Res.string.auth_password_hint),
            )
            Spacer(Modifier.height(16.dp))

            formError?.let { error ->
                AuthErrorRow(error)
                Spacer(Modifier.height(16.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onIntent(RegisterIntent.Submit) },
                enabled = state.canSubmit,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(Res.string.auth_register_loading))
                } else {
                    Text(stringResource(Res.string.auth_register_button))
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onLoginClick, enabled = !state.isLoading) {
                Text(
                    "${stringResource(Res.string.auth_have_account)} ${stringResource(Res.string.auth_login_button)}"
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    HalerChatTheme {
        RegisterScreen(
            state = RegisterState(displayName = "Alice", email = "email@post.com", password = "password1"),
            onIntent = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenErrorPreview() {
    HalerChatTheme {
        RegisterScreen(
            state = RegisterState(
                displayName = "Alice",
                email = "email@post.com",
                password = "password1",
                error = AuthResult.EmailTaken
            ),
            onIntent = {},
            onLoginClick = {}
        )
    }
}
