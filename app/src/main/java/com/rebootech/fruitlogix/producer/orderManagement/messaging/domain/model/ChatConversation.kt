package com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model

data class ChatConversation(
    val orderId: String,
    val title: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0
)