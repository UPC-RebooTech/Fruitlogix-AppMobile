package com.rebootech.fruitlogix.producer.orderManagement.presentation

import com.rebootech.fruitlogix.shared.core.model.UiText
import java.time.LocalTime
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderFilters
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.StockShortage

/** Orders state: data and resource-based text only, so the screen follows the selected language. */
data class ProducerOrdersUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: UiText? = null,
    val allOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val filters: OrderFilters = OrderFilters(),
    val awaitingActionCount: Int = 0,
    val inShedCount: Int = 0,
    val activeVolumeKg: Int = 0,
    val statusCounts: Map<OrderStatus?, Int> = emptyMap(),
    val stockShortages: Map<String, StockShortage> = emptyMap(),
    val pendingAcceptOrder: Order? = null,
    val showAcceptConfirmDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val lastAcceptedOrder: Order? = null,
    val lastAcceptedTime: LocalTime? = null,
    val distributorName: String = "Carlos Mendoza",
    val showFilterSheet: Boolean = false
)
