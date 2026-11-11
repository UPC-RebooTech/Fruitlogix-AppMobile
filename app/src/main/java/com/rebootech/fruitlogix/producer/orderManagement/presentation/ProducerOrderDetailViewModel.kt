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

    fun acceptOrder(onDone: () -> Unit) {
        val id = order.value?.id ?: return
        viewModelScope.launch {
            repository.acceptOrder(id)
            onDone()
        }
    }
}
