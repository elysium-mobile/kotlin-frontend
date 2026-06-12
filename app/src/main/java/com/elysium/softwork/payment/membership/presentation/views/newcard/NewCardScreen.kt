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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.payment.membership.presentation.viewmodel.MembershipViewModel
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkTextField
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal

/**
 * Credit-card composer screen.
 *
 * Layout:
 *  - Header with a back arrow and the localized title.
 *  - High-fidelity credit-card preview that updates as the worker types.
 *  - Four text fields: cardholder name, PAN (digits only), expiry (`MM/YY`), CVV.
 *  - "Save this card" Material 3 [Switch].
 *  - Primary "Add card" CTA, disabled until the form satisfies the ViewModel's minimal
 *    validation contract.
 *
 * The root column is vertically scrollable and pads itself against the IME inset via
 * [Modifier.imePadding]; the focused field is therefore always reachable above the
 * software keyboard without remeasuring the parent layout.
 *
 * @param onBack invoked when the worker taps the header back arrow.
 * @param onCardAdded invoked after the card has been persisted by the ViewModel; the
 *   caller pops the back stack so the worker returns to the methods screen.
 * @param viewModel provider for the form state buffer and the card-add coroutine.
 *   Resolved through the manual service locator via the factory exposed on the ViewModel
 *   companion.
 */
@Composable
fun NewCardScreen(
    onBack: () -> Unit,
    onCardAdded: () -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    val form: MembershipViewModel.CardFormState by viewModel.cardForm.collectAsStateWithLifecycle()

    val onAddCard: () -> Unit = remember(viewModel, onCardAdded) {
        { viewModel.addCard(onAdded = onCardAdded) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite)
            .verticalScroll(rememberScrollState())
            .imePadding(),
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
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                        ),
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
                onClick = onAddCard,
                enabled = form.isValid,
            )

            Spacer(modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Sticky screen header. Left-aligned back arrow and a centered title both rendered in
 * the brand navy weight.
 *
 * @param onBack invoked when the worker taps the back arrow.
 */
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
 * High-fidelity credit-card preview rendered as a linear-gradient surface with overlaid
 * text. Empty fields fall back to localized placeholders so the layout dimensions stay
 * constant across keystrokes and the preview never visually "snaps".
 *
 * The PAN is grouped into four-character chunks separated by spaces; missing positions
 * are padded with the letter `X` to a fixed 16-character width so the displayed string
 * length is deterministic.
 *
 * @param holderName raw cardholder name from the form buffer; rendered uppercase.
 * @param pan raw digit string from the form buffer; grouped and padded for display.
 * @param expiry pre-formatted `MM/YY` string from the form buffer.
 */
@Composable
private fun CreditCardPreview(holderName: String, pan: String, expiry: String) {
    val displayPan: String = remember(pan) { formatPanForPreview(pan) }
    val holderPlaceholder: String = stringResource(R.string.payment_holder_name)
    val expiryPlaceholder: String = stringResource(R.string.payment_expiry_placeholder)
    val displayHolder: String = holderName.ifBlank { holderPlaceholder }.uppercase()
    val displayExpiry: String = expiry.ifBlank { expiryPlaceholder }

    val captionColor: Color = Color.White.copy(alpha = 0.65f)

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
                    color = captionColor,
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
                    color = captionColor,
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
 * Formats a raw digit string as a 16-character preview grouped by four characters with
 * single-space separators. Missing positions are padded with the letter `X` so the
 * preview width stays constant regardless of how many digits the worker has entered.
 *
 * @param pan raw digit string (no separators).
 * @return display-ready string of the form `"4242 4242 XXXX XXXX"`.
 */
private fun formatPanForPreview(pan: String): String {
    val padded: String = pan.padEnd(length = 16, padChar = 'X').take(16)
    return padded
        .chunked(size = 4)
        .joinToString(separator = " ")
}

/**
 * "Save this card" row pairing a brand-colored Material 3 [Switch] with its localized
 * label. The fixed thumb/track palette intentionally bypasses the theme: the switch
 * always reads sky-on-white when checked, mint-on-white when unchecked, so the
 * recommended state stays unambiguous regardless of the surrounding card variant.
 *
 * @param checked current switch value.
 * @param onCheckedChange invoked when the worker toggles the switch.
 */
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
