package com.rebootech.fruitlogix.distributor.paymentManagement.domain.model

data class Invoice(
    val id: String,
    val counterpartyName: String,
    val amount: Double,
    val dateLabel: String,
    val type: InvoiceType,
    val status: InvoiceStatus
)