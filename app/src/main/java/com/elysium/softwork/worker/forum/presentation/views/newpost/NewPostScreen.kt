package com.elysium.softwork.worker.forum.presentation.views.newpost

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
import com.elysium.softwork.worker.forum.presentation.viewmodel.NewPostViewModel
import com.elysium.softwork.shared.presentation.components.InitialsAvatar
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * New-thread composer.
 *
 * The header doubles as a top bar (close × on the left, "Post" text button on the right). The
 * privacy banner reads `forum_anonymity` via [NewPostViewModel.isAnonymous]. The worker types
 * a title (required) and an optional body that seeds the thread's first message. The legacy
 * category picker is removed — the backend keys threads by a numeric `category_id` the client
 * cannot derive.
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
            onPublish = viewModel::publish,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            IdentityBanner(isAnonymous = viewModel.isAnonymous, userName = userName)

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
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}
