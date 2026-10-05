package com.rebootech.fruitlogix.orderManagement.data

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderRegistration
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary
import com.rebootech.fruitlogix.orderManagement.domain.model.RegisteredOrder
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository

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
                status = OrderStatus.ON_ROUTE
            ),
            OrderSummary(
                id = "FX-1040",
                clientName = "Mercados Lima Sur",
                productName = "Fresa",
                quantityLabel = "14 pallets • 11.2 t",
                deliveryDateLabel = "Mañana, 08:00",
                status = OrderStatus.IN_PREPARATION
            ),
            OrderSummary(
                id = "FX-1039",
                clientName = "Distribuidora Andina",
                productName = "Mango Kent",
                quantityLabel = "20 pallets • 15.0 t",
                deliveryDateLabel = "26 Oct, 11:15",
                status = OrderStatus.PENDING
            )
        )

        private val registeredOrders =
            mutableListOf<RegisteredOrder>()
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
}