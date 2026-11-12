package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Active orders: create, filter by status and open one to edit it. */
@Composable
fun OrdersScreen(
    onNewOrderClick: () -> Unit,
    onOrderClick: (orderId: String) -> Unit,
    onHistoryClick: () -> Unit,
    onScanReceptionClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OrdersViewModel = viewModel()
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
            Column {
                Text(stringResource(R.string.dist_orders_title), style = FruitLogixTheme.typography.headlineMedium, color = scheme.onBackground)
                Text(
                    text = pluralStringResource(R.plurals.dist_orders_active_count, state.orders.size, state.orders.size),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                PrimaryButton(
                    text = stringResource(R.string.dist_orders_new),
                    onClick = onNewOrderClick,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { AppIcon(id = R.drawable.ic_orders, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    SecondaryButton(
                        text = stringResource(R.string.dist_orders_scan),
                        onClick = onScanReceptionClick,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { AppIcon(id = R.drawable.ic_radar, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                    )
                    SecondaryButton(
                        text = stringResource(R.string.dist_orders_history),
                        onClick = onHistoryClick,
                        modifier = Modifier.weight(1f),
                        leadingIcon = { AppIcon(id = R.drawable.ic_schedule, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                SelectableChip(
                    label = stringResource(R.string.dist_orders_filter_count, stringResource(R.string.dist_orders_filter_all), state.orders.size),
                    selected = state.filter == null,
                    onClick = { viewModel.onFilterChange(null) }
                )
                OrderStatus.entries.forEach { status ->
                    SelectableChip(
                        label = stringResource(R.string.dist_orders_filter_count, stringResource(orderStatusLabel(status)), state.count(status)),
                        selected = state.filter == status,
                        onClick = { viewModel.onFilterChange(status) }
                    )
                }
            }
        }

        val visible = state.visibleOrders
        if (visible.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.dist_orders_empty_title),
                    message = stringResource(
                        if (state.orders.isEmpty()) R.string.dist_orders_empty_message else R.string.dist_orders_empty_filter
                    ),
                    icon = { AppIcon(id = R.drawable.ic_orders, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconBox)) }
                )
            }
        } else {
            items(visible, key = { it.id }) { order ->
                OrderCard(order = order, onClick = { onOrderClick(order.id) })
            }
        }
    }
}

/** Light card (lists use surfaceContainer*): id, status, client, product, quantity and delivery. */
@Composable
private fun OrderCard(order: DistributorOrder, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(order.id, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(
                text = stringResource(orderStatusLabel(order.status)),
                type = orderStatusBadge(order.status),
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(order.clientName, style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            text = "${order.productName} · ${formatQuantity(order.quantityTons, order.pallets)}",
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_schedule, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
            Text(
                text = stringResource(R.string.dist_orders_delivery, formatDeliveryDate(order.deliveryDate)),
                style = FruitLogixTheme.typography.labelLarge,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            AppIcon(id = R.drawable.ic_chevron_right, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        }
    }
}
