package com.rebootech.fruitlogix.orderManagement.data

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderRegistration
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary
import com.rebootech.fruitlogix.orderManagement.domain.model.RegisteredOrder
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderHistoryStatus

class FakeOrderRepository : OrderRepository {

    companion object {

        private var nextOrderNumber = 1043

        private val orders = mutableListOf(
            OrderSummary(
                id = "FX-1042",
                clientName = "Supermercados Lima Norte",
                productName = "Palta Hass",
                quantityLabel = "18 pallets • 12.0 t",
                deliveryDateLabel = "Hoy, 14:30",
                status = OrderStatus.ON_ROUTE,
                createdHoursAgo = 40
            ),
            OrderSummary(
                id = "FX-1040",
                clientName = "Mercados Lima Sur",
                productName = "Fresa",
                quantityLabel = "14 pallets • 11.2 t",
                deliveryDateLabel = "Mañana, 08:00",
                status = OrderStatus.IN_PREPARATION,
                createdHoursAgo = 30
            ),
            OrderSummary(
                id = "FX-1039",
                clientName = "Distribuidora Andina",
                productName = "Mango Kent",
                quantityLabel = "20 pallets • 15.0 t",
                deliveryDateLabel = "26 Oct, 11:15",
                status = OrderStatus.PENDING,
                createdHoursAgo = 8
            )
        )

        private val registeredOrders =
            mutableListOf<RegisteredOrder>()

        private val orderHistory = listOf(
            OrderHistoryItem(
                id = "FX-1035",
                clientName = "Supermercados Lima Centro",
                producerName = "Finca Los Andes",
                completedDateLabel = "21 Oct, 16:20",
                status = OrderHistoryStatus.DELIVERED
            ),
            OrderHistoryItem(
                id = "FX-1032",
                clientName = "Distribuidora Pacífico",
                producerName = "Agro Valle Verde",
                completedDateLabel = "18 Oct, 10:45",
                status = OrderHistoryStatus.DELIVERED
            ),
            OrderHistoryItem(
                id = "FX-1028",
                clientName = "Mercados del Sur",
                producerName = "Campos del Norte",
                completedDateLabel = "14 Oct, 09:10",
                status = OrderHistoryStatus.CANCELLED
            )
        )

    }

    override fun getOrders(): List<OrderSummary> {
        return orders.toList()
    }

    override fun getOrderById(
        orderId: String
    ): OrderSummary? {
        return orders.firstOrNull {
            it.id == orderId
        }
    }

    override fun createOrder(
        order: OrderRegistration
    ): RegisteredOrder {
        val registeredOrder = RegisteredOrder(
            id = "FX-${nextOrderNumber++}",
            registration = order
        )

        registeredOrders.add(registeredOrder)

        return registeredOrder
    }

    override fun updateOrder(
        orderId: String,
        productName: String,
        quantity: Double,
        requiredDate: String
    ): Boolean {
        val index = orders.indexOfFirst {
            it.id == orderId
        }

        if (index == -1) {
            return false
        }

        val currentOrder = orders[index]

        if (currentOrder.status != OrderStatus.PENDING) {
            return false
        }

        val palletsLabel =
            currentOrder.quantityLabel.substringBefore("•").trim()

        orders[index] = currentOrder.copy(
            productName = productName,
            quantityLabel = "$palletsLabel • $quantity t",
            deliveryDateLabel = requiredDate
        )

        return true
    }

    override fun deleteOrder(
        orderId: String
    ): DeleteOrderResult {
        val index = orders.indexOfFirst {
            it.id == orderId
        }

        if (index == -1) {
            return DeleteOrderResult.NOT_FOUND
        }

        val order = orders[index]

        if (order.createdHoursAgo > 24) {
            return DeleteOrderResult.WINDOW_EXPIRED
        }

        orders.removeAt(index)

        return DeleteOrderResult.SUCCESS
    }

    override fun getOrderHistory(): List<OrderHistoryItem> {
        return orderHistory
    }

}