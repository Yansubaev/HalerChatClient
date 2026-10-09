package com.ians.halerchat.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ians.halerchat.core.data.auth.AuthResult

@Composable
internal fun AuthResult.Failure.toErrorText(): String = when (this) {
    AuthResult.EmailTaken -> stringResource(R.string.auth_error_email_taken)
    AuthResult.InvalidCredentials -> stringResource(R.string.auth_error_invalid_credentials)
    AuthResult.Network -> stringResource(R.string.auth_error_network)
    AuthResult.RateLimited -> stringResource(R.string.auth_error_rate_limited)
    AuthResult.Unknown -> stringResource(R.string.auth_error_unknown)
    is AuthResult.Validation -> message ?: stringResource(R.string.auth_error_validation)
}
