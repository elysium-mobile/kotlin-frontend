package com.elysium.softwork.payment.membership.presentation.views.newcard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.payment.membership.application.viewmodel.MembershipViewModel
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkTextField
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal

/**
 * "Add payment method" screen — credit-card composer with a live high-fidelity preview.
 *
 * Layout: header (back arrow + title), [CreditCardPreview] that updates as the worker
 * types, four `SoftWorkTextField` inputs (holder name, PAN, expiry, CVV), the "Save this
 * card" `Switch`, and the primary "Add card" CTA.
 *
 * @param onBack invoked when the worker taps the back arrow.
 * @param onCardAdded invoked once the card has been persisted; the caller pops back to
 *   the methods screen.
 */
@Composable
fun NewCardScreen(
    onBack: () -> Unit,
    onCardAdded: () -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    val form: MembershipViewModel.CardFormState by viewModel.cardForm.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite)
            .verticalScroll(rememberScrollState()),
    ) {
        NewCardHeader(onBack = onBack)

        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            CreditCardPreview(
                holderName = form.holderName,
                pan = form.cardNumber,
                expiry = form.expiry,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SoftWorkTextField(
                value = form.holderName,
                onValueChange = viewModel::onHolderNameChange,
                placeholder = stringResource(R.string.payment_holder_name),
            )
            SoftWorkTextField(
                value = form.cardNumber,
                onValueChange = viewModel::onCardNumberChange,
                placeholder = stringResource(R.string.payment_card_number),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    SoftWorkTextField(
                        value = form.expiry,
                        onValueChange = viewModel::onExpiryChange,
                        placeholder = stringResource(R.string.payment_expiry_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SoftWorkTextField(
                        value = form.cvv,
                        onValueChange = viewModel::onCvvChange,
                        placeholder = stringResource(R.string.payment_cvv_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    )
                }
            }

            SaveCardSwitchRow(
                checked = form.saveCard,
                onCheckedChange = viewModel::onSaveCardChange,
            )

            Spacer(modifier = Modifier.size(8.dp))

            SoftWorkButton(
                text = stringResource(R.string.payment_add_card),
                onClick = { viewModel.addCard(onAdded = onCardAdded) },
                enabled = form.isValid,
            )

            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun NewCardHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.cd_back),
            tint = PrimaryNavy,
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onBack),
        )
        Text(
            text = stringResource(R.string.payment_add_method),
            color = PrimaryNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp),
        )
    }
}

/**
 * High-fidelity credit-card preview. Renders a teal gradient surface and overlays the
 * dynamically-formatted PAN, holder name, and expiry so the worker sees their card take
 * shape as they type. Empty fields are filled with `XXXX`-style placeholders to keep the
 * layout stable.
 *
 * @param holderName cardholder name; falls back to a placeholder when blank.
 * @param pan raw digit string from the form; grouped into 4-character chunks for display.
 * @param expiry pre-formatted `MM/YY` string from the form.
 */
@Composable
private fun CreditCardPreview(holderName: String, pan: String, expiry: String) {
    val displayPan: String = formatPanForPreview(pan)
    val displayHolder: String = holderName.ifBlank { stringResource(R.string.payment_holder_name) }
        .uppercase()
    val displayExpiry: String = expiry.ifBlank { stringResource(R.string.payment_expiry_placeholder) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                brush = Brush.linearGradient(listOf(PrimaryTeal, PrimaryNavy)),
                shape = RoundedCornerShape(20.dp),
            )
            .padding(20.dp),
    ) {
        Text(
            text = displayPan,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Center),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.payment_card_holder_label),
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = displayHolder,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.payment_card_expires_label),
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = displayExpiry,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

/**
 * Formats a raw digit string as a 16-character preview grouped by 4. Missing positions are
 * padded with `X` so the preview length stays stable across keystrokes.
 */
private fun formatPanForPreview(pan: String): String {
    val padded: String = pan.padEnd(length = 16, padChar = 'X').take(16)
    return padded
        .chunked(size = 4)
        .joinToString(separator = " ")
}

@Composable
private fun SaveCardSwitchRow(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimarySky,
                checkedBorderColor = PrimarySky,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = AccentMint,
                uncheckedBorderColor = AccentMint,
            ),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = stringResource(R.string.payment_save_card),
            color = AccentDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
