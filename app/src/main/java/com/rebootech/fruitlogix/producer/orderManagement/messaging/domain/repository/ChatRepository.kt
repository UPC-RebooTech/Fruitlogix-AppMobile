package com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.repository

import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.ChatMessage
import com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model.MessageSender
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    /** Messages by order id (without "#"), oldest first. */
    val messagesByOrder: StateFlow<Map<String, List<ChatMessage>>>

    /** Unread distributor messages by order id. */
    val unreadByOrder: StateFlow<Map<String, Int>>

    fun markConversationRead(orderId: String)

    fun send(orderId: String, sender: MessageSender, text: String)
}
