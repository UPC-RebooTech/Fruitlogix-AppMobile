package com.rebootech.fruitlogix.distributor.paymentManagement.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * An invoice of the distributor: to collect from a client ([InvoiceType.RECEIVABLE])
 * or to pay to a producer ([InvoiceType.PAYABLE]). Amounts in soles (PEN).
 */
data class Invoice(
    val id: String,
    val counterpartyName: String,
    val amount: Double,
    val issuedOn: LocalDate,
    val dueOn: LocalDate,
    val type: InvoiceType,
    val status: InvoiceStatus,
    /** Order that originated it, when there is one. */
    val orderId: String? = null
)

/** Why a credit note is proposed (US67). The text is built in presentation. */
enum class CreditReason { THERMAL_EXCURSION, REJECTED_FRUIT, DAMAGED_IN_RECEPTION }

/** Credit note suggested for a receivable invoice (US67). */
data class ProposedCreditNote(
    val id: String,
    val invoiceId: String,
    val clientName: String,
    val reason: CreditReason,
    val productName: String,
    val affectedKg: Int,
    /** For thermal excursions: peak temperature and minutes outside the range. */
    val peakCelsius: Double? = null,
    val excursionMinutes: Int? = null,
    val suggestedAmount: Double,
    /** Set when the distributor approves it (possibly with an adjusted amount). */
    val approvedAmount: Double? = null
) {
    val isApproved: Boolean get() = approvedAmount != null
}

/** Price per kilo agreed with a client in a month (US68). */
data class PricePoint(val month: YearMonth, val pricePerKg: Double)

enum class SummaryPeriod { WEEK, MONTH }

/** What a client bought in a period (US69). */
data class PurchaseSummary(
    val clientName: String,
    val period: SummaryPeriod,
    val kilos: Int,
    val amount: Double,
    val orders: Int
)

/** Totals shown at the top of the screen. */
data class BillingTotals(
    val toCollect: Double,
    val toPay: Double,
    val overdueToCollect: Int,
    val overdueToPay: Int
)
