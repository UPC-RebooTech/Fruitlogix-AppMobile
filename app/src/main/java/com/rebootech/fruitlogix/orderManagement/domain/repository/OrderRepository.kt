package com.rebootech.fruitlogix.orderManagement.domain.repository

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary

interface OrderRepository {

    fun getOrders(): List<OrderSummary>
}