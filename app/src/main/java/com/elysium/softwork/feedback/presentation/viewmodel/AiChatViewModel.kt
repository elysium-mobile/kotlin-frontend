package com.elysium.softwork.feedback.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.feedback.application.usecase.ObserveConversationUseCase
import com.elysium.softwork.feedback.application.usecase.SendChatMessageUseCase
import com.elysium.softwork.feedback.domain.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the AI chat surface.
 *
 * The ViewModel owns no business logic: conversation access and message dispatch are
 * delegated to application-layer use cases. Two read-only streams remain:
 *  - [messages] — the conversation log, proxied from the observe use case. Reads are
 *    cheap because the underlying stream is hot — the screen renders the current value
 *    synchronously on first composition without a default-state flash.
 *  - [isSending] — transient flag set while a reply round-trip is in flight. Drives the
 *    typing indicator and gates the send button.
 *
 * @param observeConversation streams the conversation log.
 * @param sendChatMessage dispatches the worker's message and awaits the reply.
 */
class AiChatViewModel(
    observeConversation: ObserveConversationUseCase,
    private val sendChatMessage: SendChatMessageUseCase,
) : ViewModel() {

    /** Immutable read-only view of the conversation log. */
    val messages: StateFlow<List<ChatMessage>> = observeConversation()

    private val _isSending: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * `true` while a reply round-trip is in flight for the most recent worker message.
     * The screen consumes this to render the typing indicator and to gate the send
     * button so the worker cannot enqueue a second message mid-round-trip.
     */
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    /**
     * Submits [content] through the use case. No-ops when [content] is blank or while a
     * previous send is still in flight, so repeated taps cannot fan-out duplicates.
     *
     * The flag transitions are: `false` → `true` on entry, then back to `false` in a
     * `finally` block so a future failure mode (network exception, cancellation) does
     * not strand the UI in the loading state.
     *
     * @param content text typed by the worker.
     */
    fun send(content: String) {
        if (content.isBlank() || _isSending.value) return
        viewModelScope.launch {
            _isSending.value = true
            try {
                sendChatMessage(content)
            } finally {
                _isSending.value = false
            }
        }
    }

    companion object {
        /**
         * Factory that assembles the feedback use cases from the application service
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
                val store = application.serviceLocator.feedbackStore
                return AiChatViewModel(
                    observeConversation = ObserveConversationUseCase(store),
                    sendChatMessage = SendChatMessageUseCase(store),
                ) as T
            }
        }
    }
}
