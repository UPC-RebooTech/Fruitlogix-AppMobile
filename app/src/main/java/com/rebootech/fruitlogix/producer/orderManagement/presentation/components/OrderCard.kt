package com.rebootech.fruitlogix.producer.orderManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.StockShortage
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OrderCard(
    order: Order,
    stockShortage: StockShortage?,
    onCardClick: () -> Unit,
    onAcceptOrder: () -> Unit,
    onRespondOrder: () -> Unit,
    onReviewOrder: () -> Unit,
    onPrepareOrder: () -> Unit,
    onLoadTruck: () -> Unit,
    onViewReport: () -> Unit,
    onReinspect: () -> Unit,
    onReviewResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardVariant = when (order.status) {
        OrderStatus.IN_TRANSIT -> CardVariant.White
        OrderStatus.DELIVERED -> CardVariant.Tint
        OrderStatus.REJECTED, OrderStatus.CANCELLED -> CardVariant.Dark
        else -> CardVariant.Dark
    }

    val isRejectedQuality = order.status == OrderStatus.IN_PREPARATION && order.qualityStatus == QualityStatus.REJECTED

    FruitLogixCard(
        variant = cardVariant,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Franja roja lateral si la calidad fue rechazada
            if (isRejectedQuality) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .background(FruitLogixTheme.colors.dangerStrong)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header: ID • Cliente + Chip de estado
                OrderHeaderRow(order = order)

                Spacer(modifier = Modifier.height(6.dp))

                // Items summary: "Kent Mango 500 kg · Blueberries 80 kg"
                val itemsSummary = order.items.joinToString(" · ") { "${it.productName} ${it.kg} kg" }
                Text(
                    text = itemsSummary,
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = FruitLogixTheme.colors.textMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Contenido según estado del pedido
                when (order.status) {
                    OrderStatus.PENDING -> {
                        PendingOrderContent(
                            order = order,
                            stockShortage = stockShortage,
                            onAcceptOrder = onAcceptOrder,
                            onRespondOrder = onRespondOrder,
                            onReviewOrder = onReviewOrder
                        )
                    }

                    OrderStatus.IN_PREPARATION -> {
                        InPreparationOrderContent(
                            order = order,
                            onPrepareOrder = onPrepareOrder,
                            onLoadTruck = onLoadTruck,
                            onViewReport = onViewReport,
                            onReinspect = onReinspect,
                            onReviewResult = onReviewResult
                        )
                    }

                    OrderStatus.IN_TRANSIT -> {
                        InTransitOrderContent(order = order)
                    }

                    OrderStatus.DELIVERED -> {
                        DeliveredOrderContent(
                            order = order,
                            onViewDetails = onReviewOrder
                        )
                    }

                    OrderStatus.REJECTED, OrderStatus.CANCELLED -> {
                        // Tarjeta atenuada sin acciones adicionales
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderHeaderRow(order: Order) {
    val isLightCard = order.status == OrderStatus.IN_TRANSIT
    val titleColor = when {
        order.status == OrderStatus.PENDING -> FruitLogixTheme.colors.primary
        isLightCard -> FruitLogixTheme.colors.textOnLight
        else -> FruitLogixTheme.colors.textOnDark
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = order.id,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = titleColor
            )
            Text(
                text = " • ",
                style = FruitLogixTheme.typography.titleMedium,
                color = FruitLogixTheme.colors.textMuted
            )
            Text(
                text = order.clientName,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = if (isLightCard) FruitLogixTheme.colors.textOnLight else FruitLogixTheme.colors.textOnDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Chip de estado general
        when (order.status) {
            OrderStatus.PENDING -> StatusBadge(
                text = stringResource(R.string.producer_orders_chip_pending),
                type = StatusBadgeType.WARNING
            )
            OrderStatus.IN_PREPARATION -> StatusBadge(
                text = stringResource(R.string.producer_orders_chip_in_preparation),
                type = StatusBadgeType.NEUTRAL
            )
            OrderStatus.IN_TRANSIT -> StatusBadge(
                text = stringResource(R.string.producer_orders_chip_in_transit),
                type = StatusBadgeType.HIGHLIGHT
            )
            OrderStatus.DELIVERED -> StatusBadge(
                text = stringResource(R.string.producer_orders_chip_delivered),
                type = StatusBadgeType.SUCCESS
            )
            OrderStatus.REJECTED -> StatusBadge(
                text = stringResource(R.string.producer_orders_chip_rejected),
                type = StatusBadgeType.DANGER
            )
            OrderStatus.CANCELLED -> StatusBadge(
                text = stringResource(R.string.producer_orders_chip_cancelled),
                type = StatusBadgeType.NEUTRAL
            )
        }
    }
}

@Composable
private fun PendingOrderContent(
    order: Order,
    stockShortage: StockShortage?,
    onAcceptOrder: () -> Unit,
    onRespondOrder: () -> Unit,
    onReviewOrder: () -> Unit
) {
    val dueStr = order.dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = R.drawable.ic_schedule,
                contentDescription = null,
                tint = FruitLogixTheme.colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.producer_orders_due_date, dueStr),
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textMuted
            )
        }

        val hoursLeft = order.respondByHoursLeft ?: 0
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = R.drawable.ic_logistics_clock,
                contentDescription = null,
                tint = if (hoursLeft > 0) FruitLogixTheme.colors.warning else FruitLogixTheme.colors.dangerStrong,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (hoursLeft > 0) {
                    stringResource(R.string.producer_orders_respond_within, hoursLeft)
                } else {
                    stringResource(R.string.producer_orders_overdue)
                },
                style = FruitLogixTheme.typography.bodySmall,
                color = if (hoursLeft > 0) FruitLogixTheme.colors.warning else FruitLogixTheme.colors.dangerStrong,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // Alerta de falta de stock si aplica
    if (stockShortage != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FruitLogixTheme.colors.warning.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(
                    id = R.drawable.ic_warning,
                    contentDescription = null,
                    tint = FruitLogixTheme.colors.warning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        R.string.producer_orders_short_by,
                        stockShortage.shortKg,
                        stockShortage.productName
                    ),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = FruitLogixTheme.colors.warning,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Botones de acción
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SecondaryButton(
            text = stringResource(R.string.producer_orders_btn_review),
            onClick = onReviewOrder,
            modifier = Modifier.weight(1f)
        )
        PrimaryButton(
            text = stringResource(R.string.producer_orders_btn_accept),
            onClick = onAcceptOrder,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InPreparationOrderContent(
    order: Order,
    onPrepareOrder: () -> Unit,
    onLoadTruck: () -> Unit,
    onViewReport: () -> Unit,
    onReinspect: () -> Unit,
    onReviewResult: () -> Unit
) {
    val dueStr = order.dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault()))

    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(
            id = R.drawable.ic_schedule,
            contentDescription = null,
            tint = FruitLogixTheme.colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.producer_orders_due_date, dueStr),
            style = FruitLogixTheme.typography.bodySmall,
            color = FruitLogixTheme.colors.textMuted
        )
    }

    Spacer(modifier = Modifier.height(10.dp))

    when (order.qualityStatus) {
        QualityStatus.APPROVED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    text = stringResource(R.string.producer_orders_quality_approved),
                    type = StatusBadgeType.SUCCESS
                )
                if (order.isReadyToLoad) {
                    StatusBadge(
                        text = stringResource(R.string.producer_orders_ready_to_load),
                        type = StatusBadgeType.HIGHLIGHT
                    )
                }
            }

            order.assignedTruck?.let { truck ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onLoadTruck)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(
                            id = R.drawable.ic_truck_pin,
                            contentDescription = null,
                            tint = FruitLogixTheme.colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.producer_orders_truck_assigned, truck.plate),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.textMuted
                        )
                    }
                    AppIcon(
                        id = R.drawable.ic_arrow_forward,
                        contentDescription = "Load Truck",
                        tint = FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        QualityStatus.PENDING -> {
            // PENDING = report submitted, in review. Show chip + "View review status" button.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = stringResource(R.string.producer_orders_quality_in_review),
                    type = StatusBadgeType.WARNING
                )

                SecondaryButton(
                    text = stringResource(R.string.producer_orders_btn_view_review_status),
                    onClick = onReviewResult
                )
            }
        }

        QualityStatus.REJECTED -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(FruitLogixTheme.colors.dangerStrong.copy(alpha = 0.12f))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    AppIcon(
                        id = R.drawable.ic_warning,
                        contentDescription = null,
                        tint = FruitLogixTheme.colors.dangerStrong,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.producer_orders_quality_rejected_badge),
                            style = FruitLogixTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = FruitLogixTheme.colors.dangerStrong
                        )
                        val reason = order.qualityRejectionReason ?: order.rejectionReason ?: ""
                        if (reason.isNotEmpty()) {
                            Text(
                                text = reason,
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.dangerStrong
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SecondaryButton(
                    text = stringResource(R.string.producer_orders_btn_view_report),
                    onClick = onViewReport,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onReinspect,
                    colors = ButtonDefaults.buttonColors(containerColor = FruitLogixTheme.colors.dangerStrong),
                    shape = FruitLogixTheme.shapes.Pill,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.producer_orders_btn_reinspect),
                        color = FruitLogixTheme.colors.textOnDark,
                        fontWeight = FontWeight.Bold,
                        style = FruitLogixTheme.typography.labelMedium
                    )
                }
            }
        }

        null -> {
            // null = no report submitted yet. Show neutral chip + Prepare & inspect.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = stringResource(R.string.producer_orders_quality_not_started),
                    type = StatusBadgeType.NEUTRAL
                )

                SecondaryButton(
                    text = stringResource(R.string.producer_orders_btn_prepare_inspect),
                    onClick = onPrepareOrder
                )
            }
        }
    }
}

@Composable
private fun InTransitOrderContent(order: Order) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dispatchedText = order.dispatchedAtFormatted ?: "today · 08:30 AM"
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = R.drawable.ic_schedule,
                contentDescription = null,
                tint = FruitLogixTheme.colors.textMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.producer_orders_dispatched_at, dispatchedText),
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textMuted
            )
        }

        order.assignedTruck?.etaFormatted?.let { eta ->
            Text(
                text = stringResource(R.string.producer_orders_eta, eta),
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textOnLight,
                fontWeight = FontWeight.Bold
            )
        }
    }

    order.assignedTruck?.let { truck ->
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(FruitLogixTheme.colors.surfaceLight)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                id = R.drawable.ic_truck_pin,
                contentDescription = null,
                tint = FruitLogixTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${truck.plate} · ${truck.driverName}",
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textOnLight,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DeliveredOrderContent(
    order: Order,
    onViewDetails: () -> Unit
) {
    val dateText = order.deliveredAtFormatted ?: order.dueDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = R.drawable.ic_task_check,
                contentDescription = null,
                tint = FruitLogixTheme.colors.success,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.producer_orders_delivered_at, dateText),
                style = FruitLogixTheme.typography.bodySmall,
                color = FruitLogixTheme.colors.textMuted
            )
        }

        Text(
            text = stringResource(R.string.producer_orders_btn_view_details),
            style = FruitLogixTheme.typography.bodySmall,
            color = FruitLogixTheme.colors.textOnLight,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .clickable(onClick = onViewDetails)
                .padding(4.dp)
        )
    }
}