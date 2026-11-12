package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@StringRes
fun orderStatusLabel(status: OrderStatus): Int = when (status) {
    OrderStatus.PENDING -> R.string.dist_order_status_pending
    OrderStatus.IN_PREPARATION -> R.string.dist_order_status_in_preparation
    OrderStatus.ON_ROUTE -> R.string.dist_order_status_on_route
}

fun orderStatusBadge(status: OrderStatus): StatusBadgeType = when (status) {
    OrderStatus.PENDING -> StatusBadgeType.NEUTRAL
    OrderStatus.IN_PREPARATION -> StatusBadgeType.WARNING
    OrderStatus.ON_ROUTE -> StatusBadgeType.INFO
}

/** "today", "tomorrow" or "Thu 14 Nov", in the selected language. */
@Composable
fun formatDeliveryDate(date: LocalDate): String {
    val today = LocalDate.now()
    val locale = appLocale()
    return when (date) {
        today -> stringResource(R.string.dist_orders_today)
        today.plusDays(1) -> stringResource(R.string.dist_orders_tomorrow)
        else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)).replaceFirstChar { it.titlecase(locale) }
    }
}

/** Full date for the form field: "Thursday, 14 November 2026". */
@Composable
fun formatLongDate(date: LocalDate): String {
    val locale = appLocale()
    return date.format(DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.FULL).withLocale(locale))
        .replaceFirstChar { it.titlecase(locale) }
}

@Composable
fun formatQuantity(tons: Double, pallets: Int): String =
    stringResource(R.string.dist_orders_quantity, formatDecimal(tons), pallets)
