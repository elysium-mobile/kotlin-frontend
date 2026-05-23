package com.elysium.softwork.iam.presentation.views.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.iam.application.AuthState
import com.elysium.softwork.iam.application.viewmodel.AuthViewModel
import com.elysium.softwork.iam.presentation.components.GoogleOutlineButton
import com.elysium.softwork.iam.presentation.components.PasswordVisibilityToggle
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkTextField
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * Login surface for the SoftWork Employee app.
 *
 * Layout: brand logo + product name → "Bienvenido de vuelta" subtitle → email/password
 * inputs → forgot-password link (right-aligned) → primary "Sign in" button →
 * outline Google button → footer link to register.
 *
 * @param onLoginSuccess invoked when the auth call completes successfully.
 * @param onNavigateToRegister opens the standard register flow.
 * @param onNavigateToRegisterWithGoogle opens the Google-flow register screen.
 * @param onForgotPassword opens the forgot-password flow (currently a no-op placeholder).
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToRegisterWithGoogle: () -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
    val state: AuthState by viewModel.state.collectAsStateWithLifecycle()
    val form: AuthViewModel.FormState by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onLoginSuccess()
            viewModel.consumeState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = stringResource(R.string.cd_logo),
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_welcome_back),
            style = MaterialTheme.typography.bodyLarge,
            color = AccentDark,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(40.dp))

        SoftWorkTextField(
            value = form.email,
            onValueChange = viewModel::onEmailChange,
            label = stringResource(R.string.corporate_email_label),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = form.email.isNotEmpty() && !form.isEmailFormatValid,
        )

        Spacer(Modifier.height(16.dp))

        SoftWorkTextField(
            value = form.password,
            onValueChange = viewModel::onPasswordChange,
            label = stringResource(R.string.password_label),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (form.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                PasswordVisibilityToggle(
                    isVisible = form.isPasswordVisible,
                    onToggle = viewModel::togglePasswordVisibility,
                )
            },
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.forgot_password),
            color = PrimarySky,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp)
                .clickableText(onClick = onForgotPassword),
            textAlign = TextAlign.End,
        )

        Spacer(Modifier.height(20.dp))

        // Validation relaxed for the mock-auth testing phase — both fields just need a
        // value. Real format/length checks return once the backend is integrated; the
        // helpers in AuthValidation are preserved for that switch-back.
        SoftWorkButton(
            text = stringResource(R.string.login_button),
            onClick = viewModel::submitLogin,
            enabled = state !is AuthState.Loading &&
                form.email.isNotBlank() &&
                form.password.isNotBlank(),
            variant = ButtonVariant.EMPLOYEE,
        )

        Spacer(Modifier.height(12.dp))

        GoogleOutlineButton(
            onClick = onNavigateToRegisterWithGoogle,
            enabled = state !is AuthState.Loading,
        )

        if (state is AuthState.Error) {
            Spacer(Modifier.height(12.dp))
            ErrorText(message = (state as AuthState.Error).message)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.register_link),
            color = AccentDark,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .clickableText(onClick = onNavigateToRegister),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Tiny helper to keep clickable text declarations terse. */
private fun Modifier.clickableText(onClick: () -> Unit): Modifier =
    this.padding(vertical = 4.dp).clickable(onClick = onClick)
