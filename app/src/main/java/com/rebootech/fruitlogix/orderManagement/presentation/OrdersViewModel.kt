package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.rebootech.fruitlogix.orderManagement.domain.model.DeleteOrderResult

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
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            orders = repository.getOrders()
        )
    }

    fun requestDelete(orderId: String) {
        val order = repository.getOrderById(orderId)

        _uiState.value = _uiState.value.copy(
            deleteCandidate = order,
            deleteResult = null
        )
    }

    fun confirmDelete() {
        val order = _uiState.value.deleteCandidate ?: return

        val result = repository.deleteOrder(order.id)

        _uiState.value = _uiState.value.copy(
            orders = repository.getOrders(),
            deleteCandidate = null,
            deleteResult = result
        )
    }

    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(
            deleteCandidate = null,
            deleteResult = null
        )
    }

    fun clearDeleteResult() {
        _uiState.value = _uiState.value.copy(
            deleteResult = null
        )
    }

}