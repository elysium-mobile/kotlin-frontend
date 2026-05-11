package com.elysium.softwork.forum.presentation.views.newpost

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.elysium.softwork.forum.application.viewmodel.NewPostViewModel
import com.elysium.softwork.forum.presentation.components.Chip
import com.elysium.softwork.shared.presentation.components.InitialsAvatar
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * New-post composer.
 *
 * The header doubles as a top bar (close × on the left, "Post" text button on the right).
 * The privacy banner reads `forum_anonymity` via [NewPostViewModel.isAnonymous] — the user
 * does NOT toggle anonymity here; it must be set on the protected-identity screen.
 *
 * @param userName name shown in the privacy banner when posting non-anonymously.
 * @param onClose pop handler for the × button.
 * @param onPublished invoked once [NewPostViewModel] reports `Published`.
 */
@Composable
fun NewPostScreen(
    userName: String,
    onClose: () -> Unit,
    onPublished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewPostViewModel = viewModel(factory = NewPostViewModel.Factory),
) {
    val form: NewPostViewModel.FormState by viewModel.form.collectAsStateWithLifecycle()
    val publishState: NewPostViewModel.PublishState by viewModel.publishState.collectAsStateWithLifecycle()

    LaunchedEffect(publishState) {
        if (publishState is NewPostViewModel.PublishState.Published) {
            onPublished()
            viewModel.consumePublishState()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Header(
            canPublish = form.isReadyToPublish &&
                publishState !is NewPostViewModel.PublishState.Publishing,
            onClose = onClose,
            onPublish = { viewModel.publish(authorName = userName) },
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            IdentityBanner(isAnonymous = viewModel.isAnonymous, userName = userName)

            Spacer(Modifier.height(20.dp))
            CategorySection(
                selected = form.category,
                onSelect = viewModel::selectCategory,
            )

            Spacer(Modifier.height(16.dp))
            TitleField(value = form.title, onValueChange = viewModel::onTitleChange)

            Spacer(Modifier.height(12.dp))
            BodyField(
                value = form.content,
                onValueChange = viewModel::onContentChange,
                maxLength = viewModel.maxBodyLength,
                modifier = Modifier.weight(1f),
            )

            if (publishState is NewPostViewModel.PublishState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = (publishState as NewPostViewModel.PublishState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Toolbar()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Header(canPublish: Boolean, onClose: () -> Unit, onPublish: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.cd_close),
            tint = AccentDark,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onClose),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.forum_new_post),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = PrimaryNavy,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.forum_new_post_publish),
            color = if (canPublish) PrimarySky else PrimarySky.copy(alpha = 0.45f),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .clickable(enabled = canPublish, onClick = onPublish)
                .padding(horizontal = 8.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun IdentityBanner(isAnonymous: Boolean, userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = AccentMint.copy(alpha = 0.20f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isAnonymous) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color = AccentDark.copy(alpha = 0.55f), shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "?", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text(
                text = stringResource(R.string.forum_identity_warning_anon),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = PrimaryNavy,
            )
        } else {
            InitialsAvatar(fullName = userName, size = 32.dp)
            Text(
                text = stringResource(R.string.forum_identity_warning_user, userName),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = PrimaryNavy,
            )
        }
    }
}

@Composable
private fun CategorySection(selected: ForumCategory, onSelect: (ForumCategory) -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.forum_category_label),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = AccentDark,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ForumCategory.entries.forEach { category ->
                Chip(
                    label = stringResource(category.labelRes),
                    selected = category == selected,
                    onClick = { onSelect(category) },
                )
            }
        }
    }
}

@Composable
private fun TitleField(value: String, onValueChange: (String) -> Unit) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            color = PrimaryNavy,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        cursorBrush = SolidColor(PrimarySky),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.forum_post_title_hint),
                        color = AccentDark.copy(alpha = 0.5f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                inner()
            }
        },
    )
}

@Composable
private fun BodyField(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = AccentWhite, shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            textStyle = LocalTextStyle.current.copy(
                color = PrimaryNavy,
                fontSize = 14.sp,
            ),
            cursorBrush = SolidColor(PrimarySky),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.forum_post_hint),
                        color = AccentDark.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                    )
                }
                inner()
            },
        )
        Text(
            text = stringResource(R.string.forum_post_counter, value.length, maxLength),
            color = AccentDark,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun Toolbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ToolbarIcon(
            iconRes = R.drawable.ic_image,
            contentDescription = stringResource(R.string.cd_image),
            onClick = { /* Phase 4 placeholder — image picker arrives in Phase 5. */ },
        )
        ToolbarIcon(
            iconRes = R.drawable.ic_paperclip,
            contentDescription = stringResource(R.string.cd_paperclip),
            onClick = { /* Phase 4 placeholder — attachment picker arrives in Phase 5. */ },
        )
    }
}

@Composable
private fun ToolbarIcon(iconRes: Int, contentDescription: String, onClick: () -> Unit) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = AccentDark,
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick),
    )
}
