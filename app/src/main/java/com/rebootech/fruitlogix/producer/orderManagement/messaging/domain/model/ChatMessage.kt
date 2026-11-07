package com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model

enum class MessageSender {
    PRODUCER,
    DISTRIBUTOR
}

data class ChatMessage(
    val id: String,
    val orderId: String,
    val sender: MessageSender,
    val message: String,
    val timeFormatted: String
)