package com.elysium.softwork.shared.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal

/**
 * Visual variant of [SoftWorkButton].
 *
 * - [EMPLOYEE] — sky-to-teal horizontal gradient. Default. Used for all Employee-facing
 *   primary actions (sign in, submit check-in, post to forum, etc.).
 * - [HR] — solid [PrimaryNavy]. Reserved for HR-themed primary actions surfaced to the
 *   Employee (e.g. responding to an HR-initiated request).
 */
enum class ButtonVariant { EMPLOYEE, HR }

/**
 * Brand primary button used across SoftWork screens.
 *
 * Spec: full-width, 52.dp tall, 12.dp corner radius. Visual fill is selected by [variant].
 * Disabled state dims the surface to 50% alpha and suppresses the click handler.
 *
 * Example — Employee primary action:
 * ```
 * SoftWorkButton(
 *     text = stringResource(R.string.login_button),
 *     onClick = viewModel::onSignInClicked,
 * )
 * ```
 *
 * Example — HR-themed action:
 * ```
 * SoftWorkButton(
 *     text = stringResource(R.string.acknowledge_hr_request),
 *     onClick = viewModel::onAcknowledge,
 *     variant = ButtonVariant.HR,
 * )
 * ```
 *
 * @param text label rendered inside the button (already localized by the caller).
 * @param onClick invoked on tap when [enabled] is true.
 * @param modifier additional layout modifiers; height and width are enforced internally.
 * @param variant [ButtonVariant] selecting the visual fill. Defaults to [ButtonVariant.EMPLOYEE].
 * @param enabled controls interactivity and visual dimming.
 */
@Composable
fun SoftWorkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.EMPLOYEE,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(12.dp)
    val backgroundModifier = when (variant) {
        ButtonVariant.EMPLOYEE -> Modifier.background(
            brush = Brush.horizontalGradient(listOf(PrimarySky, PrimaryTeal)),
            shape = shape,
        )
        ButtonVariant.HR -> Modifier.background(color = PrimaryNavy, shape = shape)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .then(backgroundModifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
            )
        }
    }
}
