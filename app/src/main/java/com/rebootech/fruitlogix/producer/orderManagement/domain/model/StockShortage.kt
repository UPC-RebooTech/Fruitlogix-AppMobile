package com.rebootech.fruitlogix.producer.orderManagement.domain.model

data class StockShortage(
    val productName: String,
    val shortKg: Int
)