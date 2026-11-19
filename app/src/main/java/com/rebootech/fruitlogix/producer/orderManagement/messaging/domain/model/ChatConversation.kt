package com.rebootech.fruitlogix.producer.orderManagement.messaging.domain.model

/**
 * One conversation per order with its distributor. Data only: the UI builds the title
 * ("Order #1052") and the empty preview in the selected language.
 */
data class ChatConversation(
    val orderId: String,
    val clientName: String,
    val productSummary: String,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0
)
