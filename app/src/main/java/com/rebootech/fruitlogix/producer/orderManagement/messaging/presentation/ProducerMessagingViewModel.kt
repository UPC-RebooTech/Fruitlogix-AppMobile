package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.messaging.data.FakeChatRepository
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatConversation
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

data class MessagingUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val isLoading: Boolean = true
)

/** One conversation per order; unread first, then most recent. */
class ProducerMessagingViewModel(
    private val orderRepository: OrderRepository = FakeOrderRepository.instance,
    private val chatRepository: ChatRepository = FakeChatRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagingUiState())
    val uiState: StateFlow<MessagingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                orderRepository.observeOrders(),
                chatRepository.messagesByOrder,
                chatRepository.unreadByOrder
            ) { orders, messages, unread ->
                orders.map { order ->
                    val id = order.id.removePrefix("#")
                    ChatConversation(
                        orderId = id,
                        clientName = order.clientName,
                        productSummary = order.items.joinToString(", ") { it.productName },
                        lastMessage = messages[id]?.maxByOrNull { it.sentAt },
                        unreadCount = unread[id] ?: 0
                    )
                }.sortedWith(
                    compareByDescending<ChatConversation> { it.unreadCount > 0 }
                        .thenByDescending { it.lastMessage?.sentAt ?: LocalDateTime.MIN }
                )
            }.collect { items ->
                _uiState.update { it.copy(conversations = items, isLoading = false) }
            }
        }
    }

    fun onOpenConversation(orderId: String) {
        chatRepository.markConversationRead(orderId)
    }
}
