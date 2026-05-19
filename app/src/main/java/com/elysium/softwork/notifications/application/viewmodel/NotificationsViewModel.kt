package com.elysium.softwork.notifications.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.notifications.data.store.NotificationStore
import com.elysium.softwork.notifications.domain.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Hosts the list of in-app notifications. Subscribes to [NotificationStore.getNotifications]
 * on construction and surfaces the latest snapshot through [notifications] for the Compose
 * layer to render.
 */
class NotificationsViewModel(
    private val notificationStore: NotificationStore,
) : ViewModel() {

    private val _notifications: MutableStateFlow<List<Notification>> = MutableStateFlow(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            notificationStore.getNotifications().collect { list -> _notifications.value = list }
        }
    }

    companion object {
        /**
         * Factory that pulls the [NotificationStore] from the [SoftWorkApplication] service
         * locator.
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
                return NotificationsViewModel(application.serviceLocator.notificationStore) as T
            }
        }
    }
}
