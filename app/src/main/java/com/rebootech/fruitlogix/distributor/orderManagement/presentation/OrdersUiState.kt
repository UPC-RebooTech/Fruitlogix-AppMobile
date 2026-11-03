package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderSummary

data class OrdersUiState(
    val isLoading: Boolean = true,
    val orders: List<OrderSummary> = emptyList(),
    val deleteCandidate: OrderSummary? = null,
    val deleteResult: DeleteOrderResult? = null
) {

    val isEmpty: Boolean
        get() = !isLoading && orders.isEmpty()
}