package com.elysium.softwork.payment.membership.presentation.views.selection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * Membership selection screen — first step of the subscription flow.
 *
 * Renders the catalogue exposed by [MembershipViewModel.availablePlans] as a vertically
 * scrolling list of cards. Plans flagged with `isRecommended = true` use the [PrimaryTeal]
 * architectural accent (label colour, price colour, feature-check colour, solid CTA fill)
 * to steer the worker toward the upsell. Non-recommended plans use the neutral
 * [PrimarySky] accent with an outlined CTA so the recommended tier dominates visually.
 *
 * The LazyColumn's bottom content padding stacks on the navigation-bar inset, so the
 * final card always clears the system gesture pill even on devices that hide the bar.
 *
 * @param onPlanSelected invoked when the worker taps the CTA on a plan card; carries the
 *   stable [MembershipPlan.key] forward to the methods screen.
 * @param viewModel provider for the catalogue and shared payment state. Resolved through
 *   the manual service locator via the factory exposed on the ViewModel companion.
 */
@Composable
fun MembershipSelectionScreen(
    onPlanSelected: (String) -> Unit,
    viewModel: MembershipViewModel = viewModel(factory = MembershipViewModel.Factory),
) {
    val plans: List<MembershipPlan> = viewModel.availablePlans

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentWhite),
    ) {
        SelectionHeader()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.navigationBars
                .add(WindowInsets(left = 20.dp, top = 16.dp, right = 20.dp, bottom = 16.dp))
                .asPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = plans, key = { plan -> plan.key }) { plan ->
                PlanCard(plan = plan, onSelect = onPlanSelected)
            }
        }
    }
}

/**
 * Static screen header rendering the localized title in the brand navy weight.
 * Standalone so its layout invariants are obvious at the call site.
 */
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

/**
 * Single plan card rendered inside the [MembershipSelectionScreen] LazyColumn.
 *
 * Visual contract:
 *  - Title and price colour resolve from [MembershipPlan.isRecommended]: teal for the
 *    recommended tier, navy with a sky-coloured price for the rest.
 *  - The feature list is rendered with check icons tinted to match the accent colour.
 *  - The CTA delegates to [SelectPlanButton], which renders a solid fill for the
 *    recommended tier and an outlined treatment for the rest.
 *
 * The per-card `onClick` is hoisted through [remember] keyed on the plan key and the
 * caller's [onSelect] reference so taps allocate no fresh lambda across recompositions.
 *
 * @param plan plan model rendered by this card.
 * @param onSelect invoked with the plan's stable key when the worker taps the CTA.
 */
@Composable
private fun PlanCard(plan: MembershipPlan, onSelect: (String) -> Unit) {
    val accent: Color = if (plan.isRecommended) PrimaryTeal else PrimarySky
    val titleColor: Color = if (plan.isRecommended) PrimaryTeal else PrimaryNavy

    val onClick: () -> Unit = remember(plan.key, onSelect) { { onSelect(plan.key) } }

    SoftWorkCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = plan.name,
                color = titleColor,
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
                onClick = onClick,
            )
        }
    }
}

/**
 * A single check-prefixed row within a plan card's feature list.
 *
 * @param text feature label (already localized by the catalogue source).
 * @param tint colour applied to the check icon — matches the card's accent so the row
 *   inherits the per-plan visual identity.
 */
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
 * Per-plan call to action.
 *
 * The recommended plan ships with a solid [accent] fill (closer to the mockup's primary
 * "Select plan" CTA on Plan Pro) while the basic plan ships with the neutral outlined
 * treatment so the recommended tier visually wins.
 *
 * The fill colour for the recommended branch is intentionally the per-plan accent rather
 * than a Material role: the screen ships exactly two tiers with hand-tuned colours, and
 * routing through the theme would force a second token solely for this surface.
 *
 * @param accent base colour of the variant. Drives both the fill (recommended) and the
 *   label tint (outlined).
 * @param isRecommended `true` to render the solid fill, `false` for the outlined variant.
 * @param onClick invoked when the worker taps the CTA.
 */
@Composable
private fun SelectPlanButton(
    accent: Color,
    isRecommended: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val surfaceColor: Color =
        if (isRecommended) accent else MaterialTheme.colorScheme.surface
    val labelColor: Color =
        if (isRecommended) MaterialTheme.colorScheme.onPrimary else accent
    val border: BorderStroke? =
        if (isRecommended) null else BorderStroke(width = 1.dp, color = AccentMint)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = surfaceColor,
        shape = shape,
        border = border,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.payment_select_plan),
                color = labelColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
