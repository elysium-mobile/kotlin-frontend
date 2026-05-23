package com.elysium.softwork.worker.forum.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.worker.forum.data.store.PostStore
import com.elysium.softwork.worker.forum.domain.model.Post
import com.elysium.softwork.shared.data.local.SharedPrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State holder for the thread-detail screen. Loads the original [Post] from [PostStore] by
 * id and exposes the `forum_anonymity` flag so the sticky comment input can render the
 * correct identity.
 *
 * Comments are not persisted: the screen renders a static sample list inline so the bubble
 * layout can be reviewed without a backend. A future Comment store can replace the sample
 * data without changing the screen's contract.
 */
class ThreadViewModel(
    private val store: PostStore,
    prefs: SharedPrefsManager,
) : ViewModel() {

    private val _post: MutableStateFlow<Post?> = MutableStateFlow(null)
    val post: StateFlow<Post?> = _post.asStateFlow()

    /** Resolved once on construction — re-enter the screen to pick up a privacy change. */
    val isAnonymous: Boolean = prefs.getBoolean(SharedPrefsManager.KEY_FORUM_ANONYMITY)

    fun load(postId: String) {
        viewModelScope.launch {
            _post.value = store.getById(postId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return ThreadViewModel(
                    store = app.serviceLocator.postStore,
                    prefs = app.serviceLocator.sharedPrefsManager,
                ) as T
            }
        }
    }
}
