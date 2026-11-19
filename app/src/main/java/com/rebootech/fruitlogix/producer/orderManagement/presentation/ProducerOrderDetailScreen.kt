package com.rebootech.fruitlogix.producer.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.DeliveryMode
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.qualityBadgeConfig
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.DetailRow
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.HeaderAction
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Order detail (producer).
 * - Highlighted (dark) summary: status, total kg, delivery date, reply deadline.
 * - Light sections: products, dispatch conditions, quality and truck (when they apply).
 * - Main action at the bottom depends on the order status.
 */
@Composable
fun ProducerOrderDetailScreen(
    orderId: String,
    onNavigateBack: () -> Unit = {},
    onAccepted: () -> Unit = {},
    onOpenChat: (String) -> Unit = {},
    onInspect: (String) -> Unit = {},
    onLoadTruck: (String) -> Unit = {},
    viewModel: ProducerOrderDetailViewModel = viewModel()
) {
    val order by viewModel.order.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    var showAcceptDialog by rememberSaveable { mutableStateOf(false) }
    val cleanId = orderId.removePrefix("#")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            BackHeader(
                title = stringResource(R.string.order_detail_title, cleanId),
                subtitle = order?.clientName,
                onBack = onNavigateBack,
                actions = {
                    HeaderAction(
                        iconRes = R.drawable.ic_chat,
                        contentDescription = stringResource(R.string.order_detail_cd_chat),
                        onClick = { onOpenChat(cleanId) }
                    )
                }
            )

            val current = order
            if (current == null) {
                Text(
                    text = stringResource(R.string.order_detail_not_found),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            } else {
                SummaryCard(current)
                ProductsSection(current)
                DispatchSection(current)
                if (current.qualityStatus != null) QualitySection(current)
                current.assignedTruck?.let { truck ->
                    DetailSection(title = stringResource(R.string.order_detail_section_truck)) {
                        DetailRow(R.drawable.ic_truck_pin, stringResource(R.string.order_detail_truck_plate), truck.plate, valueStyle = FruitLogixTheme.extraTypography.dataMono)
                        DetailRow(R.drawable.ic_reefer, stringResource(R.string.order_detail_truck_vehicle), truck.vehicleType)
                        DetailRow(R.drawable.ic_person, stringResource(R.string.order_detail_truck_driver), truck.driverName)
                        truck.etaFormatted?.let {
                            DetailRow(R.drawable.ic_eta, stringResource(R.string.order_detail_truck_eta), it, valueStyle = FruitLogixTheme.extraTypography.dataMono)
                        }
                    }
                }
            }
        }

        order?.let { current ->
            BottomActions(
                order = current,
                onAccept = { showAcceptDialog = true },
                onInspect = { onInspect(cleanId) },
                onLoadTruck = { onLoadTruck(cleanId) },
                onOpenChat = { onOpenChat(cleanId) }
            )
        }
    }

    if (showAcceptDialog) {
        ConfirmDialog(
            title = stringResource(R.string.producer_orders_accept_dialog_title, cleanId),
            message = stringResource(R.string.producer_orders_accept_dialog_desc),
            confirmText = stringResource(R.string.producer_orders_accept_confirm),
            dismissText = stringResource(R.string.producer_orders_accept_cancel),
            onConfirm = {
                showAcceptDialog = false
                viewModel.acceptOrder(onDone = onAccepted)
            },
            onDismiss = { showAcceptDialog = false }
        )
    }
}

@Composable
private fun SummaryCard(order: Order) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val locale = appLocale()
    val (statusRes, badgeType) = statusBadge(order.status)

    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = stringResource(statusRes), type = badgeType, maxLines = 1, softWrap = false)
                Spacer(modifier = Modifier.weight(1f))
                if (order.status == OrderStatus.PENDING) {
                    val hours = order.respondByHoursLeft ?: 0
                    Text(
                        text = if (hours > 0) {
                            stringResource(R.string.producer_orders_respond_within, hours)
                        } else {
                            stringResource(R.string.producer_orders_overdue)
                        },
                        style = FruitLogixTheme.typography.labelMedium,
                        color = if (hours > 0) colors.warning else colors.dangerStrong
                    )
                }
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Text(
                text = stringResource(R.string.order_detail_total).uppercase(),
                style = FruitLogixTheme.typography.labelSmall,
                color = colors.onInverseSurfaceVariant
            )
            Text(
                text = stringResource(
                    R.string.producer_orders_tile_volume_format,
                    NumberFormat.getNumberInstance(locale).format(order.totalKg)
                ),
                style = FruitLogixTheme.extraTypography.kpiNumber,
                color = scheme.inverseOnSurface
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
            Text(
                text = "${pluralStringResource(R.plurals.order_detail_products_count, order.items.size, order.items.size)} · " +
                    stringResource(
                        R.string.order_detail_delivery_on,
                        order.dueDate.format(DateTimeFormatter.ofPattern("EEE d MMM", locale))
                    ),
                style = FruitLogixTheme.typography.bodyMedium,
                color = colors.onInverseSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProductsSection(order: Order) {
    val scheme = MaterialTheme.colorScheme
    val numberFormat = NumberFormat.getNumberInstance(appLocale())
    DetailSection(title = stringResource(R.string.order_detail_section_products)) {
        order.items.forEachIndexed { index, item ->
            if (index > 0) HorizontalDivider(color = scheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = FruitLogixTheme.typography.titleSmall,
                        color = scheme.onSurface
                    )
                    item.specification?.let {
                        Text(
                            text = it,
                            style = FruitLogixTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.producer_orders_tile_volume_format, numberFormat.format(item.kg)),
                    style = FruitLogixTheme.extraTypography.dataMono,
                    color = scheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DispatchSection(order: Order) {
    val scheme = MaterialTheme.colorScheme
    val locale = appLocale()
    val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
    fun LocalTime.fmt() = format(timeFormat)

    DetailSection(title = stringResource(R.string.order_detail_section_dispatch)) {
        DetailRow(
            iconRes = R.drawable.ic_dispatch_truck,
            label = stringResource(R.string.order_detail_mode),
            value = stringResource(
                if (order.deliveryMode == DeliveryMode.FARM_PICKUP) R.string.order_detail_mode_pickup else R.string.order_detail_mode_delivery
            )
        )
        DetailRow(
            iconRes = R.drawable.ic_schedule,
            label = stringResource(R.string.order_detail_date),
            value = order.dueDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
                .replaceFirstChar { it.titlecase(locale) }
        )
        if (order.pickupFrom != null && order.pickupTo != null) {
            DetailRow(
                iconRes = R.drawable.ic_logistics_clock,
                label = stringResource(R.string.order_detail_window),
                value = stringResource(R.string.order_detail_window_value, order.pickupFrom.fmt(), order.pickupTo.fmt()),
                valueStyle = FruitLogixTheme.extraTypography.dataMono
            )
        }
        if (order.coldChainMinC != null && order.coldChainMaxC != null) {
            DetailRow(
                iconRes = R.drawable.ic_logistics_snowflake,
                label = stringResource(R.string.order_detail_cold_chain),
                value = stringResource(
                    R.string.order_detail_cold_chain_value,
                    formatDecimal(order.coldChainMinC),
                    formatDecimal(order.coldChainMaxC)
                )
            )
        }
        DetailRow(
            iconRes = R.drawable.ic_person,
            label = stringResource(R.string.order_detail_distributor),
            value = order.distributorName
        )
        order.distributorNote?.let { note ->
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(scheme.secondaryContainer)
                    .padding(FruitLogixTheme.spacing.inner)
            ) {
                Text(
                    text = stringResource(R.string.order_detail_note),
                    style = FruitLogixTheme.typography.labelMedium,
                    color = scheme.onSecondaryContainer
                )
                Text(
                    text = note,
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun QualitySection(order: Order) {
    val scheme = MaterialTheme.colorScheme
    val config = qualityBadgeConfig(order.qualityStatus)
    DetailSection(title = stringResource(R.string.order_detail_section_quality)) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        StatusBadge(text = stringResource(config.labelRes), type = config.badgeType, maxLines = 1, softWrap = false)
        val reason = order.qualityRejectionReason ?: order.rejectionReason
        if (order.qualityStatus == QualityStatus.REJECTED && reason != null) {
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(scheme.errorContainer)
                    .padding(FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.Top
            ) {
                AppIcon(
                    id = R.drawable.ic_warning,
                    contentDescription = null,
                    tint = scheme.error,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(text = reason, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun BottomActions(
    order: Order,
    onAccept: () -> Unit,
    onInspect: () -> Unit,
    onLoadTruck: () -> Unit,
    onOpenChat: () -> Unit
) {
    val spacing = FruitLogixTheme.spacing
    val primary: Pair<Int, () -> Unit>? = when {
        order.status == OrderStatus.PENDING -> R.string.producer_orders_btn_accept to onAccept
        order.status == OrderStatus.IN_PREPARATION && order.qualityStatus == null ->
            R.string.producer_orders_btn_prepare_inspect to onInspect
        order.status == OrderStatus.IN_PREPARATION && order.qualityStatus == QualityStatus.REJECTED ->
            R.string.producer_orders_btn_reinspect to onInspect
        order.isReadyToLoad -> R.string.producer_orders_cd_load_truck to onLoadTruck
        else -> null
    }

    Column(modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)) {
        if (primary != null) {
            PrimaryButton(
                text = stringResource(primary.first),
                onClick = primary.second,
                modifier = Modifier.fillMaxWidth()
            )
            TextActionButton(
                text = stringResource(R.string.order_detail_message),
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            SecondaryButton(
                text = stringResource(R.string.order_detail_message),
                onClick = onOpenChat,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun statusBadge(status: OrderStatus): Pair<Int, StatusBadgeType> = when (status) {
    OrderStatus.PENDING -> R.string.producer_orders_chip_pending to StatusBadgeType.WARNING
    OrderStatus.IN_PREPARATION -> R.string.producer_orders_chip_in_preparation to StatusBadgeType.NEUTRAL
    OrderStatus.IN_TRANSIT -> R.string.producer_orders_chip_in_transit to StatusBadgeType.INFO
    OrderStatus.DELIVERED -> R.string.producer_orders_chip_delivered to StatusBadgeType.SUCCESS
    OrderStatus.REJECTED -> R.string.producer_orders_chip_rejected to StatusBadgeType.DANGER
    OrderStatus.CANCELLED -> R.string.producer_orders_chip_cancelled to StatusBadgeType.NEUTRAL
}
