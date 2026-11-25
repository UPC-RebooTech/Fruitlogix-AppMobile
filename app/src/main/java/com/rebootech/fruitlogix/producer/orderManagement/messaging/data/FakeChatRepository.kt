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

/** Chats kept in memory until the messaging backend exists. There are no sample messages. */
class FakeChatRepository : ChatRepository {

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    override val messagesByOrder: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _unread = MutableStateFlow<Map<String, Int>>(emptyMap())
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
