package com.rebootech.fruitlogix.orderManagement.presentation

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<OrderSummary> = emptyList()
) {

    val isEmpty: Boolean
        get() = !isLoading && orders.isEmpty()
}