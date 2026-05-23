package com.elysium.softwork.payment.membership.presentation.views.success

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.payment.membership.application.viewmodel.MembershipViewModel
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal

/**
 * "Membership activated" confirmation screen.
 *
 * Renders the SoftWork lockup, a large checkmark, the success title, and a primary
 * "Main menu" CTA. Tapping the CTA activates the membership in the store (flipping
 * `KEY_HAS_MEMBERSHIP` to `true` and persisting [planKey]) and then defers to
 * [onEnterMainShell], which lets the host Activity unmount the payment graph in favour of
 * the main app shell.
 *
 * @param planKey stable [com.elysium.softwork.payment.membership.domain.model.MembershipPlan.key]
 *   the worker just paid for. Persisted as the active plan on activation.
 * @param onEnterMainShell invoked AFTER the activation call returns; the host swaps to
 *   `MainNavHost`.
 */
@Composable
fun PaymentSuccessScreen(
    planKey: String,
    onEnterMainShell: () -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Brand mark sourced from the adaptive launcher foreground so the in-app branding
        // matches the device launcher exactly. Rendered via `Image` (not `Icon`) and with
        // NO `tint` parameter so the vector's native gradients and multi-colour fills
        // survive — applying a tint here would collapse the asset to a single flat colour.
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(72.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = PrimaryTeal,
                modifier = Modifier.size(96.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.payment_success_title),
            color = PrimaryNavy,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.payment_success_subtitle),
            color = AccentDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            SoftWorkButton(
                text = stringResource(R.string.payment_success_cta),
                onClick = {
                    viewModel.activateMembership(planKey = planKey)
                    onEnterMainShell()
                },
            )
        }
    }
}
