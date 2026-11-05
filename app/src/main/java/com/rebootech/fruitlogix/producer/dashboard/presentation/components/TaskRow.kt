package com.rebootech.fruitlogix.producer.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.domain.model.ProducerTask
import com.rebootech.fruitlogix.producer.dashboard.domain.model.TaskPriority
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun TaskRow(
    task: ProducerTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRedBox = task.priority == TaskPriority.HIGH
    val boxBg = if (isRedBox) FruitLogixTheme.colors.danger.copy(alpha = 0.35f) else FruitLogixTheme.colors.appbar
    val iconTint = if (isRedBox) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary

    val dotColor = when (task.priority) {
        TaskPriority.HIGH -> FruitLogixTheme.colors.dangerStrong
        TaskPriority.MEDIUM -> FruitLogixTheme.colors.warning
        TaskPriority.LOW -> FruitLogixTheme.colors.primary
    }
    val priorityDescription = when (task.priority) {
        TaskPriority.HIGH -> "Prioridad alta"
        TaskPriority.MEDIUM -> "Prioridad media"
        TaskPriority.LOW -> "Prioridad baja"
    }

    val iconRes = when {
        task.id.startsWith("task_pending") -> R.drawable.ic_orders
        task.id.startsWith("task_rejected") -> R.drawable.ic_warning
        task.id.startsWith("task_inspection") -> R.drawable.ic_task_check
        task.id.startsWith("task_truck") -> R.drawable.ic_dispatch_truck
        else -> R.drawable.ic_inventory
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FruitLogixTheme.colors.appbar.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(boxBg),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = FruitLogixTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = FruitLogixTheme.colors.textOnDark
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .semantics { contentDescription = priorityDescription }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = task.detail,
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textMuted
            )

            if (task.badgeText != null && task.badgeType != null) {
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(
                    text = task.badgeText,
                    type = task.badgeType,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        AppIcon(
            id = R.drawable.ic_chevron_right,
            contentDescription = null,
            tint = FruitLogixTheme.colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

