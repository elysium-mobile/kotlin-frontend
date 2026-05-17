package com.elysium.softwork.iam.presentation.views.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.iam.application.AuthState
import com.elysium.softwork.iam.application.viewmodel.AuthViewModel
import com.elysium.softwork.iam.presentation.components.BackTopBar
import com.elysium.softwork.iam.presentation.components.PasswordVisibilityToggle
import com.elysium.softwork.iam.presentation.components.RoleSelectorCard
import com.elysium.softwork.iam.presentation.components.VerifiedDomainChip
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkTextField
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy

/**
 * Standard register flow: corporate email + password. The verified-domain chip animates in
 * once the typed email passes both [com.elysium.softwork.iam.application.AuthValidation.isEmailValid]
 * and the corporate-domain check.
 */
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
    val state: AuthState by viewModel.state.collectAsStateWithLifecycle()
    val form: AuthViewModel.FormState by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onRegisterSuccess()
            viewModel.consumeState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        BackTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.create_account),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryNavy,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.corporate_email_only),
                style = MaterialTheme.typography.bodyMedium,
                color = AccentDark,
            )

            Spacer(Modifier.height(28.dp))

            SoftWorkTextField(
                value = form.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.username_label),
            )

            Spacer(Modifier.height(16.dp))

            SoftWorkTextField(
                value = form.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.corporate_email_label_register),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = form.email.isNotEmpty() && !form.isEmailFormatValid,
            )

            AnimatedVisibility(visible = form.isCorporateDomain) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    VerifiedDomainChip()
                }
            }

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

            Spacer(Modifier.height(16.dp))

            SoftWorkTextField(
                value = form.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.confirm_password_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (form.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    PasswordVisibilityToggle(
                        isVisible = form.isConfirmPasswordVisible,
                        onToggle = viewModel::toggleConfirmPasswordVisibility,
                    )
                },
                isError = form.confirmPassword.isNotEmpty() && !form.passwordsMatch,
            )

            Spacer(Modifier.height(20.dp))

            RoleSelectorCard()

            Spacer(Modifier.height(28.dp))

            SoftWorkButton(
                text = stringResource(R.string.create_account),
                onClick = viewModel::submitRegister,
                enabled = state !is AuthState.Loading &&
                    form.isUsernameValid && form.isCorporateDomain &&
                    form.isPasswordValid && form.passwordsMatch,
                variant = ButtonVariant.EMPLOYEE,
            )

            if (state is AuthState.Error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = (state as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
