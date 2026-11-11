package com.rebootech.fruitlogix.producer.infrastructureIot.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.infrastructureIot.data.FakeIotAlertRepository
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AlertKind
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AlertSeverity
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.AssetType
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.producer.infrastructureIot.domain.model.MonitoredAsset
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatTimeAgo
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private enum class AlertFilter { ALL, CRITICAL, COLD_CHAIN }

/**
 * Notification inbox: what happened in the field (sensor alerts, telemetry, traceability).
 * Notification *preferences* live in My account → Notifications (gear button here).
 * Card rule: alerts are light list cards; critical ones get an error-tinted icon and badge.
 */
@Composable
fun ProducerNotificationsScreen(
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {}
) {
    val repository = FakeIotAlertRepository.instance
    val alerts by repository.alerts.collectAsState()
    var filter by rememberSaveable { mutableStateOf(AlertFilter.ALL) }

    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    val visible = alerts.filter {
        when (filter) {
            AlertFilter.ALL -> true
            AlertFilter.CRITICAL -> it.severity == AlertSeverity.CRITICAL
            AlertFilter.COLD_CHAIN -> it.isColdChain
        }
    }
    val unread = alerts.count { !it.isRead }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.inner)
    ) {
        // Header: title, unread count, settings
        item {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.iot_inbox_title),
                        style = FruitLogixTheme.typography.headlineMedium,
                        color = scheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.iot_inbox_subtitle),
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
                val settingsDescription = stringResource(R.string.iot_inbox_cd_settings)
                Box(
                    modifier = Modifier
                        .size(FruitLogixTheme.sizing.minTouch)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onOpenSettings)
                        .semantics { contentDescription = settingsDescription },
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = R.drawable.ic_limits,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(FruitLogixTheme.sizing.icon)
                    )
                }
            }
        }

        if (unread > 0) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = pluralStringResource(R.plurals.iot_inbox_unread, unread, unread),
                        type = StatusBadgeType.HIGHLIGHT,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextActionButton(
                        text = stringResource(R.string.iot_inbox_mark_all_read),
                        onClick = repository::markAllRead
                    )
                }
            }
        }

        // Filters
        item {
            val counts = mapOf(
                AlertFilter.ALL to alerts.size,
                AlertFilter.CRITICAL to alerts.count { it.severity == AlertSeverity.CRITICAL },
                AlertFilter.COLD_CHAIN to alerts.count { it.isColdChain }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(AlertFilter.entries) { option ->
                    val labelRes = when (option) {
                        AlertFilter.ALL -> R.string.iot_filter_all
                        AlertFilter.CRITICAL -> R.string.iot_filter_critical
                        AlertFilter.COLD_CHAIN -> R.string.iot_filter_cold_chain
                    }
                    FilterChipItem(
                        label = stringResource(labelRes, counts[option] ?: 0),
                        selected = filter == option,
                        onClick = { filter = option }
                    )
                }
            }
        }

        if (visible.isEmpty()) {
            item {
                EmptyState(
                    title = stringResource(R.string.iot_empty_title),
                    message = stringResource(R.string.iot_empty_message)
                )
            }
        } else {
            items(visible, key = { it.id }) { alert ->
                AlertCard(
                    alert = alert,
                    onClick = { repository.markRead(alert.id) },
                    onDismiss = { repository.dismiss(alert.id) },
                    onOrderClick = { orderId ->
                        repository.markRead(alert.id)
                        onNavigate(ProducerOrderManagementRoutes.orderDetail(orderId))
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.Pill)
            .background(if (selected) scheme.secondary else scheme.surfaceContainerHigh)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = if (selected) scheme.onSecondary else scheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun assetLabel(asset: MonitoredAsset): String = stringResource(
    when (asset.type) {
        AssetType.COLD_ROOM -> R.string.iot_asset_cold_room
        AssetType.WAREHOUSE -> R.string.iot_asset_warehouse
        AssetType.TRUCK -> R.string.iot_asset_truck
        AssetType.GATEWAY -> R.string.iot_asset_gateway
    },
    asset.code
)

/** Localized title + description built from the alert data. */
@Composable
private fun alertTexts(alert: IotAlert): Pair<String, String> {
    val asset = assetLabel(alert.asset)
    return when (val kind = alert.kind) {
        is AlertKind.TemperatureHigh -> stringResource(R.string.iot_title_temperature, asset) to
            stringResource(R.string.iot_desc_temperature, formatDecimal(kind.valueC), formatDecimal(kind.limitC))
        is AlertKind.HumidityLow -> stringResource(R.string.iot_title_humidity, asset) to
            stringResource(R.string.iot_desc_humidity, formatDecimal(kind.valuePct), formatDecimal(kind.minimumPct))
        is AlertKind.Vibration -> stringResource(R.string.iot_title_vibration, asset) to
            stringResource(R.string.iot_desc_vibration, kind.dock)
        is AlertKind.TraceabilitySynced -> stringResource(R.string.iot_title_traceability) to
            stringResource(R.string.iot_desc_traceability, kind.kg, kind.productName)
    }
}

/** Alert (light list card). Unread alerts show a dot and a bolder border. */
@Composable
private fun AlertCard(
    alert: IotAlert,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val (title, description) = alertTexts(alert)

    val (severityRes, badgeType) = when (alert.severity) {
        AlertSeverity.CRITICAL -> R.string.iot_severity_critical to StatusBadgeType.DANGER
        AlertSeverity.WARNING -> R.string.iot_severity_warning to StatusBadgeType.WARNING
        AlertSeverity.INFO -> R.string.iot_severity_info to StatusBadgeType.INFO
    }
    val (iconBg, iconTint) = when (alert.severity) {
        AlertSeverity.CRITICAL -> scheme.errorContainer to scheme.error
        AlertSeverity.WARNING -> colors.attentionContainer to colors.attentionContent
        AlertSeverity.INFO -> colors.coldContainer to colors.coldContent
    }
    val iconRes = when (alert.kind) {
        is AlertKind.TemperatureHigh -> R.drawable.ic_thermostat
        is AlertKind.HumidityLow -> R.drawable.ic_humidity
        is AlertKind.Vibration -> R.drawable.ic_fleet_truck
        is AlertKind.TraceabilitySynced -> R.drawable.ic_verified
    }
    val unreadDescription = stringResource(R.string.iot_cd_unread)

    FruitLogixCard(
        variant = CardVariant.White,
        onClick = onClick,
        border = if (!alert.isRead) androidx.compose.foundation.BorderStroke(2.dp, scheme.primary) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = FruitLogixTheme.spacing.sm, top = FruitLogixTheme.spacing.inner, bottom = FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!alert.isRead) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(scheme.primary)
                            .semantics { contentDescription = unreadDescription }
                    )
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                }
                StatusBadge(
                    text = stringResource(severityRes),
                    type = badgeType,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = formatTimeAgo(alert.occurredAt),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                val dismissDescription = stringResource(R.string.iot_cd_dismiss)
                Box(
                    modifier = Modifier
                        .size(FruitLogixTheme.sizing.minTouch)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .semantics { contentDescription = dismissDescription },
                    contentAlignment = Alignment.Center
                ) {
                    AppIcon(
                        id = R.drawable.ic_close,
                        contentDescription = null,
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(end = FruitLogixTheme.spacing.sm),
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
                        id = iconRes,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = FruitLogixTheme.typography.titleMedium,
                        color = scheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = FruitLogixTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            // Sensor + location, and link to the related order
            Row(
                modifier = Modifier
                    .padding(end = FruitLogixTheme.spacing.sm)
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(scheme.surfaceContainerLow)
                    .padding(horizontal = FruitLogixTheme.spacing.inner, vertical = FruitLogixTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.iot_sensor, alert.sensorId),
                        style = FruitLogixTheme.extraTypography.dataMonoSmall,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = alert.location,
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                alert.relatedOrderId?.let { orderId ->
                    TextActionButton(
                        text = stringResource(R.string.iot_view_order, orderId),
                        onClick = { onOrderClick(orderId) },
                        trailingIcon = {
                            AppIcon(
                                id = R.drawable.ic_chevron_right,
                                contentDescription = null,
                                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                            )
                        }
                    )
                }
            }
        }
    }
}
