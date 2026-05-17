package com.elysium.softwork.worker.forum.presentation.views.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.shared.utils.values.ForumCategory
import com.elysium.softwork.worker.forum.application.viewmodel.ForumViewModel
import com.elysium.softwork.worker.forum.domain.model.Post
import com.elysium.softwork.worker.forum.presentation.components.AnonymousBadge
import com.elysium.softwork.worker.forum.presentation.components.CategoryChips
import com.elysium.softwork.worker.forum.presentation.components.Chip
import com.elysium.softwork.shared.presentation.components.InitialsAvatar
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.Danger

/**
 * Forum feed (the "Forum" tab destination). Composes the category chip bar above a
 * lazy-loaded list of [PostCard]s, with a circular FAB anchored to the bottom-right that
 * opens the new-post composer.
 *
 * @param onNewPost handler for the "+" FAB tap.
 * @param onOpenThread handler invoked with the post id when a card is tapped.
 * @param onReportPost handler for reporting a post directly from the feed.
 */
@Composable
fun ForumScreen(
    onNewPost: () -> Unit,
    onOpenThread: (String) -> Unit,
    onReportPost: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ForumViewModel = viewModel(factory = ForumViewModel.Factory),
) {
    val posts: List<Post> by viewModel.posts.collectAsStateWithLifecycle()
    val selectedCategory: ForumCategory? by viewModel.selectedCategory.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedHeader()
            CategoryChips(
                selected = selectedCategory,
                onSelect = viewModel::selectCategory,
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 4.dp,
                    bottom = 96.dp,
                ),
            ) {
                items(items = posts, key = { it.id }) { post ->
                    PostCard(
                        post = post, 
                        onClick = { onOpenThread(post.id) },
                        onReport = { onReportPost(post.id) }
                    )
                }
            }
        }

        NewPostFab(
            onClick = onNewPost,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun FeedHeader() {
    Text(
        text = stringResource(R.string.forum_title),
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = PrimaryNavy,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun PostCard(post: Post, onClick: () -> Unit, onReport: () -> Unit) {
    SoftWorkCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthorRow(post = post, onReport = onReport)

            Spacer(Modifier.height(10.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryNavy,
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentDark,
                fontSize = 14.sp,
                maxLines = 3,
            )

            Spacer(Modifier.height(12.dp))
            FooterRow(post = post)
        }
    }
}

@Composable
private fun AuthorRow(post: Post, onReport: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (post.isAnonymous) {
                AnonymousAvatar()
                Spacer(Modifier.size(8.dp))
                AnonymousBadge()
            } else {
                InitialsAvatar(fullName = post.authorName, size = 32.dp)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryNavy,
                )
            }
        }
        
        // Fast Report Icon (Flag)
        Icon(
            painter = painterResource(R.drawable.ic_flag),
            contentDescription = stringResource(R.string.report_title),
            tint = Danger.copy(alpha = 0.6f),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onReport)
        )
    }
}

@Composable
private fun AnonymousAvatar(size: androidx.compose.ui.unit.Dp = 32.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(color = AccentDark.copy(alpha = 0.55f), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun FooterRow(post: Post) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ForumCategory.fromKey(post.category)?.let { category ->
            Chip(label = stringResource(category.labelRes), selected = false, onClick = {})
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.forum_replies_count, post.repliesCount),
            style = MaterialTheme.typography.bodySmall,
            color = AccentDark,
        )
    }
}

@Composable
private fun NewPostFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(
                color = PrimarySky,
                shape = RoundedCornerShape(28.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(R.string.cd_new_post),
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
