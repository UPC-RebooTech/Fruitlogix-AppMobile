package com.rebootech.fruitlogix.orderManagement.domain.model

data class OrderSummary(
    val id: String,
    val clientName: String,
    val productName: String,
    val quantityLabel: String,
    val deliveryDateLabel: String,
    val status: OrderStatus,
    val createdHoursAgo: Int = 0
)