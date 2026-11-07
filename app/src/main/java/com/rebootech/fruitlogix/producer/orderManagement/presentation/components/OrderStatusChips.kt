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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private data class StatusChipItem(
    val status: OrderStatus?,
    val labelRes: Int,
    val isPending: Boolean = false
)

private val chipItems = listOf(
    StatusChipItem(null, R.string.producer_orders_chip_all),
    StatusChipItem(OrderStatus.PENDING, R.string.producer_orders_chip_pending, isPending = true),
    StatusChipItem(OrderStatus.IN_PREPARATION, R.string.producer_orders_chip_in_preparation),
    StatusChipItem(OrderStatus.IN_TRANSIT, R.string.producer_orders_chip_in_transit),
    StatusChipItem(OrderStatus.DELIVERED, R.string.producer_orders_chip_delivered),
    StatusChipItem(OrderStatus.REJECTED, R.string.producer_orders_chip_rejected),
    StatusChipItem(OrderStatus.CANCELLED, R.string.producer_orders_chip_cancelled)
)

@Composable
fun OrderStatusChips(
    selectedStatuses: Set<OrderStatus>,
    statusCounts: Map<OrderStatus?, Int>,
    onStatusSelected: (OrderStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(chipItems) { item ->
            val isSelected = if (item.status == null) {
                selectedStatuses.isEmpty()
            } else {
                selectedStatuses.contains(item.status)
            }
            val count = statusCounts[item.status] ?: 0

            StatusChip(
                label = stringResource(item.labelRes),
                count = count,
                isSelected = isSelected,
                isPendingAlert = item.isPending && count > 0,
                onClick = { onStatusSelected(item.status) }
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    isPendingAlert: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) FruitLogixTheme.colors.surfaceDark else FruitLogixTheme.colors.surfaceLight
    val textColor = if (isSelected) FruitLogixTheme.colors.textOnDark else FruitLogixTheme.colors.textOnLight

    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(FruitLogixTheme.shapes.Pill)
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = FruitLogixTheme.typography.labelSmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Badge with count
            val badgeBg = when {
                isPendingAlert && !isSelected -> FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.16f)
                isSelected -> FruitLogixTheme.colors.primary
                else -> FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.12f)
            }
            val badgeTextColor = when {
                isPendingAlert && !isSelected -> FruitLogixTheme.colors.dangerStrong
                isSelected -> FruitLogixTheme.colors.onPrimary
                else -> FruitLogixTheme.colors.textMuted
            }

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}