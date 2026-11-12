package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrderHistoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.inner)
    ) {
        item {
            BackHeader(
                title = stringResource(R.string.dist_history_title),
                subtitle = stringResource(R.string.dist_history_subtitle),
                onBack = onBackClick
            )
        }

        if (state.all.isNotEmpty()) {
            item {
                LabeledTextField(
                    label = stringResource(R.string.dist_history_search),
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    leadingIconRes = R.drawable.ic_search,
                    imeAction = ImeAction.Search
                )
            }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    SelectableChip(
                        label = stringResource(R.string.dist_orders_filter_all),
                        selected = state.status == null,
                        onClick = { viewModel.onStatusChange(null) }
                    )
                    SelectableChip(
                        label = stringResource(R.string.dist_history_delivered),
                        selected = state.status == OrderHistoryStatus.DELIVERED,
                        onClick = { viewModel.onStatusChange(OrderHistoryStatus.DELIVERED) }
                    )
                    SelectableChip(
                        label = stringResource(R.string.dist_history_cancelled),
                        selected = state.status == OrderHistoryStatus.CANCELLED,
                        onClick = { viewModel.onStatusChange(OrderHistoryStatus.CANCELLED) }
                    )
                }
            }
        }

        val visible = state.visible
        when {
            state.all.isEmpty() -> item {
                EmptyState(
                    title = stringResource(R.string.dist_history_empty_title),
                    message = stringResource(R.string.dist_history_empty_message),
                    icon = { AppIcon(id = R.drawable.ic_schedule, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconBox)) }
                )
            }
            visible.isEmpty() -> item {
                EmptyState(
                    title = stringResource(R.string.dist_history_no_results),
                    message = "",
                    actionText = stringResource(R.string.dist_history_clear),
                    onAction = viewModel::clearFilters
                )
            }
            else -> items(visible, key = { it.id }) { HistoryCard(it) }
        }
    }
}

@Composable
private fun HistoryCard(item: OrderHistoryItem) {
    val scheme = MaterialTheme.colorScheme
    val delivered = item.status == OrderHistoryStatus.DELIVERED
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.id, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(
                text = stringResource(if (delivered) R.string.dist_history_delivered else R.string.dist_history_cancelled),
                type = if (delivered) StatusBadgeType.SUCCESS else StatusBadgeType.NEUTRAL,
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(item.clientName, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = stringResource(R.string.dist_history_product_producer, item.productName, item.producerName),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(
            text = stringResource(
                if (delivered) R.string.dist_history_delivered_at else R.string.dist_history_cancelled_at,
                formatRelativeDateTime(item.completedAt)
            ),
            style = FruitLogixTheme.typography.labelLarge,
            color = scheme.onSurface
        )
    }
}
