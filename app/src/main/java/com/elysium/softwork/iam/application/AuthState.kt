package com.elysium.softwork.iam.application

import com.elysium.softwork.iam.domain.model.User

/**
 * UI-facing state for IAM flows (login, register, register-with-google). Exposed by
 * [com.elysium.softwork.iam.application.viewmodel.AuthViewModel] as a [kotlinx.coroutines.flow.StateFlow].
 *
 * The states are intentionally coarse — granular field validation lives inside the form
 * state holder, this enum-like sealed hierarchy only describes the request lifecycle.
 */
sealed interface AuthState {

    /** Initial state — no auth call in flight. */
    data object Idle : AuthState

    /** A network call is in progress. UI should disable inputs and show progress. */
    data object Loading : AuthState

    /** Last call completed successfully. The screen should navigate forward. */
    data class Success(val user: User) : AuthState

    /** Last call failed. [message] is a localized, user-facing reason. */
    data class Error(val message: String) : AuthState
}
