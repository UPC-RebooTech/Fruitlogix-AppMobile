package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

data class OrderHistoryItem(
    val id: String,
    val clientName: String,
    val producerName: String,
    val completedDateLabel: String,
    val status: OrderHistoryStatus
)