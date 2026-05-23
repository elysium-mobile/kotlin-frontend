package com.elysium.softwork.iam.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.iam.application.AuthState
import com.elysium.softwork.iam.application.AuthValidation
import com.elysium.softwork.iam.data.store.AuthStore
import com.elysium.softwork.iam.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Hosts UI state for the IAM flows.
 *
 * The ViewModel keeps two streams:
 * - [state] — request lifecycle ([com.elysium.softwork.iam.application.AuthState.Idle], [com.elysium.softwork.iam.application.AuthState.Loading], [com.elysium.softwork.iam.application.AuthState.Success],
 *   [com.elysium.softwork.iam.application.AuthState.Error]). Drives navigation and progress indicators.
 * - [form] — current form values + per-field validation. Drives the input rendering.
 *
 * Validation lives in [com.elysium.softwork.iam.application.AuthValidation] and is recomputed on every keystroke; the screen
 * consumes the derived booleans to decide when to enable the primary button or show error
 * helper text.
 */
class AuthViewModel(private val authStore: AuthStore) : ViewModel() {

    /** Snapshot of the current form. Updated via the `on*Change` handlers. */
    data class FormState(
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val role: String = ROLE_EMPLOYEE,
    ) {
        val isEmailFormatValid: Boolean get() = email.isEmpty() || AuthValidation.isEmailValid(email)
        val isCorporateDomain: Boolean get() = AuthValidation.isCorporateDomain(email)
        val isPasswordValid: Boolean get() = AuthValidation.isPasswordValid(password)
        val passwordsMatch: Boolean get() = AuthValidation.doPasswordsMatch(password, confirmPassword)
        val isUsernameValid: Boolean get() = AuthValidation.isUsernameValid(username)

        /** The Employee client only emits the EMPLOYEE role. */
        companion object {
            const val ROLE_EMPLOYEE: String = "EMPLOYEE"
        }
    }

    private val _state: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _form: MutableStateFlow<FormState> = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    // region Form handlers
    fun onUsernameChange(value: String) {
        _form.value = _form.value.copy(username = value)
    }

    fun onEmailChange(value: String) {
        _form.value = _form.value.copy(email = value)
    }

    fun onPasswordChange(value: String) {
        _form.value = _form.value.copy(password = value)
    }

    fun onConfirmPasswordChange(value: String) {
        _form.value = _form.value.copy(confirmPassword = value)
    }

    fun togglePasswordVisibility() {
        _form.value = _form.value.copy(isPasswordVisible = !_form.value.isPasswordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _form.value = _form.value.copy(isConfirmPasswordVisible = !_form.value.isConfirmPasswordVisible)
    }

    /** Resets the request stream back to [AuthState.Idle] — useful after dismissing an error. */
    fun consumeState() {
        _state.value = AuthState.Idle
    }
    // endregion

    // region Actions
    /**
     * Submits the login form. Strict validation (corporate-email format, 8+ char password)
     * is intentionally relaxed for the mock-auth testing phase — the only requirement is
     * that both fields contain something. Re-tighten this when the real backend is wired
     * up. See `AuthValidation.isEmailValid` / `isPasswordValid` for the production rules.
     */
    fun submitLogin() {
        val current: FormState = _form.value
        if (current.email.isBlank() || current.password.isBlank()) return
        runRequest { authStore.login(current.email.trim(), current.password) }
    }

    /** Submits the standard registration form. No-ops when validation fails. */
    fun submitRegister() {
        val current: FormState = _form.value
        if (!current.isUsernameValid) return
        if (!current.isCorporateDomain) return
        if (!current.isPasswordValid) return
        if (!current.passwordsMatch) return
        runRequest {
            authStore.register(
                username = current.username.trim(),
                email = current.email.trim(),
                password = current.password,
                role = current.role,
            )
        }
    }

    /** Submits the Google-flow registration (only username + role). */
    fun submitRegisterWithGoogle() {
        val current: FormState = _form.value
        if (!current.isUsernameValid) return
        runRequest {
            authStore.registerWithGoogle(username = current.username.trim(), role = current.role)
        }
    }
    // endregion

    private fun runRequest(block: suspend () -> Result<User>) {
        if (_state.value is AuthState.Loading) return
        _state.value = AuthState.Loading
        viewModelScope.launch {
            val result: Result<User> = block()
            _state.value = result.fold(
                onSuccess = { AuthState.Success(it) },
                onFailure = { AuthState.Error(it.message ?: GENERIC_ERROR) },
            )
        }
    }

    companion object {
        private const val GENERIC_ERROR: String = "Unexpected error"

        /**
         * Factory that pulls the [AuthStore] from the [com.elysium.softwork.SoftWorkApplication] service locator.
         *
         * Use it inside Composables:
         * ```
         * val viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
         * ```
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return AuthViewModel(application.serviceLocator.authStore) as T
            }
        }
    }
}