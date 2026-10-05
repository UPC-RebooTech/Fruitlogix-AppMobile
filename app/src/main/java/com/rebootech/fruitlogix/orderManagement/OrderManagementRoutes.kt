package com.rebootech.fruitlogix.orderManagement

object OrderManagementRoutes {
    const val CreateOrder = "order_management_create"
    const val EditOrder = "order_management_edit/{orderId}"
    const val OrderHistory = "order_management_history"

    fun editOrder(orderId: String): String {
        return "order_management_edit/$orderId"
    }
}