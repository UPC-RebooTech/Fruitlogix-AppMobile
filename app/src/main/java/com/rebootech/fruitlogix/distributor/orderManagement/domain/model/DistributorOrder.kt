package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * An order the distributor places for a client. Only data: dates are real dates
 * (not "Today, 14:30" texts), so they can be shown in any language.
 */
data class DistributorOrder(
    val id: String,
    val clientName: String,
    val productName: String,
    val quantityTons: Double,
    val pallets: Int,
    val deliveryDate: LocalDate,
    val status: OrderStatus,
    val createdAt: LocalDateTime
) {
    /** Only pending orders can be changed: once in preparation the producer is already working on it. */
    val isEditable: Boolean get() = status == OrderStatus.PENDING

    /** Orders can be deleted during their first 24 hours. */
    fun canBeDeleted(now: LocalDateTime = LocalDateTime.now()): Boolean =
        createdAt.plusHours(DELETE_WINDOW_HOURS).isAfter(now)

    companion object {
        const val DELETE_WINDOW_HOURS = 24L
    }
}

/** What the distributor fills in to create or change an order. */
data class OrderDraft(
    val clientName: String,
    val productName: String,
    val quantityTons: Double,
    val deliveryDate: LocalDate
)

/** A finished order (delivered or cancelled). */
data class OrderHistoryItem(
    val id: String,
    val clientName: String,
    val producerName: String,
    val productName: String,
    val completedAt: LocalDateTime,
    val status: OrderHistoryStatus
)

/** Result of reading a lot label at the warehouse gate. */
sealed interface LotScanResult {
    val code: String

    data class Matched(override val code: String, val order: DistributorOrder) : LotScanResult

    /** The label exists but its order is not on its way (e.g. still pending). */
    data class NotOnRoute(override val code: String, val order: DistributorOrder) : LotScanResult

    data class NotRecognized(override val code: String) : LotScanResult
}
