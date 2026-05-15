package com.elysium.softwork.shared.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elysium.softwork.shared.presentation.theme.AccentWhite

/**
 * Brand surface card. White background, 1.dp [AccentWhite] border, 16.dp corner radius and a
 * 4.dp shadow. The composable is content-agnostic — callers compose any layout inside.
 *
 * Example:
 * ```
 * SoftWorkCard {
 *     Column(Modifier.padding(16.dp)) {
 *         Text("Daily check-in")
 *         SoftWorkButton(text = "Submit", onClick = onSubmit)
 *     }
 * }
 * ```
 *
 * @param modifier additional layout modifiers applied to the underlying [Surface].
 * @param content composable subtree rendered inside the card.
 */
@Composable
fun SoftWorkCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, AccentWhite),
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        content = content,
    )
}
