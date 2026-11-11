package com.rebootech.fruitlogix.producer.orderManagement.data

import android.util.Log
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.AssignedTruck
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderItem
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class FakeOrderRepository : OrderRepository {
    private val _orders = MutableStateFlow(createInitialOrders())

    // NUEVO: Apenas se abre la app, intentamos buscar los pedidos reales en internet en segundo plano
    init {
        fetchOrdersFromBackend()
    }

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

    // NUEVO: La función que va a internet a pedir los datos a tu backend en Render usando Retrofit
    private fun fetchOrdersFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Aquí usa Retrofit para llamar al servidor
                val remoteOrders = RetrofitClient.api.getOrders()

                if (remoteOrders.isNotEmpty()) {
                    // Convertimos lo que viene de internet al diseño de tus pedidos locales
                    val mappedRemote = remoteOrders.map { dto ->
                        Order(
                            id = "#${dto.id ?: 1000}",
                            clientName = dto.clientName ?: "Cliente General",
                            distributorName = "Carlos Mendoza",
                            items = listOf(
                                OrderItem(
                                    productId = "prod",
                                    productName = dto.productName ?: "Fruta",
                                    kg = dto.quantityKg?.toInt() ?: 100
                                )
                            ),
                            dueDate = LocalDate.now().plusDays(2),
                            status = OrderStatus.PENDING,
                            respondByHoursLeft = 2
                        )
                    }

                    // Juntamos los de internet con tus pedidos de prueba sin que se repitan
                    _orders.update { localList ->
                        (mappedRemote + localList).distinctBy { it.id }
                    }

                    // Avisamos que estamos conectados en línea
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                }
            } catch (e: Exception) {
                // Si Render está "dormido" o no hay internet, la app no se rompe y usa tus datos locales
                Log.e("FruitLogix-API", "Usando modo local (Backend inactivo): ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
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
                    qualityStatus = null,
                    distributorNote = "Wong requested export grade, minimum 23% dry matter"
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
                    qualityRejectionReason = "Bruising above 4% limit · Re-grading requested",
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
                    status = OrderStatus.IN_TRANSIT,
                    dispatchedAtFormatted = "today · 08:30 AM",
                    assignedTruck = AssignedTruck(
                        plate = "FL-302",
                        vehicleType = "Reefer Truck 2.5T",
                        driverName = "Mario Rossi",
                        driverPhone = "+51 912 345 678",
                        etaMinutes = 135,
                        etaFormatted = "2 h 15 min"
                    )
                ),
                Order(
                    id = "#1030",
                    clientName = "Supermercados Wong",
                    distributorName = "Carlos Mendoza",
                    items = listOf(
                        OrderItem(productId = "mango", productName = "Kent Mango", kg = 350)
                    ),
                    dueDate = today.minusDays(1),
                    status = OrderStatus.DELIVERED,
                    deliveredAtFormatted = "Oct 30"
                )
            )
        }
    }
}