package com.rebootech.fruitlogix.producer.orderManagement.messaging.data

import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatMessage
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.util.UUID

/** In-memory chats until the messaging backend exists. Message texts are sample user content. */
class FakeChatRepository : ChatRepository {

    private val now = LocalDateTime.now()

    private val _messages = MutableStateFlow(
        mapOf(
            "1052" to listOf(
                ChatMessage(
                    id = "1052-1",
                    orderId = "1052",
                    sender = MessageSender.DISTRIBUTOR,
                    message = "¿Pueden confirmar el pedido hoy? Necesitamos los 500 kg de Kent para el viernes.",
                    sentAt = now.minusMinutes(18)
                )
            ),
            "1048" to listOf(
                ChatMessage(
                    id = "1048-1",
                    orderId = "1048",
                    sender = MessageSender.DISTRIBUTOR,
                    message = "Hola, ¿el lote de palta estará listo a las 8:00 para el recojo?",
                    sentAt = now.minusHours(3)
                ),
                ChatMessage(
                    id = "1048-2",
                    orderId = "1048",
                    sender = MessageSender.PRODUCER,
                    message = "Sí, ya está en cajas de 4 kg en el galpón principal.",
                    sentAt = now.minusHours(2).minusMinutes(50)
                )
            ),
            "1044" to listOf(
                ChatMessage(
                    id = "1044-1",
                    orderId = "1044",
                    sender = MessageSender.DISTRIBUTOR,
                    message = "El reporte salió con 5,4 % de magulladuras. ¿Pueden reclasificar el lote?",
                    sentAt = now.minusDays(1).withHour(17).withMinute(5)
                )
            )
        )
    )
    override val messagesByOrder: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _unread = MutableStateFlow(mapOf("1052" to 1, "1044" to 2))
    override val unreadByOrder: StateFlow<Map<String, Int>> = _unread.asStateFlow()

    override fun markConversationRead(orderId: String) {
        _unread.update { it - orderId.removePrefix("#") }
    }

    override fun send(orderId: String, sender: MessageSender, text: String) {
        val id = orderId.removePrefix("#")
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            orderId = id,
            sender = sender,
            message = text,
            sentAt = LocalDateTime.now()
        )
        _messages.update { it + (id to (it[id].orEmpty() + message)) }
    }

    companion object {
        val instance: ChatRepository by lazy { FakeChatRepository() }
    }
}
