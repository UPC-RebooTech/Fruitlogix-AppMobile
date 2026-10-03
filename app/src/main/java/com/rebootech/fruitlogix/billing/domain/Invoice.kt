package com.rebootech.fruitlogix.billing.domain

data class Invoice(
    val id: String,
    val producerName: String,
    val amount: Double,
    val status: String
)
