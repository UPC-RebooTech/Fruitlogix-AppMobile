package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrdersViewModel(
    private val repository: OrderRepository = FakeOrderRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())

    val uiState: StateFlow<OrdersUiState> =
        _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val orders = repository.getOrders()

        _uiState.value = OrdersUiState(
            isLoading = false,
            orders = orders
        )
    }
}