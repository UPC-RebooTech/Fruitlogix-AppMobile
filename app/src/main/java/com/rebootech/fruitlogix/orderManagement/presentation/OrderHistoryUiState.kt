package com.rebootech.fruitlogix.orderManagement.presentation

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryStatus

data class OrderHistoryUiState(
    val isLoading: Boolean = true,
    val allOrders: List<OrderHistoryItem> = emptyList(),
    val orders: List<OrderHistoryItem> = emptyList(),

    val searchQuery: String = "",
    val dateFilter: String = "",
    val producerFilter: String = "",
    val statusFilter: OrderHistoryStatus? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && allOrders.isEmpty()

    val hasNoResults: Boolean
        get() = !isLoading &&
                allOrders.isNotEmpty() &&
                orders.isEmpty()
}