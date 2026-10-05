package com.rebootech.fruitlogix.orderManagement.presentation

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryItem

data class OrderHistoryUiState(
    val isLoading: Boolean = true,
    val orders: List<OrderHistoryItem> = emptyList()
) {
    val isEmpty: Boolean
        get() = !isLoading && orders.isEmpty()
}