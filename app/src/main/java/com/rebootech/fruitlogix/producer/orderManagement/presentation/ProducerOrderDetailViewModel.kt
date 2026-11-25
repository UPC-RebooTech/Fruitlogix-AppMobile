package com.rebootech.fruitlogix.producer.orderManagement.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Order detail. Reads "orderId" from the navigation arguments. */
class ProducerOrderDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository = FakeOrderRepository.instance
    private val orderId: String = savedStateHandle.get<String>("orderId").orEmpty().removePrefix("#")

    /** null while loading or when the order does not exist. */
    val order: StateFlow<Order?> = repository.observeOrders()
        .map { orders -> orders.find { it.id.removePrefix("#") == orderId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch { repository.refresh() }
    }

    private val _rejectFailed = kotlinx.coroutines.flow.MutableStateFlow(false)
    /** The server did not accept the rejection (connection or state). */
    val rejectFailed: StateFlow<Boolean> = _rejectFailed

    /** Declines the order with a reason; the distributor and the client see it as rejected. */
    fun rejectOrder(reason: String, onDone: () -> Unit) {
        val id = order.value?.id ?: return
        if (reason.isBlank()) return
        viewModelScope.launch {
            val ok = repository.rejectOrder(id, reason)
            _rejectFailed.value = !ok
            if (ok) onDone()
        }
    }

    fun acceptOrder(onDone: () -> Unit) {
        val id = order.value?.id ?: return
        viewModelScope.launch {
            repository.acceptOrder(id)
            onDone()
        }
    }
}
