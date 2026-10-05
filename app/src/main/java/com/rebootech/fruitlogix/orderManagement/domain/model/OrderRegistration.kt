package com.rebootech.fruitlogix.orderManagement.domain.model

data class OrderRegistration(
    val productName: String,
    val quantity: Double,
    val requiredDate: String
)