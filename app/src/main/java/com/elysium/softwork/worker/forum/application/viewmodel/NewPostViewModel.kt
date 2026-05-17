package com.elysium.softwork.worker.forum.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.utils.values.ForumCategory
import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State holder for the new-post composer.
 *
 * On construction, it reads `forum_anonymity` from [SharedPrefsManager] and surfaces it via
 * [isAnonymous]. The composer renders the privacy banner from this flag — the user does
 * NOT toggle anonymity here; that's owned by the protected-identity screen.
 *
 * @property maxBodyLength character limit enforced by the body input + the live counter.
 */
class NewPostViewModel(
    private val store: PostStore,
    prefs: SharedPrefsManager,
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
    val isAnonymous: Boolean = prefs.getBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY)

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

    fun publish(authorName: String) {
        val current = _form.value
        if (!current.isReadyToPublish) return
        if (_publishState.value is PublishState.Publishing) return

        _publishState.value = PublishState.Publishing
        viewModelScope.launch {
            val result = store.publish(
                title = current.title.trim(),
                content = current.content.trim(),
                category = current.category.key,
                authorName = if (isAnonymous) "" else authorName,
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

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return NewPostViewModel(
                    store = app.serviceLocator.postStore,
                    prefs = app.serviceLocator.sharedPrefsManager,
                ) as T
            }
        }
    }
}
