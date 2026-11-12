package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OrdersUiState(
    val orders: List<DistributorOrder> = emptyList(),
    /** null = all statuses. */
    val filter: OrderStatus? = null
) {
    val visibleOrders: List<DistributorOrder>
        get() = if (filter == null) orders else orders.filter { it.status == filter }

    fun count(status: OrderStatus): Int = orders.count { it.status == status }
}

class OrdersViewModel : ViewModel() {

    private val repository: OrderRepository = FakeOrderRepository.instance
    private val filter = MutableStateFlow<OrderStatus?>(null)

    // The list follows the repository: new, edited, deleted and received orders show up at once.
    val uiState: StateFlow<OrdersUiState> = combine(repository.orders, filter) { orders, filter ->
        OrdersUiState(orders = orders, filter = filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrdersUiState(orders = repository.orders.value))

    fun onFilterChange(status: OrderStatus?) {
        filter.value = status
    }
}
