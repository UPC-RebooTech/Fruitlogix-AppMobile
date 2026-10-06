package com.rebootech.fruitlogix.paymentManagement.data

import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceType
import com.rebootech.fruitlogix.paymentManagement.domain.repository.BillingRepository

class FakeBillingRepository : BillingRepository {

    override fun getInvoices(): List<Invoice> {
        return invoices
    }
    override fun getInvoiceById(invoiceId: String): Invoice? {
        return invoices.find { invoice ->
            invoice.id == invoiceId
        }
    }
    companion object {

        private val invoices = listOf(
            Invoice(
                id = "INV-1042",
                counterpartyName = "Supermercados Lima Norte",
                amount = 8450.00,
                dateLabel = "24 Oct 2026",
                type = InvoiceType.RECEIVABLE,
                status = InvoiceStatus.PENDING
            ),
            Invoice(
                id = "INV-1038",
                counterpartyName = "Mercados Lima Sur",
                amount = 6200.00,
                dateLabel = "20 Oct 2026",
                type = InvoiceType.RECEIVABLE,
                status = InvoiceStatus.PAID
            ),
            Invoice(
                id = "INV-1034",
                counterpartyName = "Distribuidora Central",
                amount = 3850.00,
                dateLabel = "15 Oct 2026",
                type = InvoiceType.RECEIVABLE,
                status = InvoiceStatus.OVERDUE
            ),
            Invoice(
                id = "INV-2031",
                counterpartyName = "Finca Los Andes",
                amount = 5200.00,
                dateLabel = "23 Oct 2026",
                type = InvoiceType.PAYABLE,
                status = InvoiceStatus.PENDING
            ),
            Invoice(
                id = "INV-2028",
                counterpartyName = "Agro Valle Verde",
                amount = 3200.00,
                dateLabel = "18 Oct 2026",
                type = InvoiceType.PAYABLE,
                status = InvoiceStatus.PAID
            ),
            Invoice(
                id = "INV-2024",
                counterpartyName = "Campos del Norte",
                amount = 4100.00,
                dateLabel = "12 Oct 2026",
                type = InvoiceType.PAYABLE,
                status = InvoiceStatus.OVERDUE
            )
        )
    }
}