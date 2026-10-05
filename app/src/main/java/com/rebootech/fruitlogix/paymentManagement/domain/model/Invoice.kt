package com.rebootech.fruitlogix.paymentManagement.domain.model

data class Invoice(
    val id: String,
    val counterpartyName: String,
    val amount: Double,
    val dateLabel: String,
    val type: InvoiceType,
    val status: InvoiceStatus
)