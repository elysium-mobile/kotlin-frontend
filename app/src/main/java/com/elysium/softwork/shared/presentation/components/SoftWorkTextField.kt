package com.elysium.softwork.shared.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimarySky

/**
 * Brand text input. Filled background ([AccentWhite]), 12.dp radius, 1.dp border that animates
 * from [AccentMint] to [PrimarySky] on focus. Built on top of [OutlinedTextField] so we inherit
 * accessibility, IME handling, and selection toolbars from Material 3.
 *
 * Example:
 * ```
 * SoftWorkTextField(
 *     value = state.email,
 *     onValueChange = viewModel::onEmailChange,
 *     label = stringResource(R.string.email_label),
 *     keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
 * )
 * ```
 *
 * @param value current text value (state-hoisted).
 * @param onValueChange callback invoked when the user types.
 * @param modifier additional layout modifiers; width defaults to [Modifier.fillMaxWidth].
 * @param label optional floating label.
 * @param placeholder optional placeholder shown when the field is empty.
 * @param leadingIcon optional composable rendered before the text (use [androidx.compose.ui.res.painterResource]).
 * @param isError marks the field as invalid; switches the border to [Danger].
 * @param keyboardOptions IME configuration (e.g. email keyboard, capitalization).
 * @param visualTransformation transform applied to the displayed text (e.g. password mask).
 */
@Composable
fun SoftWorkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
    val isFocused: Boolean by interactionSource.collectIsFocusedAsState()

    val targetBorder: Color = when {
        isError -> Danger
        isFocused -> PrimarySky
        else -> AccentMint
    }
    val animatedBorder: Color by animateColorAsState(targetValue = targetBorder, label = "softWorkTextFieldBorder")

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(text = it) } },
        placeholder = placeholder?.let { { Text(text = it) } },
        leadingIcon = leadingIcon,
        isError = isError,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AccentWhite,
            unfocusedContainerColor = AccentWhite,
            disabledContainerColor = AccentWhite,
            errorContainerColor = AccentWhite,
            focusedBorderColor = animatedBorder,
            unfocusedBorderColor = animatedBorder,
            errorBorderColor = animatedBorder,
            focusedTextColor = AccentDark,
            unfocusedTextColor = AccentDark,
            cursorColor = PrimarySky,
        ),
    )
}
