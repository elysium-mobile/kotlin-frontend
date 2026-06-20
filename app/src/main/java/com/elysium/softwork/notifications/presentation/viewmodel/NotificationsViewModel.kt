package com.elysium.softwork.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.notifications.application.usecase.GetNotificationsUseCase
import com.elysium.softwork.notifications.domain.model.NotificationFeedItem
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import com.elysium.softwork.shared.data.network.BadRequestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the notifications feed.
 *
 * Loads the feed through [GetNotificationsUseCase] on construction and surfaces the latest
 * snapshot through the read-only [notifications] stream. A backend `400 Bad Request` is
 * caught, its [BadRequestException] payload parsed, and the offending `field_errors` message
 * routed into [errorMessage] for inline display. The per-card category theming
 * (`SURVEY` / `PAYMENT` / `FORUM` / `MESSAGE`) is driven entirely by the network category on
 * each [NotificationFeedItem]. No business logic lives here.
 *
 * @param getNotifications builds the worker's notification feed from the backend.
 */
class NotificationsViewModel(
    private val getNotifications: GetNotificationsUseCase,
) : ViewModel() {

    private val _notifications: MutableStateFlow<List<NotificationFeedItem>> = MutableStateFlow(emptyList())
    val notifications: StateFlow<List<NotificationFeedItem>> = _notifications.asStateFlow()

    private val _errorMessage: MutableStateFlow<String?> = MutableStateFlow(null)

    /** Latest backend validation / load error, or `null` when none. Cleared via [consumeError]. */
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refresh()
    }

    /** (Re)loads the feed. A `400` lands its parsed field message on [errorMessage]. */
    fun refresh() {
        viewModelScope.launch {
            getNotifications().fold(
                onSuccess = { feed ->
                    _notifications.value = feed
                    _errorMessage.value = null
                },
                onFailure = { throwable -> _errorMessage.value = resolveError(throwable) },
            )
        }
    }

    /** Clears the surfaced error after the UI has shown it. */
    fun consumeError() {
        _errorMessage.value = null
    }

    /**
     * Maps a failure to a user-facing message. A [BadRequestException] yields the parsed
     * `field_errors` message; anything else yields the exception message or a generic fallback.
     */
    private fun resolveError(throwable: Throwable): String = when (throwable) {
        is BadRequestException -> throwable.response.primaryFieldError() ?: GENERIC_ERROR
        else -> throwable.message ?: GENERIC_ERROR
    }

    companion object {
        private const val GENERIC_ERROR: String = "Unexpected error"

        /**
         * Factory that assembles the use case from the application service locator. The
         * `user_account_id` provider reads `SharedPrefsManager` live on each query.
         *
         * Use it inside Composables:
         * ```
         * val viewModel: NotificationsViewModel =
         *     viewModel(factory = NotificationsViewModel.Factory)
         * ```
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SoftWorkApplication
                val locator = application.serviceLocator
                return NotificationsViewModel(
                    getNotifications = GetNotificationsUseCase(
                        store = locator.notificationStore,
                        accountIdProvider = {
                            locator.sharedPrefsManager
                                .getLong(SharedPrefsManager.KEY_USER_ACCOUNT_ID)
                                .takeIf { it != SharedPrefsManager.DEFAULT_LONG }
                        },
                    ),
                ) as T
            }
        }
    }
}
