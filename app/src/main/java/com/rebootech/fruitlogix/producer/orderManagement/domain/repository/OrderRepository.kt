package com.rebootech.fruitlogix.producer.orderManagement.domain.repository

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>
    suspend fun acceptOrder(orderId: String): Boolean

    /** Marks the order's quality report as sent and waiting for the distributor (PENDING). */
    suspend fun submitQualityReport(orderId: String): Boolean

    /** The truck left the farm with the order: it goes from "in preparation" to "in transit". */
    suspend fun dispatchOrder(orderId: String): Boolean
}

