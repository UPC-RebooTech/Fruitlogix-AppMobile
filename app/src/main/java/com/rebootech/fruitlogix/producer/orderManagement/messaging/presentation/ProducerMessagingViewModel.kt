package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagingUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = false
)

class ProducerMessagingViewModel(
    private val orderRepository: OrderRepository = FakeOrderRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagingUiState())
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            orderRepository.observeOrders().collect { orders ->
                // Genera una conversación dinámica por cada pedido activo
                val items = orders.map { order ->
                    val cleanId = order.id.removePrefix("#")
                    ChatConversation(
                        orderId = cleanId,
                        title = "${order.clientName} (Pedido #$cleanId)",
                        lastMessage = "Coordinación logística de despacho para ${order.items.firstOrNull()?.productName ?: "pedido"}.",
                        timestamp = "Hoy"
                    )
                }
                _uiState.update { it.copy(conversations = items) }
            }
        }
    }
}