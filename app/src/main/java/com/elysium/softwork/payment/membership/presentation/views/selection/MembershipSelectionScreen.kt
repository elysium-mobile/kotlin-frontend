package com.elysium.softwork.payment.membership.presentation.views.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.elysium.softwork.shared.presentation.components.SoftWorkCard
import com.elysium.softwork.shared.presentation.theme.AccentDark
import com.elysium.softwork.shared.presentation.theme.AccentMint
import com.elysium.softwork.shared.presentation.theme.AccentWhite
import com.elysium.softwork.shared.presentation.theme.PrimaryNavy
import com.elysium.softwork.shared.presentation.theme.PrimarySky
import com.elysium.softwork.shared.presentation.theme.PrimaryTeal

/**
 * "Memberships" screen — first step of the payment gate.
 *
 * Renders the catalogue exposed by [MembershipViewModel.availablePlans]. Plans marked with
 * `isRecommended = true` use the PrimaryTeal accent (border, price, primary button) to
 * steer the worker toward the upsell; the rest fall back to the neutral PrimarySky variant.
 *
 * @param onPlanSelected invoked when the worker taps "Select plan" on a card; carries the
 *   stable [MembershipPlan.key] forward to the methods screen.
 */
@Composable
fun MembershipSelectionScreen(
    onPlanSelected: (String) -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite),
    ) {
        SelectionHeader()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = viewModel.availablePlans, key = { it.key }) { plan ->
                PlanCard(
                    plan = plan,
                    onSelect = { onPlanSelected(plan.key) },
                )
            }
        }
    }
}

@Composable
private fun SelectionHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = stringResource(R.string.payment_memberships_title),
            color = PrimaryNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PlanCard(plan: MembershipPlan, onSelect: () -> Unit) {
    val accent: Color = if (plan.isRecommended) PrimaryTeal else PrimarySky

    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = plan.name,
                color = if (plan.isRecommended) PrimaryTeal else PrimaryNavy,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = plan.monthlyPrice,
                    color = accent,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.payment_price_suffix),
                    color = AccentDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            plan.features.forEach { feature ->
                FeatureRow(text = feature, tint = accent)
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(14.dp))

            SelectPlanButton(
                accent = accent,
                isRecommended = plan.isRecommended,
                onClick = onSelect,
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = text,
            color = AccentDark,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

/**
 * Per-plan call to action. The recommended plan ships with a solid teal fill — closer to
 * the mockup's "Select plan" CTA on Plan Pro — while the basic plan uses the neutral
 * outline treatment so the recommended tier visually wins.
 */
@Composable
private fun SelectPlanButton(
    accent: Color,
    isRecommended: Boolean,
    onClick: () -> Unit,
) {
    if (isRecommended) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            color = accent,
            shape = RoundedCornerShape(12.dp),
            onClick = onClick,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.payment_select_plan),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(width = 1.dp, color = AccentMint),
            onClick = onClick,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.payment_select_plan),
                    color = accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
