package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote

data class InvoiceDetailUiState(
    val isLoading: Boolean = true,
    val invoice: Invoice? = null,
    val isNotFound: Boolean = false,
    /** Credit notes proposed or approved for this invoice (US67). */
    val creditNotes: List<ProposedCreditNote> = emptyList()
)
