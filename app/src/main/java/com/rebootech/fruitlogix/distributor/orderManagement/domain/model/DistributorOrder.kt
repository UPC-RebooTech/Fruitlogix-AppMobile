package com.rebootech.fruitlogix.distributor.orderManagement.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * An order the distributor places for a client. Only data: dates are real dates
 * (not "Today, 14:30" texts), so they can be shown in any language.
 */
data class DistributorOrder(
    val id: String,
    /** Blank when the client is not in [OrderCatalog]; the screen then shows "Client #$clientId". */
    val clientName: String,
    val productName: String,
    /** Same unit as the server (quantityKg). */
    val quantityKg: Double,
    val pallets: Int,
    val deliveryDate: LocalDate,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    /** commercialClientId on the server. */
    val clientId: Int? = null,
    /** Order id on the server; null for the sample orders that only live on this phone. */
    val remoteId: Int? = null,
    /** Producer assigned on the server (assign-producer). */
    val producerId: Int? = null
) {
    /** Only pending orders can be changed: once in preparation the producer is already working on it. */
    val isEditable: Boolean get() = status == OrderStatus.PENDING

    /** On the server only the delivery date can change (PUT /orders keeps client, product and quantity). */
    val onlyDateEditable: Boolean get() = remoteId != null

    /** Orders can be deleted during their first 24 hours. */
    fun canBeDeleted(now: LocalDateTime = LocalDateTime.now()): Boolean =
        createdAt.plusHours(DELETE_WINDOW_HOURS).isAfter(now)

    companion object {
        const val DELETE_WINDOW_HOURS = 24L
    }
}

/** What the distributor fills in to create or change an order. Client and product come from [OrderCatalog]. */
data class OrderDraft(
    val clientId: Int,
    val productName: String,
    val quantityKg: Double,
    val deliveryDate: LocalDate,
    /** Producer (server id) to assign right after creating; the order then goes to "in preparation". */
    val producerId: Int? = null
)

/** A finished order (delivered or cancelled). */
data class OrderHistoryItem(
    val id: String,
    val clientName: String,
    val clientId: Int? = null,
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
