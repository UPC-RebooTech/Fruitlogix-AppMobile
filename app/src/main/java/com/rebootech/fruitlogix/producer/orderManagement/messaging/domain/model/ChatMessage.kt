package com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model

import java.time.LocalDateTime

enum class MessageSender {
    PRODUCER,
    DISTRIBUTOR
}

/** Chat message. [message] is what a person wrote (user content, not UI text). */
data class ChatMessage(
    val id: String,
    val orderId: String,
    val sender: MessageSender,
    val message: String,
    val sentAt: LocalDateTime = LocalDateTime.now()
)
