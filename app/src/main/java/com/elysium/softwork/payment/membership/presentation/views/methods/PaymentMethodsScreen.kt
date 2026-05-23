package com.elysium.softwork.payment.membership.presentation.views.methods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elysium.softwork.R
import com.elysium.softwork.payment.membership.application.viewmodel.MembershipViewModel
import com.elysium.softwork.payment.membership.domain.model.MembershipPlan
import com.elysium.softwork.payment.membership.domain.model.PaymentMethod
import com.elysium.softwork.payment.membership.presentation.navigation.PaymentRoutes
import com.elysium.softwork.shared.presentation.components.SoftWorkButton
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.Danger
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal

/**
 * "Payment methods" screen — second step of the onboarding gate and also the destination
 * reached from Profile → "Payment methods".
 *
 * Layout: gradient header card recapping the pending charge, list of saved cards (or a
 * structural placeholder when empty), an outline "Add payment method" button, and at the
 * very bottom a primary "Pay membership" CTA. A danger-tinted "Cancel subscription" text
 * link is rendered only when [fromSettings] is `true`.
 *
 * @param planKey stable [MembershipPlan.key] selected on the previous step, or the
 *   [PaymentRoutes.CURRENT_PLAN_SENTINEL] when entering from settings (the screen resolves
 *   the active plan from `MembershipStore.currentPlanKey` in that case).
 * @param fromSettings controls the visibility of the "Cancel subscription" action — `true`
 *   only when the worker reached this screen from Profile.
 * @param onBack invoked when the worker taps the back arrow in the header.
 * @param onAddPaymentMethod invoked when the worker taps "Add payment method".
 * @param onPaymentSucceeded invoked after the 1 s mock-payment delay has elapsed; carries
 *   the resolved [MembershipPlan.key] forward to the success screen.
 */
@Composable
fun PaymentMethodsScreen(
    planKey: String,
    fromSettings: Boolean,
    onBack: () -> Unit,
    onAddPaymentMethod: () -> Unit,
    onPaymentSucceeded: (String) -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    val cards: List<PaymentMethod> by viewModel.paymentMethods.collectAsState()
    val paymentState: MembershipViewModel.PaymentState by viewModel.paymentState.collectAsState()
    val activePlanKey: String? by viewModel.currentPlanKey.collectAsState()

    val resolvedPlanKey: String =
        if (planKey == PaymentRoutes.CURRENT_PLAN_SENTINEL) activePlanKey.orEmpty() else planKey
    val plan: MembershipPlan? = viewModel.availablePlans.firstOrNull { it.key == resolvedPlanKey }

    // Reactive bridge: when the mock payment finishes, navigate forward exactly once and
    // reset the VM stream so the screen does not auto-navigate again on re-entry.
    LaunchedEffect(paymentState) {
        if (paymentState is MembershipViewModel.PaymentState.Succeeded && plan != null) {
            onPaymentSucceeded(plan.key)
            viewModel.consumePaymentState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite),
    ) {
        MethodsHeader(onBack = onBack)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                NextChargeCard(plan = plan)
            }

            if (cards.isEmpty()) {
                item { EmptyCardsPlaceholder() }
            } else {
                items(items = cards, key = { it.id }) { card ->
                    SavedCardRow(card = card)
                }
            }

            item {
                AddMethodRow(onClick = onAddPaymentMethod)
            }

            if (fromSettings) {
                item {
                    CancelSubscriptionRow(onCancel = viewModel::cancelSubscription)
                }
            }
        }

        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            SoftWorkButton(
                text = stringResource(R.string.payment_pay_membership),
                onClick = viewModel::payMembership,
                enabled = cards.isNotEmpty() &&
                    plan != null &&
                    paymentState !is MembershipViewModel.PaymentState.Processing,
            )
        }
    }
}

@Composable
private fun MethodsHeader(onBack: () -> Unit) {
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
            text = stringResource(R.string.payment_methods_title),
            color = PrimaryNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp),
        )
    }
}

@Composable
private fun NextChargeCard(plan: MembershipPlan?) {
    val price: String = plan?.monthlyPrice ?: ""
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(listOf(PrimaryTeal, PrimaryNavy)),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.payment_next_charge),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = price,
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = plan?.name.orEmpty(),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SavedCardRow(card: PaymentMethod) {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandBadge(brand = card.brand)
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.payment_card_label, card.brand, card.last4),
                    color = PrimaryNavy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.payment_expiry_label, card.expiryMonthYear),
                    color = AccentDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun BrandBadge(brand: String) {
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 26.dp)
            .background(color = PrimaryTeal, shape = RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = brand.uppercase().take(4),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EmptyCardsPlaceholder() {
    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.payment_no_cards),
                color = AccentDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun AddMethodRow(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentMint),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = null,
                tint = PrimarySky,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = stringResource(R.string.payment_add_method),
                color = PrimarySky,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CancelSubscriptionRow(onCancel: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCancel)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.payment_cancel_subscription),
            color = Danger,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

