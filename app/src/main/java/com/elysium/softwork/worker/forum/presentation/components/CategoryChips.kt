package com.elysium.softwork.worker.forum.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elysium.softwork.R
import com.elysium.softwork.shared.utils.values.ForumCategory
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * Horizontally scrollable category bar used by the forum feed.
 *
 * The first chip is a localized "All" option that maps to a `null` selection — keeping the
 * feed unfiltered. The remaining chips iterate over [ForumCategory] entries.
 *
 * @param selected currently active category; `null` means "all".
 * @param onSelect invoked with the new selection (or `null` for "all").
 */
@Composable
fun CategoryChips(
    selected: ForumCategory?,
    onSelect: (ForumCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip(
            label = stringResource(R.string.forum_category_all),
            selected = selected == null,
            onClick = { onSelect(null) },
        )
        ForumCategory.entries.forEach { category ->
            Chip(
                label = stringResource(category.labelRes),
                selected = selected == category,
                onClick = { onSelect(category) },
            )
        }
    }
}

/**
 * Inline chip used by [CategoryChips] and the new-post category selector. Pulled out so the
 * two surfaces stay visually consistent.
 */
@Composable
fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    val container: Color = if (selected) PrimarySky else Color.White
    val content: Color = if (selected) Color.White else AccentDark
    val border: BorderStroke? = if (selected) null else BorderStroke(1.dp, AccentMint)

    Surface(
        shape = shape,
        color = container,
        border = border,
        modifier = modifier
            .clickable(onClick = onClick)
            .background(color = container, shape = shape),
    ) {
        Text(
            text = label,
            color = content,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
