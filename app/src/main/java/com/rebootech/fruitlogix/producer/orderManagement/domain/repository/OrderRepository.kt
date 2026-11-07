package com.rebootech.fruitlogix.producer.orderManagement.domain.repository

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun observeOrders(): Flow<List<Order>>
    suspend fun acceptOrder(orderId: String): Boolean
}

