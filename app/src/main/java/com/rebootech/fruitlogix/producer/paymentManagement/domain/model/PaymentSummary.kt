package com.rebootech.fruitlogix.producer.paymentManagement.domain.model

data class PaymentSummary(
    val pendingAmount: Double,
    val paidAmount: Double,
    val overdueAmount: Double = 0.0
)

