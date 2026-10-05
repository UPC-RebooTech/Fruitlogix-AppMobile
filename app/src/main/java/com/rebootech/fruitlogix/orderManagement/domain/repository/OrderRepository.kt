package com.rebootech.fruitlogix.orderManagement.domain.repository

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderRegistration
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary
import com.rebootech.fruitlogix.orderManagement.domain.model.RegisteredOrder
import com.rebootech.fruitlogix.orderManagement.domain.model.DeleteOrderResult

interface OrderRepository {

    fun getOrders(): List<OrderSummary>

    fun getOrderById(
        orderId: String
    ): OrderSummary?

    fun createOrder(
        order: OrderRegistration
    ): RegisteredOrder

    fun updateOrder(
        orderId: String,
        productName: String,
        quantity: Double,
        requiredDate: String
    ): Boolean

    fun deleteOrder(
        orderId: String
    ): DeleteOrderResult

}