package com.elysium.softwork.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * Circular avatar that displays the user's initials. Used wherever the design calls for
 * a profile thumbnail, and we don't have a remote image yet.
 *
 * @param fullName name to derive the initials from. Up to two glyphs are shown.
 * @param size diameter of the circle. Defaults to 40.dp (header trailing) — pass 56.dp for
 *   the profile hero block.
 * @param background fill color of the circle. Defaults to [PrimarySky].
 * @param contentColor color used for the initials text.
 */
@Composable
fun InitialsAvatar(
    fullName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    background: Color = PrimarySky,
    contentColor: Color = Color.White,
) {
    val initials: String = computeInitials(fullName)
    val textStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        color = contentColor,
    )

    Box(
        modifier = modifier
            .size(size)
            .background(color = background, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initials, style = textStyle)
    }
}

private fun computeInitials(fullName: String): String {
    val parts: List<String> = fullName.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.isEmpty()) return ""
    val first: Char = parts.first().first().uppercaseChar()
    val second: Char? = parts.getOrNull(1)?.first()?.uppercaseChar()
    return if (second != null) "$first$second" else first.toString()
}
