package com.rebootech.fruitlogix.distributor.fleetManagement.domain.model

enum class VehicleStatus(val label: String) {
    AVAILABLE("AVAILABLE"),
    ON_ROUTE("ON ROUTE"),
    MAINTENANCE("MAINTENANCE")
}

data class Vehicle(
    val id: String,
    val plate: String,
    val unitCode: String,
    val type: String,
    val capacityTons: Double,
    val tempRange: String,
    val status: VehicleStatus,
    val currentRoute: String? = null
)

data class PendingDispatchOption(
    val dispatchId: String,
    val orderCode: String,
    val productDescription: String,
    val destination: String
)