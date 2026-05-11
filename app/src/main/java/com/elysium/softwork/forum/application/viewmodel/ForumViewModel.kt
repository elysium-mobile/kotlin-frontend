package com.elysium.softwork.forum.application.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.forum.application.ForumCategory
import com.elysium.softwork.forum.data.store.PostStore
import com.elysium.softwork.forum.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Feed ViewModel for [com.elysium.softwork.forum.presentation.views.feed.ForumScreen].
 *
 * Combines the cached posts flow from [PostStore] with the in-memory category filter and
 * exposes a single [posts] stream for the UI. Triggers a one-shot [PostStore.refresh] on
 * construction so the cache is updated as soon as the screen mounts.
 */
class ForumViewModel(private val store: PostStore) : ViewModel() {

    private val _selectedCategory: MutableStateFlow<ForumCategory?> = MutableStateFlow(null)
    val selectedCategory: StateFlow<ForumCategory?> = _selectedCategory.asStateFlow()

    /**
     * Filtered feed. Re-emits whenever Room writes new rows or the user changes the category.
     */
    val posts: StateFlow<List<Post>> = combine(
        store.observe(),
        _selectedCategory,
    ) { all, category ->
        if (category == null) all else all.filter { it.category == category.key }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch { store.refresh() }
    }

    fun selectCategory(category: ForumCategory?) {
        _selectedCategory.value = category
    }

    /** Pull-to-refresh hook for the feed. */
    fun refresh() {
        viewModelScope.launch { store.refresh() }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                return ForumViewModel(app.serviceLocator.postStore) as T
            }
        }
    }
}
