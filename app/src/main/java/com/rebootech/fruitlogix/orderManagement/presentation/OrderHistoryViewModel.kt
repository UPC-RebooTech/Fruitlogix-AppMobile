package com.rebootech.fruitlogix.orderManagement.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryStatus
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
        val history = repository.getOrderHistory()

        _uiState.value = OrderHistoryUiState(
            isLoading = false,
            allOrders = history,
            orders = history
        )
    }

    fun onSearchQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = value
        )

        applyFilters()
    }

    fun onDateFilterChange(value: String) {
        _uiState.value = _uiState.value.copy(
            dateFilter = value
        )

        applyFilters()
    }

    fun onProducerFilterChange(value: String) {
        _uiState.value = _uiState.value.copy(
            producerFilter = value
        )

        applyFilters()
    }

    fun onStatusFilterChange(
        status: OrderHistoryStatus?
    ) {
        _uiState.value = _uiState.value.copy(
            statusFilter = status
        )

        applyFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            dateFilter = "",
            producerFilter = "",
            statusFilter = null,
            orders = _uiState.value.allOrders
        )
    }

    private fun applyFilters() {
        val state = _uiState.value

        val filteredOrders = state.allOrders.filter { order ->

            val matchesSearch =
                state.searchQuery.isBlank() ||
                        order.id.contains(
                            state.searchQuery,
                            ignoreCase = true
                        ) ||
                        order.clientName.contains(
                            state.searchQuery,
                            ignoreCase = true
                        )

            val matchesDate =
                state.dateFilter.isBlank() ||
                        order.completedDateLabel.contains(
                            state.dateFilter,
                            ignoreCase = true
                        )

            val matchesProducer =
                state.producerFilter.isBlank() ||
                        order.producerName.contains(
                            state.producerFilter,
                            ignoreCase = true
                        )

            val matchesStatus =
                state.statusFilter == null ||
                        order.status == state.statusFilter

            matchesSearch &&
                    matchesDate &&
                    matchesProducer &&
                    matchesStatus
        }

        _uiState.value = state.copy(
            orders = filteredOrders
        )
    }
}