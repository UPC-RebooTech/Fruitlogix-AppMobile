package com.rebootech.fruitlogix.paymentManagement

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.AutoInvoiceRules
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.CreditNoteRules
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.rules.InvoiceRules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class BillingRulesTest {

    private val today = LocalDate.of(2026, 11, 11)

    private fun invoice(type: InvoiceType, amount: Double, dueOn: LocalDate, status: InvoiceStatus = InvoiceStatus.PENDING) =
        Invoice("INV-1", "Cliente", amount, dueOn.minusDays(30), dueOn, type, status)

    // US66 ----------------------------------------------------------------------

    @Test
    fun autoInvoice_isKilosTimesPrice() {
        assertEquals(AutoInvoiceRules.Result.Issued(4230.0), AutoInvoiceRules.calculate("Lima Norte", 450.0, 9.4))
        assertEquals(AutoInvoiceRules.Result.Issued(33.33), AutoInvoiceRules.calculate("Lima Norte", 3.333, 10.0))
    }

    @Test
    fun autoInvoice_withoutAgreedPrice_staysPending() {
        assertEquals(AutoInvoiceRules.Result.PendingPrice, AutoInvoiceRules.calculate("Lima Norte", 450.0, null))
        assertEquals(AutoInvoiceRules.Result.PendingPrice, AutoInvoiceRules.calculate("Lima Norte", 450.0, 0.0))
    }

    @Test
    fun autoInvoice_needsClientAndKilos() {
        assertEquals(AutoInvoiceRules.Result.MissingClient, AutoInvoiceRules.calculate(" ", 450.0, 9.4))
        assertEquals(AutoInvoiceRules.Result.InvalidKilos, AutoInvoiceRules.calculate("Lima Norte", 0.0, 9.4))
    }

    // US67 ----------------------------------------------------------------------

    @Test
    fun creditNote_mustBePositive_andNotAboveInvoice() {
        assertEquals(CreditNoteRules.Error.NOT_A_NUMBER, CreditNoteRules.validate(null, 1000.0))
        assertEquals(CreditNoteRules.Error.NOT_POSITIVE, CreditNoteRules.validate(0.0, 1000.0))
        assertEquals(CreditNoteRules.Error.ABOVE_INVOICE, CreditNoteRules.validate(1200.0, 1000.0))
        assertNull(CreditNoteRules.validate(405.0, 1000.0))
    }

    // Overdue and totals --------------------------------------------------------

    @Test
    fun unpaidInvoicePastDue_isOverdue() {
        val late = invoice(InvoiceType.RECEIVABLE, 100.0, today.minusDays(10))
        assertEquals(10, InvoiceRules.daysOverdue(late, today))
        assertEquals(InvoiceStatus.OVERDUE, InvoiceRules.effectiveStatus(late, today))
    }

    @Test
    fun paidInvoice_isNeverOverdue() {
        val paid = invoice(InvoiceType.RECEIVABLE, 100.0, today.minusDays(10), InvoiceStatus.PAID)
        assertEquals(0, InvoiceRules.daysOverdue(paid, today))
        assertEquals(InvoiceStatus.PAID, InvoiceRules.effectiveStatus(paid, today))
    }

    @Test
    fun totals_countOnlyOpenInvoices() {
        val totals = InvoiceRules.totals(
            listOf(
                invoice(InvoiceType.RECEIVABLE, 1000.0, today.plusDays(5)),
                invoice(InvoiceType.RECEIVABLE, 500.0, today.minusDays(2)),
                invoice(InvoiceType.RECEIVABLE, 900.0, today.minusDays(2), InvoiceStatus.PAID),
                invoice(InvoiceType.PAYABLE, 300.0, today.plusDays(1))
            ),
            today
        )
        assertEquals(1500.0, totals.toCollect, 0.0)
        assertEquals(300.0, totals.toPay, 0.0)
        assertEquals(1, totals.overdueToCollect)
        assertEquals(0, totals.overdueToPay)
    }
}
