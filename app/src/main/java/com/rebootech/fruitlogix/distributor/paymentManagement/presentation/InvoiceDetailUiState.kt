package com.rebootech.fruitlogix.distributor.paymentManagement.presentation

import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.Invoice

data class InvoiceDetailUiState(
    val isLoading: Boolean = true,
    val invoice: Invoice? = null,
    val isNotFound: Boolean = false
)