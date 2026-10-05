package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderHistoryViewModel : ViewModel() {

    private val repository: OrderRepository =
        FakeOrderRepository()

    private val _uiState =
        MutableStateFlow(OrderHistoryUiState())

    val uiState: StateFlow<OrderHistoryUiState> =
        _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        _uiState.value = OrderHistoryUiState(
            isLoading = false,
            orders = repository.getOrderHistory()
        )
    }
}