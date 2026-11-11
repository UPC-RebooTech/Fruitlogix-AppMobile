package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.dashboard.domain.model.TaskPriority
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private data class TaskText(val title: String, val detail: String, val iconRes: Int)

@Composable
private fun ProducerTask.toText(): TaskText = when (this) {
    is ProducerTask.NewOrder -> TaskText(
        title = stringResource(R.string.producer_home_task_new_order),
        detail = "$orderId · $clientName · $itemsSummary",
        iconRes = R.drawable.ic_orders
    )
    is ProducerTask.QualityRejected -> TaskText(
        title = stringResource(R.string.producer_home_task_rejected),
        detail = "$orderId · ${reason ?: stringResource(R.string.producer_home_task_rejected_fallback)}",
        iconRes = R.drawable.ic_warning
    )
    is ProducerTask.InspectionPending -> TaskText(
        title = stringResource(R.string.producer_home_task_inspection),
        detail = "$orderId · $clientName · $itemsSummary",
        iconRes = R.drawable.ic_task_check
    )
    is ProducerTask.TruckAssigned -> TaskText(
        title = stringResource(R.string.producer_home_task_truck, truckPlate),
        detail = stringResource(R.string.producer_home_task_truck_detail, orderId, eta),
        iconRes = R.drawable.ic_dispatch_truck
    )
    is ProducerTask.CriticalStock -> TaskText(
        title = stringResource(R.string.producer_home_task_critical_stock),
        detail = stringResource(
            R.string.producer_home_task_critical_stock_detail,
            productName,
            availableKg,
            minThresholdKg
        ),
        iconRes = R.drawable.ic_inventory
    )
}

/**
 * Task row for light list cards. High priority shows an "Urgent" tag (text, not only color);
 * trucks ready to load show the "Ready to load" tag.
 */
@Composable
fun TaskRow(
    task: ProducerTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val text = task.toText()
    val isHigh = task.priority == TaskPriority.HIGH

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.iconBox)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(if (isHigh) scheme.errorContainer else scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = text.iconRes,
                contentDescription = null,
                tint = if (isHigh) scheme.error else scheme.onSecondaryContainer,
                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
            )
        }

        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text.title,
                style = FruitLogixTheme.typography.titleSmall,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text.detail,
                style = FruitLogixTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val tag: Pair<String, StatusBadgeType>? = when {
                isHigh -> stringResource(R.string.producer_home_task_urgent) to StatusBadgeType.DANGER
                task is ProducerTask.TruckAssigned ->
                    stringResource(R.string.producer_orders_ready_to_load) to StatusBadgeType.HIGHLIGHT
                else -> null
            }
            tag?.let { (label, type) ->
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                StatusBadge(text = label, type = type, maxLines = 1, softWrap = false)
            }
        }

        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))

        AppIcon(
            id = R.drawable.ic_chevron_right,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
        )
    }
}

