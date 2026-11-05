package com.rebootech.fruitlogix.producer.orderManagement.data

import com.rebootech.fruitlogix.producer.orderManagement.domain.model.AssignedTruck
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderItem
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeOrderRepository : OrderRepository {
    private val _orders = MutableStateFlow(createInitialOrders())

    override fun observeOrders(): Flow<List<Order>> = _orders.asStateFlow()

    override suspend fun acceptOrder(orderId: String): Boolean {
        var found = false
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId && order.status == OrderStatus.PENDING) {
                    found = true
                    order.copy(status = OrderStatus.IN_PREPARATION)
                } else {
                    order
                }
            }
        }
        return found
    }

    companion object {
        val instance: FakeOrderRepository by lazy { FakeOrderRepository() }

        private fun createInitialOrders(): List<Order> {
            val today = LocalDate.now()
            return listOf(
                Order(
                    id = "#1052",
                    clientName = "Mercado Central S.A.",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "mango", productName = "Kent Mango", kg = 500),
                        OrderItem(productId = "blueberries", productName = "Blueberries", kg = 80)
                    ),
                    dueDate = today.plusDays(2),
                    status = OrderStatus.PENDING,
                    respondByHoursLeft = 2
                ),
                Order(
                    id = "#1048",
                    clientName = "Supermercados Wong",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "hass", productName = "Hass Avocado", kg = 300)
                    ),
                    dueDate = today.plusDays(3),
                    status = OrderStatus.IN_PREPARATION,
                    qualityStatus = QualityStatus.PENDING
                ),
                Order(
                    id = "#1044",
                    clientName = "Mercado Central S.A.",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "mango", productName = "Kent Mango", kg = 400)
                    ),
                    dueDate = today.plusDays(1),
                    status = OrderStatus.IN_PREPARATION,
                    qualityStatus = QualityStatus.REJECTED,
                    rejectionReason = "Bruising above 4% limit"
                ),
                Order(
                    id = "#1041",
                    clientName = "Supermercados Wong",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "blueberries", productName = "Blueberries", kg = 120)
                    ),
                    dueDate = today.plusDays(1),
                    status = OrderStatus.IN_PREPARATION,
                    qualityStatus = QualityStatus.APPROVED,
                    assignedTruck = AssignedTruck(
                        plate = "FL-408",
                        vehicleType = "Reefer Truck 3.5T",
                        driverName = "Juan Pérez",
                        driverPhone = "+51 987 654 321",
                        etaFormatted = "14:30"
                    )
                ),
                Order(
                    id = "#1036",
                    clientName = "Mercado Central S.A.",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "grapes", productName = "Red Globe Grapes", kg = 150)
                    ),
                    dueDate = today,
                    status = OrderStatus.IN_TRANSIT
                ),
                Order(
                    id = "#1030",
                    clientName = "Supermercados Wong",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "mango", productName = "Kent Mango", kg = 350)
                    ),
                    dueDate = today.minusDays(1),
                    status = OrderStatus.DELIVERED
                )
            )
        }
    }
}

