package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.messaging.data.FakeChatRepository
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatMessage
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val orderId: String = "",
    val clientName: String = "",
    val productSummary: String = "",
    val currentInput: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isDistributorTyping: Boolean = false
)

/** Chat for one order. Messages come from [ChatRepository], so the inbox stays in sync. */
class ProducerChatViewModel(
    private val orderRepository: OrderRepository = FakeOrderRepository.instance,
    private val chatRepository: ChatRepository = FakeChatRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun initChat(orderId: String) {
        val id = orderId.removePrefix("#")
        if (id == _uiState.value.orderId && observeJob != null) return
        _uiState.update { it.copy(orderId = id) }
        chatRepository.markConversationRead(id)

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(orderRepository.observeOrders(), chatRepository.messagesByOrder) { orders, messages ->
                val order = orders.find { it.id.removePrefix("#") == id }
                Triple(
                    order?.clientName.orEmpty(),
                    order?.items?.joinToString(", ") { it.productName }.orEmpty(),
                    messages[id].orEmpty().sortedBy { it.sentAt }
                )
            }.collect { (client, products, messages) ->
                _uiState.update {
                    it.copy(clientName = client, productSummary = products, messages = messages)
                }
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    /** Sends the typed text, or [quickReply] when the producer taps a suggestion. */
    fun sendMessage(quickReply: String? = null) {
        val text = (quickReply ?: _uiState.value.currentInput).trim()
        if (text.isBlank()) return
        val orderId = _uiState.value.orderId

        chatRepository.send(orderId, MessageSender.PRODUCER, text)
        if (quickReply == null) _uiState.update { it.copy(currentInput = "") }

        // Simulated distributor reply until the messaging backend exists (sample user content)
        viewModelScope.launch {
            _uiState.update { it.copy(isDistributorTyping = true) }
            delay(1500)
            chatRepository.send(orderId, MessageSender.DISTRIBUTOR, "Perfecto, gracias. Coordinamos el recojo.")
            chatRepository.markConversationRead(orderId)
            _uiState.update { it.copy(isDistributorTyping = false) }
        }
    }
}
