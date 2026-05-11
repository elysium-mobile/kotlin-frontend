package com.elysium.softwork.forum.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elysium.softwork.R
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy

/**
 * Pill-shaped badge rendered in place of the author name when the post is anonymous.
 * Background [AccentMint], text [PrimaryNavy]
 */
@Composable
fun AnonymousBadge(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.forum_anonymous_author),
        color = PrimaryNavy,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        modifier = modifier
            .background(color = AccentMint, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
