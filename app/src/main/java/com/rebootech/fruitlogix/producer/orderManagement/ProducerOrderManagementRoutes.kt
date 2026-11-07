package com.rebootech.fruitlogix.producer.orderManagement

object ProducerOrderManagementRoutes {
    const val OrderDetail = "order_management_detail/{orderId}"
    const val RespondOrder = "order_management_respond/{orderId}"
    const val OrderHistory = "order_management_history"
    const val Inbox = "order_management_inbox"                  // messaging
    const val Chat = "order_management_chat/{orderId}"          // messaging
    fun orderDetail(id: String) = "order_management_detail/${id.removePrefix("#")}"
    fun respondOrder(id: String) = "order_management_respond/${id.removePrefix("#")}"
    fun chat(orderId: String) = "order_management_chat/${orderId.removePrefix("#")}"
}

