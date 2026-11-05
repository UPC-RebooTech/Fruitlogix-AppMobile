package com.rebootech.fruitlogix.producer.orderManagement.domain.model

import com.rebootech.fruitlogix.shared.ui.components.SyncState
import java.time.Instant
import java.time.LocalDate

enum class QualityStatus {
    PENDING,
    APPROVED,
    REJECTED
}

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
    val assignedTruck: AssignedTruck? = null,
    val respondBy: Instant? = null,
    val respondByHoursLeft: Int? = null,
    val rejectionReason: String? = null,
    val syncState: SyncState = SyncState.SYNCED
) {
    val totalKg: Int get() = items.sumOf { it.kg }

    val isReadyToLoad: Boolean get() =
        status == OrderStatus.IN_PREPARATION &&
        qualityStatus == QualityStatus.APPROVED &&
        assignedTruck != null
}

