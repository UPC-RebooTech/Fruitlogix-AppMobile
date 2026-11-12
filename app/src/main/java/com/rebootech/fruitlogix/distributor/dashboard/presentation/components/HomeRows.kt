package com.rebootech.fruitlogix.distributor.dashboard.presentation.components

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.dashboard.domain.DistributorTask
import com.rebootech.fruitlogix.distributor.dashboard.domain.HomeTruck
import com.rebootech.fruitlogix.shared.ui.format.formatTime
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import java.time.LocalDate
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Light row for a pending task (card rule: lists use surfaceContainer*). */
@Composable
fun DistributorTaskRow(
    task: DistributorTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val (iconRes, title, detail, urgent) = when (task) {
        is DistributorTask.MissingDispatchGuide -> TaskText(
            R.drawable.ic_receipt,
            stringResource(R.string.dist_home_task_guide_title),
            stringResource(R.string.dist_home_task_guide_detail, task.orderId, task.producerName),
            urgent = true
        )
        is DistributorTask.ReceiveAtDock -> TaskText(
            R.drawable.ic_logistics_arrive,
            stringResource(R.string.dist_home_task_intake_title, task.unitId, task.dock),
            stringResource(R.string.dist_home_task_intake_detail, task.pallets, task.productName),
            urgent = false
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
            .whiteCard(FruitLogixTheme.shapes.Button)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.iconBox)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(if (urgent) scheme.errorContainer else scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = if (urgent) scheme.error else scheme.onSecondaryContainer,
                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
            )
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(detail, style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (urgent) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                StatusBadge(text = stringResource(R.string.dist_home_task_urgent), type = StatusBadgeType.DANGER, maxLines = 1, softWrap = false)
            }
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        AppIcon(id = R.drawable.ic_chevron_right, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
    }
}

private data class TaskText(val iconRes: Int, val title: String, val detail: String, val urgent: Boolean)

/** One truck on the road: status, route, trip progress, ETA and reefer temperature. */
@Composable
fun FleetUnitRow(
    truck: HomeTruck,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val d = truck.dispatch
    val (statusText, statusType) = when {
        truck.atRisk -> stringResource(R.string.dist_home_fleet_watch) to StatusBadgeType.WARNING
        d.isDelayed -> stringResource(R.string.dist_home_fleet_delayed) to StatusBadgeType.DANGER
        else -> stringResource(R.string.dist_home_fleet_on_time) to StatusBadgeType.SUCCESS
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .whiteCard(FruitLogixTheme.shapes.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_fleet_truck, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(d.unitId, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(text = statusText, type = statusType, maxLines = 1, softWrap = false)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(
            text = "${d.productName} · ${d.originName} → ${d.destinationName}",
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        LimeProgressBar(progress = d.progress, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (d.isDelayed) {
                    stringResource(R.string.dist_home_fleet_delay, "+${d.delayMinutes} min")
                } else {
                    stringResource(R.string.dist_home_fleet_eta, formatTime(LocalDate.now().atTime(d.eta)))
                },
                style = FruitLogixTheme.typography.labelLarge,
                color = if (d.isDelayed) scheme.error else scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            truck.celsius?.let { t ->
                AppIcon(id = R.drawable.ic_thermostat, contentDescription = null, tint = FruitLogixTheme.colors.coldContent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.dist_home_fleet_reefer, "${formatDecimal(t)} °C"),
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
    }
}
