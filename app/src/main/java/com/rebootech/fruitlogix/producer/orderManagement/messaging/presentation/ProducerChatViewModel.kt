package com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatMessage
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

data class ChatUiState(
    val orderId: String = "",
    val distributorName: String = "Mercado Central S.A.",
    val currentInput: String = "",
    val messages: List<ChatMessage> = emptyList()
)

class ProducerChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initChat(orderId: String, distributorName: String = "Mercado Central S.A.") {
        val cleanId = orderId.removePrefix("#")
        _uiState.update {
            it.copy(
                orderId = cleanId,
                distributorName = distributorName,
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        orderId = cleanId,
                        sender = MessageSender.DISTRIBUTOR,
                        message = "Hola, ¿podrían confirmar si el lote de Mango Kent estará listo a las 8:00 AM para el recojo?",
                        timeFormatted = "08:15 AM"
                    ),
                    ChatMessage(
                        id = "2",
                        orderId = cleanId,
                        sender = MessageSender.PRODUCER,
                        message = "Buen día. Sí, ya está cosechado y en cajas de 4kg en el galpón principal.",
                        timeFormatted = "08:20 AM"
                    )
                )
            )
        }
    }

    fun onInputChange(text: String) {
        _uiState.update { it.copy(currentInput = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.currentInput.trim()
        if (text.isBlank()) return

        val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            orderId = _uiState.value.orderId,
            sender = MessageSender.PRODUCER,
            message = text,
            timeFormatted = nowTime
        )

        _uiState.update {
            it.copy(
                currentInput = "",
                messages = it.messages + userMsg
            )
        }

        // Respuesta simulada del distribuidor tras 1.5s
        viewModelScope.launch {
            delay(1500)
            val replyMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                orderId = _uiState.value.orderId,
                sender = MessageSender.DISTRIBUTOR,
                message = "Excelente, el camión refrigerado llegará puntual.",
                timeFormatted = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
            )
            _uiState.update { it.copy(messages = it.messages + replyMsg) }
        }
    }
}