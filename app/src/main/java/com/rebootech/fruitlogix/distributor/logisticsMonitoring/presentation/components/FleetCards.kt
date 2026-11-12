package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertKind
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.rules.ArrivalRules
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.DispatchItem
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.ReeferRisk
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.celsius
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.dispatchBadge
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.rangeText
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LimeProgressBar
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatTime
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.LocalDate

/**
 * A truck on route. Light card (lists use surfaceContainer*); a trip at risk gets a red border
 * line on the left so it stands out without turning the whole list red.
 */
@Composable
fun DispatchCard(item: DispatchItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val d = item.dispatch
    val (badge, badgeType) = dispatchBadge(item)
    val atRisk = item.risk != ReeferRisk.OK

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .whiteCard(FruitLogixTheme.shapes.Card)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        // Red stripe as tall as the card when the reefer is at risk
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(if (atRisk) scheme.error else Color.Transparent)
        )
        Column(modifier = Modifier.weight(1f).padding(FruitLogixTheme.spacing.inner)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(id = R.drawable.ic_fleet_truck, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(d.unitId, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(d.plate, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                StatusBadge(text = badge, type = badgeType, maxLines = 1, softWrap = false)
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(
                text = stringResource(R.string.fleet_cargo, d.productName, d.pallets),
                style = FruitLogixTheme.typography.titleMedium,
                color = scheme.onSurface
            )
            Text(
                text = stringResource(R.string.fleet_route, d.originName, d.destinationName),
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            LimeProgressBar(progress = d.progress, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.fleet_km, d.travelledKm.toInt(), d.totalKm.toInt()),
                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                color = scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            // Arrival on one line (time on the left, delay on the right) ...
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(id = R.drawable.ic_schedule, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
                Text(
                    text = stringResource(R.string.fleet_eta, formatTime(LocalDate.now().atTime(d.eta))),
                    style = FruitLogixTheme.typography.labelLarge,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (d.isDelayed) {
                    Text(
                        text = stringResource(R.string.fleet_delay, d.delayMinutes),
                        style = FruitLogixTheme.typography.labelMedium,
                        color = FruitLogixTheme.colors.attentionContent
                    )
                }
            }
            // ... and the reefer temperature on its own line, so neither gets squeezed
            item.celsius?.let { t ->
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                ReeferChip(
                    text = item.threshold?.let { stringResource(R.string.fleet_reefer, celsius(t), rangeText(it)) } ?: celsius(t),
                    alert = atRisk
                )
            }
        }
    }
}

@Composable
private fun ReeferChip(text: String, alert: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Row(
        modifier = Modifier
            .clip(FruitLogixTheme.shapes.Tag)
            .background(if (alert) scheme.errorContainer else colors.coldContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            id = R.drawable.ic_thermostat,
            contentDescription = null,
            tint = if (alert) scheme.error else colors.coldContent,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = FruitLogixTheme.extraTypography.dataMonoSmall,
            color = if (alert) scheme.onErrorContainer else colors.onColdContainer
        )
    }
}

/** A truck near the hub. At the gate the main action is to start the reception. */
@Composable
fun ArrivalCard(
    item: DispatchItem,
    onStartReception: () -> Unit,
    onViewTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val d = item.dispatch
    val atGate = ArrivalRules.isAtGate(d)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(if (atGate) scheme.secondaryContainer else scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant, FruitLogixTheme.shapes.Card)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(d.unitId, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface)
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(d.plate, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant)
            Spacer(modifier = Modifier.weight(1f))
            StatusBadge(
                text = stringResource(if (atGate) R.string.fleet_arrival_at_gate else R.string.fleet_arrival_approaching),
                type = if (atGate) StatusBadgeType.SUCCESS else StatusBadgeType.INFO,
                maxLines = 1,
                softWrap = false
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(stringResource(R.string.fleet_cargo, d.productName, d.pallets), style = FruitLogixTheme.typography.titleMedium, color = scheme.onSurface)
        Text("${d.driver.name} · ${stringResource(R.string.fleet_arrival_dock, d.dock)}", style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.inner), verticalAlignment = Alignment.CenterVertically) {
            InfoPill(R.drawable.ic_truck_pin, stringResource(R.string.fleet_arrival_distance, formatDecimal(d.remainingKm)))
            item.celsius?.let { InfoPill(R.drawable.ic_thermostat, celsius(it)) }
            InfoPill(R.drawable.ic_fleet_lock, stringResource(R.string.fleet_arrival_seal, d.sealId))
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        if (atGate) {
            PrimaryButton(text = stringResource(R.string.fleet_arrival_start), onClick = onStartReception, modifier = Modifier.fillMaxWidth())
            TextActionButton(text = stringResource(R.string.fleet_arrival_view_trip), onClick = onViewTrip, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            SecondaryButton(text = stringResource(R.string.fleet_arrival_view_trip), onClick = onViewTrip, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun InfoPill(iconRes: Int, text: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(id = iconRes, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, style = FruitLogixTheme.extraTypography.dataMonoSmall, color = scheme.onSurfaceVariant, maxLines = 1)
    }
}

/** Title of an alert in the user's language, built from its kind. */
@Composable
fun alertTitle(alert: IotAlert): String {
    val subject = alert.unitId ?: alert.deviceId
    return when (alert.kind) {
        AlertKind.TEMP_RISK -> stringResource(R.string.fleet_alert_temp_risk, subject, alert.limit?.let { celsius(it) } ?: "—")
        AlertKind.TEMP_OUT_OF_RANGE -> stringResource(R.string.fleet_alert_temp_out, subject, alert.value?.let { celsius(it) } ?: "—")
        AlertKind.SENSOR_OFFLINE -> stringResource(R.string.fleet_alert_offline, alert.deviceId)
        AlertKind.LOW_BATTERY -> stringResource(R.string.fleet_alert_low_battery, alert.deviceId, alert.value?.toInt() ?: 0)
    }
}

@Composable
fun AlertCard(
    alert: IotAlert,
    onOpen: () -> Unit,
    onResolve: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val critical = alert.severity == AlertSeverity.CRITICAL
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(if (critical) scheme.errorContainer else scheme.surfaceContainerLowest)
            .border(1.dp, scheme.outlineVariant, FruitLogixTheme.shapes.Card)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            AppIcon(
                id = if (critical) R.drawable.ic_warning else R.drawable.ic_alert,
                contentDescription = null,
                tint = if (critical) scheme.error else FruitLogixTheme.colors.attentionContent,
                modifier = Modifier.size(FruitLogixTheme.sizing.icon)
            )
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Column(modifier = Modifier.weight(1f)) {
                Text(alertTitle(alert), style = FruitLogixTheme.typography.titleSmall, color = if (critical) scheme.onErrorContainer else scheme.onSurface)
                Text(
                    text = stringResource(R.string.fleet_alert_where, alert.deviceId, formatTimeAgo(alert.createdAt)),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = if (critical) scheme.onErrorContainer else scheme.onSurfaceVariant
                )
            }
            StatusBadge(
                text = stringResource(
                    when (alert.severity) {
                        AlertSeverity.CRITICAL -> R.string.fleet_alert_critical
                        AlertSeverity.WARNING -> R.string.fleet_alert_warning
                        AlertSeverity.INFO -> R.string.fleet_alert_info
                    }
                ),
                type = when (alert.severity) {
                    AlertSeverity.CRITICAL -> StatusBadgeType.DANGER
                    AlertSeverity.WARNING -> StatusBadgeType.WARNING
                    AlertSeverity.INFO -> StatusBadgeType.INFO
                },
                maxLines = 1,
                softWrap = false
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextActionButton(text = stringResource(R.string.fleet_alert_resolve), onClick = onResolve)
            TextActionButton(
                text = stringResource(if (alert.unitId != null) R.string.fleet_alert_open_trip else R.string.fleet_alert_open_sensor),
                onClick = onOpen
            )
        }
    }
}
