package com.elysium.softwork.worker.forum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.application.usecase.GetForumAnonymityUseCase
import com.elysium.softwork.worker.forum.application.usecase.GetPostUseCase
import com.elysium.softwork.worker.forum.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state holder for the thread-detail screen.
 *
 * Loads the original [Post] through [GetPostUseCase] and exposes the forum-anonymity
 * flag so the sticky comment input renders the correct identity. No business logic
 * lives here.
 *
 * Comments are not persisted: the screen renders a static sample list inline so the
 * bubble layout can be reviewed without a backend. A future comment use case can
 * replace the sample data without changing the screen's contract.
 *
 * @param getPost resolves a post by id from the local cache.
 * @param getForumAnonymity reads the persisted forum-anonymity flag.
 */
class ThreadViewModel(
    private val getPost: GetPostUseCase,
    getForumAnonymity: GetForumAnonymityUseCase,
) : ViewModel() {

    private val _post: MutableStateFlow<Post?> = MutableStateFlow(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    /** Resolved once on construction — re-enter the screen to pick up a privacy change. */
    val isAnonymous: Boolean = getForumAnonymity()

    /** Loads the post for [postId] into the [post] stream. */
    fun load(postId: String) {
        viewModelScope.launch {
            _post.value = getPost(postId)
        }
    }

    companion object {
        /** Factory that assembles the use cases from the application service locator. */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return ThreadViewModel(
                    getPost = GetPostUseCase(app.serviceLocator.postStore),
                    getForumAnonymity = GetForumAnonymityUseCase(app.serviceLocator.sharedPrefsManager),
                ) as T
            }
        }
    }
}
