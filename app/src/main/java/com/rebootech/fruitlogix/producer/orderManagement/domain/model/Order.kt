package com.rebootech.fruitlogix.producer.orderManagement.domain.model

import com.rebootech.fruitlogix.shared.ui.components.SyncState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

enum class QualityStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/** How the order leaves the farm. */
enum class DeliveryMode { FARM_PICKUP, DELIVERY_TO_WAREHOUSE }

data class AssignedTruck(
    val plate: String,
    val vehicleType: String,
    val driverName: String,
    val driverPhone: String,
    val etaMinutes: Int? = null,
    val etaFormatted: String? = null
)

data class Order(
    val id: String,
    val clientName: String,
    val distributorName: String,
    val items: List<OrderItem>,
    val dueDate: LocalDate,
    val status: OrderStatus,
    val qualityStatus: QualityStatus? = null,
    val qualityRejectionReason: String? = null,
    val assignedTruck: AssignedTruck? = null,
    val respondBy: Instant? = null,
    val respondByHoursLeft: Int? = null,
    val rejectionReason: String? = null,
    val distributorNote: String? = null,
    val dispatchedAtFormatted: String? = null,
    val deliveredAtFormatted: String? = null,
    val syncState: SyncState = SyncState.SYNCED,
    val deliveryMode: DeliveryMode = DeliveryMode.FARM_PICKUP,
    val pickupFrom: LocalTime? = null,
    val pickupTo: LocalTime? = null,
    val coldChainMinC: Double? = null,
    val coldChainMaxC: Double? = null
) {
    val totalKg: Int get() = items.sumOf { it.kg }

    val isReadyToLoad: Boolean get() =
        status == OrderStatus.IN_PREPARATION &&
        qualityStatus == QualityStatus.APPROVED &&
        assignedTruck != null
}