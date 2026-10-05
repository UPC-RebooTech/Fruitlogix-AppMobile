package com.rebootech.fruitlogix.paymentManagement.presentation

import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceStatus
import com.rebootech.fruitlogix.paymentManagement.domain.model.InvoiceType

data class InvoicesUiState(
    val isLoading: Boolean = true,
    val invoices: List<Invoice> = emptyList()
) {

    val receivables: List<Invoice>
        get() = invoices.filter {
            it.type == InvoiceType.RECEIVABLE
        }

    val payables: List<Invoice>
        get() = invoices.filter {
            it.type == InvoiceType.PAYABLE
        }

    val outstandingReceivables: Double
        get() = receivables
            .filter {
                it.status != InvoiceStatus.PAID
            }
            .sumOf {
                it.amount
            }

    val outstandingPayables: Double
        get() = payables
            .filter {
                it.status != InvoiceStatus.PAID
            }
            .sumOf {
                it.amount
            }

    val isEmpty: Boolean
        get() = !isLoading && invoices.isEmpty()
}