package com.elysium.softwork.iam.presentation.views.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.iam.application.AuthState
import com.elysium.softwork.iam.application.viewmodel.AuthViewModel
import com.elysium.softwork.iam.presentation.components.BackTopBar
import com.elysium.softwork.iam.presentation.components.RoleSelectorCard
import com.elysium.softwork.shared.utils.discriminators.ButtonVariant
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkTextField
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy

/**
 * Register flow for accounts that authenticated through Google. Email + password are not
 * collected here — the server already has them. The user only chooses a display name and
 * confirms the role.
 */
@Composable
fun RegisterGoogleScreen(
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

    // Inset consumption strategy: the union of `systemBars` and `ime` ensures the
    // `BackTopBar` stays below the status bar and the primary "Sign up" button stays
    // above whichever is taller — the navigation-bar inset or the software keyboard.
    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.ime))
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
                text = stringResource(R.string.register_google_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = AccentDark,
            )

            Spacer(Modifier.height(28.dp))

            SoftWorkTextField(
                value = form.username,
                onValueChange = viewModel::onUsernameChange,
                label = stringResource(R.string.username_label),
            )

            Spacer(Modifier.height(20.dp))

            RoleSelectorCard()

            Spacer(Modifier.height(28.dp))

            SoftWorkButton(
                text = stringResource(R.string.create_account),
                onClick = viewModel::submitRegisterWithGoogle,
                enabled = state !is AuthState.Loading && form.isUsernameValid,
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
