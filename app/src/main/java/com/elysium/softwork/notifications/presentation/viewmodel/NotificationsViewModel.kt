package com.elysium.softwork.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.notifications.application.usecase.GetNotificationsUseCase
import com.elysium.softwork.notifications.domain.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the notifications feed.
 *
 * Subscribes to [GetNotificationsUseCase] on construction and surfaces the latest
 * snapshot through the read-only [notifications] stream for the Compose layer to render.
 * No business logic lives here.
 *
 * @param getNotifications streams the worker's in-app notification feed.
 */
class NotificationsViewModel(getNotifications: GetNotificationsUseCase) : ViewModel() {

    private val _notifications: MutableStateFlow<List<Notification>> = MutableStateFlow(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            getNotifications().collect { list -> _notifications.value = list }
        }
    }

    companion object {
        /**
         * Factory that assembles the use case from the application service locator.
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
                return NotificationsViewModel(
                    getNotifications = GetNotificationsUseCase(application.serviceLocator.notificationStore),
                ) as T
            }
        }
    }
}
