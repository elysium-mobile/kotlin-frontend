package com.elysium.softwork.worker.forum.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.elysium.softwork.SoftWorkApplication
import com.elysium.softwork.shared.utils.values.ForumCategory
import com.elysium.softwork.worker.forum.application.usecase.ObservePostsUseCase
import com.elysium.softwork.worker.forum.application.usecase.RefreshPostsUseCase
import com.elysium.softwork.worker.forum.domain.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state holder for the forum feed screen.
 *
 * Combines the cached posts stream from [ObservePostsUseCase] with the in-memory category
 * filter and exposes a single [posts] stream for the UI. Triggers a one-shot
 * [RefreshPostsUseCase] on construction so the cache updates as soon as the screen
 * mounts. No business logic lives here — both data operations are delegated.
 *
 * @param observePosts streams the cached feed.
 * @param refreshPosts pulls the latest posts into the cache.
 */
class ForumViewModel(
    observePosts: ObservePostsUseCase,
    private val refreshPosts: RefreshPostsUseCase,
) : ViewModel() {

    private val _selectedCategory: MutableStateFlow<ForumCategory?> = MutableStateFlow(null)
    val selectedCategory: StateFlow<ForumCategory?> = _selectedCategory.asStateFlow()

    /**
     * Filtered feed. Re-emits whenever the cache writes new rows or the user changes the
     * category filter.
     */
    val posts: StateFlow<List<Post>> = combine(
        observePosts(),
        _selectedCategory,
    ) { all, category ->
        if (category == null) all else all.filter { it.category == category.key }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch { refreshPosts() }
    }

    /** Applies (or clears, with `null`) the category filter. */
    fun selectCategory(category: ForumCategory?) {
        _selectedCategory.value = category
    }

    /** Pull-to-refresh hook for the feed. */
    fun refresh() {
        viewModelScope.launch { refreshPosts() }
    }

    companion object {
        /**
         * Factory that assembles the forum use cases from the application service locator.
         *
         * Use it inside Composables:
         * ```
         * val viewModel: ForumViewModel = viewModel(factory = ForumViewModel.Factory)
         * ```
         */
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SoftWorkApplication
                val store = app.serviceLocator.postStore
                return ForumViewModel(
                    observePosts = ObservePostsUseCase(store),
                    refreshPosts = RefreshPostsUseCase(store),
                ) as T
            }
        }
    }
}
