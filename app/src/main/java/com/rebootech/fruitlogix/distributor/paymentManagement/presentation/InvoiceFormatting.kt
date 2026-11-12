package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@StringRes
fun invoiceStatusLabel(status: InvoiceStatus): Int = when (status) {
    InvoiceStatus.PENDING -> R.string.inv_status_pending
    InvoiceStatus.OVERDUE -> R.string.inv_status_overdue
    InvoiceStatus.PAID -> R.string.inv_status_paid
}

fun invoiceStatusBadge(status: InvoiceStatus): StatusBadgeType = when (status) {
    InvoiceStatus.PENDING -> StatusBadgeType.WARNING
    InvoiceStatus.OVERDUE -> StatusBadgeType.DANGER
    InvoiceStatus.PAID -> StatusBadgeType.SUCCESS
}

/** "Nov 11, 2026" / "11 nov 2026". */
@Composable
fun formatInvoiceDate(date: LocalDate): String =
    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(appLocale()))
