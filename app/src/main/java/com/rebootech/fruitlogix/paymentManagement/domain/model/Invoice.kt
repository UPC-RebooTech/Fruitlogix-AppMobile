package com.rebootech.fruitlogix.paymentManagement.domain.model

data class Invoice(
    val id: String,
    val producerName: String,
    val amount: Double,
    val status: String
)