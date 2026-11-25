package com.rebootech.fruitlogix.producer.orderManagement.domain.repository

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>

    fun orderById(orderId: String): Order? = null

    /** Pending → in preparation (PATCH /orders/{id}/status accept). */
    suspend fun acceptOrder(orderId: String): Boolean

    /** The producer declines the order with a reason (PATCH /orders/{id}/status reject). */
    suspend fun rejectOrder(orderId: String, reason: String): Boolean = false

    /** Marks the order's quality report as sent and waiting for the distributor (PENDING). */
    suspend fun submitQualityReport(orderId: String): Boolean

    /** The truck left the farm with the order: it goes from "in preparation" to "in transit" (start-dispatch). */
    suspend fun dispatchOrder(orderId: String): Boolean

    /** Reads the producer's orders again (GET /orders only returns their own). */
    suspend fun refresh() {}
}
