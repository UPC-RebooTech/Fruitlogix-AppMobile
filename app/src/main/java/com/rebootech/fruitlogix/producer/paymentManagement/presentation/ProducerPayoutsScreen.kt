package com.rebootech.fruitlogix.producer.paymentManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.Payout
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutStatus
import com.rebootech.fruitlogix.producer.paymentManagement.domain.model.PayoutType
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatPen
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Producer payouts.
 * Card rule: the two totals are dark KPI tiles; payouts are light list cards.
 * Title sits on the screen (no extra top bar), in dark text like the other screens.
 */
@Composable
fun ProducerPayoutsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProducerPayoutsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.inner)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.payouts_title),
                    style = FruitLogixTheme.typography.headlineMedium,
                    color = scheme.onBackground
                )
                Text(
                    text = stringResource(R.string.payouts_subtitle),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        if (uiState.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(spacing.lg), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = scheme.primary)
                }
            }
            return@LazyColumn
        }

        // Totals (dark KPIs)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                KpiCard(
                    caption = stringResource(R.string.payouts_kpi_pending),
                    value = formatPen(uiState.totalPendingAmount, decimals = 0),
                    icon = {
                        AppIcon(
                            id = R.drawable.ic_schedule,
                            contentDescription = null,
                            tint = scheme.inversePrimary,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    },
                    footer = if (uiState.overdueAmount > 0) {
                        {
                            Text(
                                text = stringResource(R.string.payouts_kpi_overdue, formatPen(uiState.overdueAmount, decimals = 0)),
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.dangerStrong
                            )
                        }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    caption = stringResource(R.string.payouts_kpi_paid),
                    value = formatPen(uiState.totalPaidThisMonth, decimals = 0),
                    icon = {
                        AppIcon(
                            id = R.drawable.ic_invoices,
                            contentDescription = null,
                            tint = scheme.inversePrimary,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(PayoutFilter.entries) { filter ->
                    val labelRes = when (filter) {
                        PayoutFilter.ALL -> R.string.payouts_filter_all
                        PayoutFilter.PENDING -> R.string.payouts_filter_pending
                        PayoutFilter.PAID -> R.string.payouts_filter_paid
                    }
                    SelectableChip(
                        label = stringResource(labelRes, uiState.count(filter)),
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.onSelectFilter(filter) }
                    )
                }
            }
        }

        if (uiState.payouts.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.payouts_empty),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = spacing.md)
                )
            }
        } else {
            items(uiState.payouts, key = { it.id }) { payout ->
                PayoutCard(payout)
            }
        }
    }
}

/** Payout (light list card): type icon, concept, distributor, dates, amount and status. */
@Composable
private fun PayoutCard(payout: Payout) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val spacing = FruitLogixTheme.spacing
    val locale = appLocale()
    val dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    fun LocalDate.fmt() = format(dateFormat)

    val concept = when (payout.type) {
        PayoutType.LOT_SETTLEMENT -> if (payout.lotCode != null) {
            stringResource(R.string.payouts_type_settlement_lot, payout.productName, payout.lotCode)
        } else {
            stringResource(R.string.payouts_type_settlement, payout.productName)
        }
        PayoutType.HARVEST_ADVANCE -> stringResource(R.string.payouts_type_advance, payout.productName)
    }
    val (statusRes, badgeType) = when (payout.status) {
        PayoutStatus.PENDING -> R.string.payouts_status_pending to StatusBadgeType.WARNING
        PayoutStatus.PAID -> R.string.payouts_status_paid to StatusBadgeType.SUCCESS
        PayoutStatus.OVERDUE -> R.string.payouts_status_overdue to StatusBadgeType.DANGER
    }
    val dateLine = buildString {
        append(stringResource(R.string.payouts_issued, payout.issuedOn.fmt()))
        payout.dueOn?.let { due ->
            append(" · ")
            append(
                stringResource(
                    if (payout.status == PayoutStatus.OVERDUE) R.string.payouts_was_due else R.string.payouts_due,
                    due.fmt()
                )
            )
        }
    }
    val (iconBg, iconTint) = when (payout.status) {
        PayoutStatus.PAID -> colors.successContainer to colors.onSuccessContainer
        PayoutStatus.PENDING -> colors.attentionContainer to colors.onAttentionContainer
        PayoutStatus.OVERDUE -> scheme.errorContainer to scheme.onErrorContainer
    }

    FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(spacing.sm),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconBox)
                    .clip(FruitLogixTheme.shapes.IconBox)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = if (payout.type == PayoutType.HARVEST_ADVANCE) R.drawable.ic_leaf else R.drawable.ic_invoices,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
            }
            Spacer(modifier = Modifier.width(spacing.inner))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = concept,
                    style = FruitLogixTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = payout.distributorName,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = dateLine,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = if (payout.status == PayoutStatus.OVERDUE) scheme.error else scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.padding(top = spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = stringResource(statusRes),
                        type = badgeType,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = formatPen(payout.amount),
                        style = FruitLogixTheme.extraTypography.dataMono,
                        color = scheme.onSurface
                    )
                }
                Text(
                    text = payout.id,
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
