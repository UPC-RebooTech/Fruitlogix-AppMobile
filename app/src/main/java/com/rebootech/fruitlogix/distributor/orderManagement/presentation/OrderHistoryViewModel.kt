package com.rebootech.fruitlogix.distributor.orderManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OrderHistoryUiState(
    val all: List<OrderHistoryItem> = emptyList(),
    val query: String = "",
    val status: OrderHistoryStatus? = null
) {
    /** One search box (order, client, producer or product) + status: fewer fields to fill in. */
    val visible: List<OrderHistoryItem>
        get() = all.filter { item ->
            val q = query.trim()
            val matchesQuery = q.isEmpty() || listOf(item.id, item.clientName, item.producerName, item.productName)
                .any { it.contains(q, ignoreCase = true) }
            matchesQuery && (status == null || item.status == status)
        }

    val hasFilters: Boolean get() = query.isNotBlank() || status != null
}

class OrderHistoryViewModel : ViewModel() {

    private val repository: OrderRepository = FakeOrderRepository.instance
    private val query = MutableStateFlow("")
    private val status = MutableStateFlow<OrderHistoryStatus?>(null)

    val uiState: StateFlow<OrderHistoryUiState> = combine(repository.history, query, status) { all, q, s ->
        OrderHistoryUiState(all = all, query = q, status = s)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OrderHistoryUiState(all = repository.history.value))

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onStatusChange(value: OrderHistoryStatus?) {
        status.value = value
    }

    fun clearFilters() {
        query.value = ""
        status.value = null
    }
}
