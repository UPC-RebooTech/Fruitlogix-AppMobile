package com.rebootech.fruitlogix.producer.logisticsMonitoring.domain

data class ProducerPickupDispatch(
    val id: String,
    val orderCode: String,
    val distributorName: String,
    val driverName: String,
    val vehiclePlate: String,
    val driverPhone: String,
    val etaMinutes: Int,
    val assignedFruit: String,
    val totalWeightKg: Double,
    val status: String
)