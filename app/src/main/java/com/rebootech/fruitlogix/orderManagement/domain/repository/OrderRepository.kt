package com.rebootech.fruitlogix.orderManagement.domain.repository

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderRegistration
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary
import com.rebootech.fruitlogix.orderManagement.domain.model.RegisteredOrder

interface OrderRepository {

    fun getOrders(): List<OrderSummary>

    fun createOrder(
        order: OrderRegistration
    ): RegisteredOrder
}