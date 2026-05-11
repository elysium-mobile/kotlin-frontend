package com.elysium.softwork.forum.presentation.views.thread

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.forum.application.ForumCategory
import com.elysium.softwork.forum.application.viewmodel.ThreadViewModel
import com.elysium.softwork.forum.domain.model.Post
import com.elysium.softwork.forum.presentation.components.AnonymousBadge
import com.elysium.softwork.forum.presentation.components.Chip
import com.elysium.softwork.shared.presentation.components.InitialsAvatar
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * Thread-detail screen. Shows the original post at the top followed by a static sample
 * comment list (Phase 5 will replace these with real Comment data). The bottom sticky input
 * row reads `forum_anonymity` from [ThreadViewModel.isAnonymous] to decide which avatar +
 * caption to render next to the input field.
 */
@Composable
fun ThreadScreen(
    postId: String,
    userName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ThreadViewModel = viewModel(factory = ThreadViewModel.Factory),
) {
    val post: Post? by viewModel.post.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        ThreadHeader(onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 8.dp,
                bottom = 16.dp,
            ),
        ) {
            post?.let { current ->
                item { OriginalPost(post = current) }
                item {
                    Text(
                        text = stringResource(R.string.forum_thread_replies_section),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = AccentDark,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                    )
                }
                items(items = SAMPLE_COMMENTS, key = { it.id }) { comment ->
                    CommentBubble(comment = comment)
                }
            }
        }

        StickyCommentInput(isAnonymous = viewModel.isAnonymous, userName = userName)
    }
}

/** Inline placeholder comment used in Phase 4 — replaced with a Comment domain in Phase 5. */
private data class SampleComment(
    val id: String,
    val authorName: String,
    val isAnonymous: Boolean,
    val text: String,
)

private val SAMPLE_COMMENTS: List<SampleComment> = listOf(
    SampleComment("c1", "María López", false, "Totalmente de acuerdo, deberíamos abrir un canal."),
    SampleComment("c2", "", true, "Yo también lo viví la semana pasada, gracias por levantarlo."),
    SampleComment("c3", "Diego Salas", false, "¿Quién toma la iniciativa para hablar con RRHH?"),
)

@Composable
private fun ThreadHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.cd_back),
            tint = AccentDark,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBack),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.forum_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun OriginalPost(post: Post) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (post.isAnonymous) {
                    AnonymousAvatar(size = 36.dp)
                    Spacer(Modifier.size(8.dp))
                    AnonymousBadge()
                } else {
                    InitialsAvatar(fullName = post.authorName, size = 36.dp)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryNavy,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryNavy,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentDark,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
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
    }
}

@Composable
private fun CommentBubble(comment: SampleComment) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp),
        color = AccentWhite,
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (comment.isAnonymous) {
                    AnonymousAvatar(size = 24.dp)
                    Spacer(Modifier.size(6.dp))
                    AnonymousBadge()
                } else {
                    InitialsAvatar(fullName = comment.authorName, size = 24.dp)
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryNavy,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = AccentDark,
            )
        }
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
        Text(text = "?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value / 2).sp)
    }
}

@Composable
private fun StickyCommentInput(isAnonymous: Boolean, userName: String) {
    var draft: String by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isAnonymous) {
                AnonymousAvatar(size = 32.dp)
            } else {
                InitialsAvatar(fullName = userName, size = 32.dp)
            }
            Spacer(Modifier.size(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(color = AccentWhite, shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (draft.isEmpty()) {
                    Text(
                        text = stringResource(R.string.forum_thread_comment_hint),
                        color = AccentDark.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = PrimaryNavy, fontSize = 14.sp),
                    cursorBrush = SolidColor(PrimarySky),
                )
            }

            Spacer(Modifier.size(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = PrimarySky, shape = CircleShape)
                    .clickable(enabled = draft.isNotBlank()) {
                        // Phase 5: persist comment via a future CommentStore.
                        draft = ""
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = stringResource(R.string.cd_send),
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
