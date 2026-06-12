package com.elysium.softwork.worker.forum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.application.usecase.GetForumAnonymityUseCase
import com.elysium.softwork.shared.utils.values.ForumCategory
import com.elysium.softwork.worker.forum.application.usecase.PublishPostUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the new-post composer.
 *
 * Publication is delegated to [PublishPostUseCase] (which owns trimming and the
 * anonymous-author blanking rule); this class buffers the draft, enforces the body
 * character cap as a typing constraint, and projects the request lifecycle into
 * [publishState].
 *
 * On construction the forum-anonymity flag is resolved once via
 * [GetForumAnonymityUseCase] and surfaced through [isAnonymous]. The composer renders
 * the privacy banner from this flag — the user does NOT toggle anonymity here; that is
 * owned by the protected-identity screen. Re-enter the composer to pick up a change.
 *
 * @param publishPost publishes the assembled draft.
 * @param getForumAnonymity reads the persisted forum-anonymity flag.
 * @property maxBodyLength character limit enforced by the body input + the live counter.
 */
class NewPostViewModel(
    private val publishPost: PublishPostUseCase,
    getForumAnonymity: GetForumAnonymityUseCase,
) : ViewModel() {

    val maxBodyLength: Int = MAX_BODY_LENGTH

    /** Snapshot of the in-progress draft. */
    data class FormState(
        val title: String = "",
        val content: String = "",
        val category: ForumCategory = ForumCategory.SUGGESTIONS,
    ) {
        val isReadyToPublish: Boolean
            get() = title.isNotBlank() && content.isNotBlank()
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

    fun selectCategory(category: ForumCategory) {
        _form.value = _form.value.copy(category = category)
    }

    /** Publishes the current draft under [authorName] (blanked downstream when anonymous). */
    fun publish(authorName: String) {
        val current = _form.value
        if (!current.isReadyToPublish) return
        if (_publishState.value is PublishState.Publishing) return

        _publishState.value = PublishState.Publishing
        viewModelScope.launch {
            val result = publishPost(
                title = current.title,
                content = current.content,
                categoryKey = current.category.key,
                authorName = authorName,
                isAnonymous = isAnonymous,
            )
            _publishState.value = result.fold(
                onSuccess = { PublishState.Published },
                onFailure = { PublishState.Error(it.message ?: GENERIC_ERROR) },
            )
        }
    }

    /** Reset the publishing flag once the host has consumed it (e.g. after navigating back). */
    fun consumePublishState() {
        _publishState.value = PublishState.Idle
    }

    companion object {
        private const val MAX_BODY_LENGTH: Int = 500
        private const val GENERIC_ERROR: String = "Could not publish the post"

        /** Factory that assembles the use cases from the application service locator. */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return NewPostViewModel(
                    publishPost = PublishPostUseCase(app.serviceLocator.postStore),
                    getForumAnonymity = GetForumAnonymityUseCase(app.serviceLocator.sharedPrefsManager),
                ) as T
            }
        }
    }
}
