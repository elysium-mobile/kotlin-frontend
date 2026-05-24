package com.elysium.softwork.payment.membership.presentation.views.success

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * Membership-activation confirmation screen.
 *
 * Renders the brand mark, a large checkmark, a title and subtitle, and a bottom-pinned
 * primary CTA. Tapping the CTA delegates to [MembershipViewModel.activateMembership]
 * (which flips the persisted membership flag and stores [planKey] as the active plan)
 * and then invokes [onEnterMainShell] for any non-navigational side effect the host
 * wants to attach. The host above this composable typically observes the membership
 * store's reactive flag and swaps to the main shell automatically.
 *
 * The bottom CTA band consumes the navigation-bar inset so the button always clears the
 * system gesture pill on devices that render one, while still preserving the visual
 * breathing room from the 24.dp bottom padding.
 *
 * @param planKey stable plan identifier of the tier just paid for. Persisted as the
 *   active plan during activation.
 * @param onEnterMainShell invoked after [MembershipViewModel.activateMembership] is
 *   dispatched. The host typically swaps to the main shell via the membership-gate
 *   observer; the callback exists for analytics or logging side effects.
 * @param viewModel provider for the activation coroutine. Resolved through the manual
 *   service locator via the factory exposed on the ViewModel companion.
 */
@Composable
fun PaymentSuccessScreen(
    planKey: String,
    onEnterMainShell: () -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    val onActivate: () -> Unit = remember(viewModel, planKey, onEnterMainShell) {
        {
            viewModel.activateMembership(planKey = planKey)
            onEnterMainShell()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Brand mark rendered through Image (not Icon) and without a tint so the vector's
        // native multi-colour fills survive — a tint here would collapse the lockup to a
        // single flat colour.
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp),
        ) {
            SoftWorkButton(
                text = stringResource(R.string.payment_success_cta),
                onClick = onActivate,
            )
        }
    }
}
