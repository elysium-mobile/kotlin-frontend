package com.elysium.softwork.feedback.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.feedback.data.store.FeedbackStore
import com.elysium.softwork.feedback.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State holder for the AI chat surface.
 *
 * Two read-only streams are exposed:
 *  - [messages] reflects the [FeedbackStore.conversation] log directly. Reads are cheap
 *    because the underlying flow is a [StateFlow] — the screen renders the current value
 *    synchronously on first composition without a default-state flash.
 *  - [isSending] is a transient UI flag set while the ViewModel is awaiting an AI reply.
 *    Drives the "typing" indicator and the disabled state on the send button.
 *
 * The companion factory wires the [FeedbackStore] dependency from the application's
 * manual service locator via [CreationExtras], following the same pattern as every other
 * SoftWork ViewModel.
 *
 * @param store data port used to read the conversation log and submit new messages.
 */
class AiChatViewModel(private val store: FeedbackStore) : ViewModel() {

    /** Immutable read-only view of the conversation log. */
    val messages: StateFlow<List<ChatMessage>> = store.conversation

    private val _isSending: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * `true` while the ViewModel is awaiting an AI reply for the most recent worker
     * message. The screen consumes this to render the typing indicator and to gate the
     * send button so the worker cannot enqueue a second message mid-round-trip.
     */
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    /**
     * Submits [content] to the [FeedbackStore]. No-ops when [content] is blank or while a
     * previous send is still in flight, so repeated taps cannot fan-out duplicate
     * messages.
     *
     * The state flag transitions are: `false` → `true` on entry, then back to `false`
     * in a `finally` block so a future failure mode (network exception, cancellation)
     * does not strand the UI in the loading state.
     *
     * @param content text typed by the worker.
     */
    fun send(content: String) {
        if (content.isBlank() || _isSending.value) return
        viewModelScope.launch {
            _isSending.value = true
            try {
                store.send(content)
            } finally {
                _isSending.value = false
            }
        }
    }

    companion object {
        /**
         * Factory that pulls the [FeedbackStore] from the [SoftWorkApplication] service
         * locator.
         *
         * Use it inside Composables:
         * ```
         * val viewModel: AiChatViewModel = viewModel(factory = AiChatViewModel.Factory)
         * ```
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as SoftWorkApplication
                return AiChatViewModel(application.serviceLocator.feedbackStore) as T
            }
        }
    }
}
