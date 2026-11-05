package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderFilters
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private sealed interface ActiveFilterPill {
    val key: String
    val label: String
    val iconRes: Int
    val onRemove: () -> Unit
}

private data class SimpleActiveFilterPill(
    override val key: String,
    override val label: String,
    override val iconRes: Int,
    override val onRemove: () -> Unit
) : ActiveFilterPill

@Composable
fun ActiveFilterChipsRow(
    filters: OrderFilters,
    onRemoveStatus: (OrderStatus) -> Unit,
    onRemoveClient: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pills = mutableListOf<ActiveFilterPill>()

    // Client pills
    for (clientId in filters.clientIds) {
        pills.add(
            SimpleActiveFilterPill(
                key = "client_$clientId",
                label = stringResource(R.string.producer_orders_filter_client, clientId),
                iconRes = R.drawable.ic_producers,
                onRemove = { onRemoveClient(clientId) }
            )
        )
    }

    // Status pills
    for (status in filters.statuses) {
        val statusName = when (status) {
            OrderStatus.PENDING -> stringResource(R.string.producer_orders_chip_pending)
            OrderStatus.IN_PREPARATION -> stringResource(R.string.producer_orders_chip_in_preparation)
            OrderStatus.IN_TRANSIT -> stringResource(R.string.producer_orders_chip_in_transit)
            OrderStatus.DELIVERED -> stringResource(R.string.producer_orders_chip_delivered)
            OrderStatus.REJECTED -> stringResource(R.string.producer_orders_chip_rejected)
            OrderStatus.CANCELLED -> stringResource(R.string.producer_orders_chip_cancelled)
        }
        pills.add(
            SimpleActiveFilterPill(
                key = "status_${status.name}",
                label = stringResource(R.string.producer_orders_filter_status, statusName),
                iconRes = R.drawable.ic_limits,
                onRemove = { onRemoveStatus(status) }
            )
        )
    }

    // Query pill (if user typed something)
    if (filters.query.isNotBlank()) {
        pills.add(
            SimpleActiveFilterPill(
                key = "query_${filters.query}",
                label = stringResource(R.string.producer_orders_filter_query, filters.query),
                iconRes = R.drawable.ic_troubleshoot,
                onRemove = onClearQuery
            )
        )
    }

    if (pills.isEmpty()) return

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(
            items = pills,
            key = { it.key }
        ) { pill ->
            ActiveFilterPillItem(
                label = pill.label,
                iconRes = pill.iconRes,
                onRemove = pill.onRemove
            )
        }
    }
}

@Composable
private fun ActiveFilterPillItem(
    label: String,
    iconRes: Int,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(FruitLogixTheme.shapes.Pill)
            .background(FruitLogixTheme.colors.surfaceTint)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = FruitLogixTheme.colors.textOnLight,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = label,
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.textOnLight,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    id = R.drawable.ic_close,
                    contentDescription = "Remove filter",
                    tint = FruitLogixTheme.colors.textOnLight,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}