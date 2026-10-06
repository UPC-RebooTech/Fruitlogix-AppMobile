package com.rebootech.fruitlogix.paymentManagement.presentation

import com.rebootech.fruitlogix.paymentManagement.domain.model.Invoice

data class InvoiceDetailUiState(
    val isLoading: Boolean = true,
    val invoice: Invoice? = null,
    val isNotFound: Boolean = false
)