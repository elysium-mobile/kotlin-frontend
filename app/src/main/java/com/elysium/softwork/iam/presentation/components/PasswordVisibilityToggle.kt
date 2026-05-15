package com.elysium.softwork.iam.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.elysium.softwork.R

/**
 * Eye icon used as a `trailingIcon` slot on password fields. Clicking flips between
 * `ic_visibility` and `ic_visibility_off` and reports the change via [onToggle].
 *
 * @param isVisible whether the password is currently rendered in plain text.
 * @param onToggle callback invoked when the user taps the icon.
 */
@Composable
fun PasswordVisibilityToggle(
    isVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(
            id = if (isVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
        ),
        contentDescription = stringResource(R.string.cd_toggle_password_visibility),
        modifier = modifier.clickable(onClick = onToggle),
    )
}
