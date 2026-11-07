package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

data class OrderRegistration(
    val productName: String,
    val quantity: Double,
    val requiredDate: String
)