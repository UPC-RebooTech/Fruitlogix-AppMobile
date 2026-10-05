package com.rebootech.fruitlogix.orderManagement.data

import com.rebootech.fruitlogix.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.orderManagement.domain.model.OrderSummary
import com.rebootech.fruitlogix.orderManagement.domain.repository.OrderRepository

class FakeOrderRepository : OrderRepository {

    override fun getOrders(): List<OrderSummary> {
        return listOf(
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
    }
}