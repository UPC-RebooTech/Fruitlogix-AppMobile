package com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.BillingTotals
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.round

/** Automatic invoice when a reception is confirmed: kilos × agreed price (US66). */
object AutoInvoiceRules {

    sealed interface Result {
        data class Issued(val amount: Double) : Result
        /** No agreed price: the invoice is not issued and the order stays "billing pending". */
        data object PendingPrice : Result
        data object InvalidKilos : Result
        data object MissingClient : Result
    }

    fun calculate(clientName: String, kilos: Double?, pricePerKg: Double?): Result = when {
        clientName.isBlank() -> Result.MissingClient
        kilos == null || kilos <= 0.0 -> Result.InvalidKilos
        pricePerKg == null || pricePerKg <= 0.0 -> Result.PendingPrice
        else -> Result.Issued(roundCents(kilos * pricePerKg))
    }

    /** Invoices are due 30 days after issue. */
    const val PAYMENT_TERM_DAYS = 30L

    private fun roundCents(value: Double) = round(value * 100) / 100
}

/** Credit note amount when the distributor adjusts it (US67). */
object CreditNoteRules {

    enum class Error { NOT_A_NUMBER, NOT_POSITIVE, ABOVE_INVOICE }

    fun validate(amount: Double?, invoiceAmount: Double?): Error? = when {
        amount == null -> Error.NOT_A_NUMBER
        amount <= 0.0 -> Error.NOT_POSITIVE
        invoiceAmount != null && amount > invoiceAmount -> Error.ABOVE_INVOICE
        else -> null
    }
}

object InvoiceRules {

    /** Days past the due date; 0 when it is paid or not due yet. */
    fun daysOverdue(invoice: Invoice, today: LocalDate = LocalDate.now()): Int =
        if (invoice.status == InvoiceStatus.PAID || !today.isAfter(invoice.dueOn)) 0
        else ChronoUnit.DAYS.between(invoice.dueOn, today).toInt()

    /** Status as of [today]: an unpaid invoice past its due date is overdue. */
    fun effectiveStatus(invoice: Invoice, today: LocalDate = LocalDate.now()): InvoiceStatus =
        if (invoice.status != InvoiceStatus.PAID && daysOverdue(invoice, today) > 0) InvoiceStatus.OVERDUE else invoice.status

    fun totals(invoices: List<Invoice>, today: LocalDate = LocalDate.now()): BillingTotals {
        val open = invoices.filter { it.status != InvoiceStatus.PAID }
        return BillingTotals(
            toCollect = open.filter { it.type == InvoiceType.RECEIVABLE }.sumOf { it.amount },
            toPay = open.filter { it.type == InvoiceType.PAYABLE }.sumOf { it.amount },
            overdueToCollect = open.count { it.type == InvoiceType.RECEIVABLE && effectiveStatus(it, today) == InvoiceStatus.OVERDUE },
            overdueToPay = open.count { it.type == InvoiceType.PAYABLE && effectiveStatus(it, today) == InvoiceStatus.OVERDUE }
        )
    }
}
