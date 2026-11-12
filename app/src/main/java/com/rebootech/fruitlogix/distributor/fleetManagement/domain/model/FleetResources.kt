package com.rebootech.fruitlogix.distributor.fleetManagement.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class DriverStatus { AVAILABLE, ON_ROUTE, OFF_DUTY }

/** A driver of the distributor. Same people as the Fleet trips (FL-102, FL-408, FL-219). */
data class Driver(
    val id: String,
    val name: String,
    val phone: String,
    /** Peruvian license category, e.g. "A-IIIc". */
    val licenseClass: String,
    val licenseExpiry: LocalDate,
    val coldChainCertified: Boolean,
    val status: DriverStatus,
    /** Unit and order while on route. */
    val assignment: Assignment? = null,
    /** End of the mandatory rest while off duty. */
    val restUntil: LocalDateTime? = null
)

data class Assignment(val unitCode: String, val plate: String, val orderId: String, val route: String)

enum class VehicleStatus { AVAILABLE, ON_ROUTE, MAINTENANCE }

enum class VehicleType { HEAVY_REEFER, REEFER_TRUCK, MEDIUM_REEFER_VAN, LIGHT_REEFER }

data class Vehicle(
    val id: String,
    val unitCode: String,
    val plate: String,
    val type: VehicleType,
    val capacityTons: Double,
    val minCelsius: Double,
    val maxCelsius: Double,
    val status: VehicleStatus,
    /** Driver while on route. */
    val driverName: String? = null
)

/** An order ready to leave that still has no truck (from Orders: "in preparation"). */
data class PendingDispatch(
    val orderId: String,
    val clientName: String,
    val productName: String,
    val quantityTons: Double
)
