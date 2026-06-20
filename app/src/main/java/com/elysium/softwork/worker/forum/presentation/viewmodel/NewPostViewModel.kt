package com.elysium.softwork.worker.forum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.application.usecase.GetForumAnonymityUseCase
import com.elysium.softwork.shared.data.network.BadRequestException
import com.elysium.softwork.worker.forum.application.usecase.CreateThreadUseCase
import com.elysium.softwork.worker.forum.application.usecase.PostMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the new-thread composer.
 *
 * Creating a discussion is a two-step backend operation: a [CreateThreadUseCase] call
 * followed — when the worker also typed a body — by a [PostMessageUseCase] call seeding the
 * thread's first message (which binds the author's `user_account_id` from prefs). This class
 * buffers the draft, enforces the body cap, and projects the lifecycle into [publishState];
 * a backend `400` is parsed via [BadRequestException] into [PublishState.Error].
 *
 * The forum-anonymity flag is resolved once via [GetForumAnonymityUseCase] for the privacy
 * banner. The legacy category picker is removed: the backend keys threads by a numeric
 * `category_id` the client cannot derive from the `ForumCategory` enum.
 *
 * @param createThread creates the thread.
 * @param postMessage seeds the thread's first message.
 * @param getForumAnonymity reads the persisted forum-anonymity flag.
 * @property maxBodyLength character limit enforced by the body input + the live counter.
 */
class NewPostViewModel(
    private val createThread: CreateThreadUseCase,
    private val postMessage: PostMessageUseCase,
    getForumAnonymity: GetForumAnonymityUseCase,
) : ViewModel() {

    val maxBodyLength: Int = MAX_BODY_LENGTH

    /** Snapshot of the in-progress draft. */
    data class FormState(
        val title: String = "",
        val content: String = "",
    ) {
        val isReadyToPublish: Boolean get() = title.isNotBlank()
    }

    /** Distinct outcome flags so the UI can react and reset. */
    sealed interface PublishState {
        data object Idle : PublishState
        data object Publishing : PublishState
        data object Published : PublishState
        data class Error(val message: String) : PublishState
    }

    private val _form: MutableStateFlow<FormState> = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    private val _publishState: MutableStateFlow<PublishState> = MutableStateFlow(PublishState.Idle)
    val publishState: StateFlow<PublishState> = _publishState.asStateFlow()

    /** Resolved once on construction — re-enter the screen to pick up a privacy change. */
    val isAnonymous: Boolean = getForumAnonymity()

    fun onTitleChange(value: String) {
        _form.value = _form.value.copy(title = value)
    }

    fun onContentChange(value: String) {
        if (value.length > maxBodyLength) return
        _form.value = _form.value.copy(content = value)
    }

    /** Creates the thread (and seeds its first message when a body was typed). */
    fun publish() {
        val current = _form.value
        if (!current.isReadyToPublish) return
        if (_publishState.value is PublishState.Publishing) return

        _publishState.value = PublishState.Publishing
        viewModelScope.launch {
            val result = createThread(title = current.title)
            _publishState.value = result.fold(
                onSuccess = { thread ->
                    val threadId = thread.thread_id
                    if (current.content.isNotBlank() && threadId != 0L) {
                        // Best-effort first message; a failure here does not void the thread.
                        postMessage(threadId, current.content)
                    }
                    PublishState.Published
                },
                onFailure = { PublishState.Error(resolveError(it)) },
            )
        }
    }

    /** Reset the publishing flag once the host has consumed it. */
    fun consumePublishState() {
        _publishState.value = PublishState.Idle
    }

    private fun resolveError(throwable: Throwable): String = when (throwable) {
        is BadRequestException -> throwable.response.primaryFieldError() ?: GENERIC_ERROR
        else -> throwable.message ?: GENERIC_ERROR
    }

    companion object {
        private const val MAX_BODY_LENGTH: Int = 500
        private const val GENERIC_ERROR: String = "Could not publish the thread"

        /** Factory that assembles the use cases from the application service locator. */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                val locator = app.serviceLocator
                return NewPostViewModel(
                    createThread = CreateThreadUseCase(locator.forumStore),
                    postMessage = PostMessageUseCase(locator.forumStore, locator.sharedPrefsManager),
                    getForumAnonymity = GetForumAnonymityUseCase(locator.sharedPrefsManager),
                ) as T
            }
        }
    }
}
