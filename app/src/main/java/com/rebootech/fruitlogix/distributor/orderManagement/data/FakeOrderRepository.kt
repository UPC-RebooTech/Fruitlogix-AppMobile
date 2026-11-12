package com.rebootech.fruitlogix.distributor.orderManagement.data

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.validation.OrderValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalDateTime

/** In-memory orders shared by every distributor screen (use [instance]). */
class FakeOrderRepository private constructor() : OrderRepository {

    private var nextOrderNumber = 1043

    private val _orders = MutableStateFlow(initialOrders())
    override val orders: StateFlow<List<DistributorOrder>> = _orders.asStateFlow()

    private val _history = MutableStateFlow(initialHistory())
    override val history: StateFlow<List<OrderHistoryItem>> = _history.asStateFlow()

    override fun orderById(orderId: String): DistributorOrder? =
        _orders.value.firstOrNull { it.id.equals(orderId, ignoreCase = true) }

    override suspend fun createOrder(draft: OrderDraft): DistributorOrder {
        val order = DistributorOrder(
            id = "FX-${nextOrderNumber++}",
            clientName = draft.clientName.trim(),
            productName = draft.productName.trim(),
            quantityTons = draft.quantityTons,
            pallets = OrderValidator.estimatePallets(draft.quantityTons),
            deliveryDate = draft.deliveryDate,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        // Newest first, so the user sees it at the top of the list
        _orders.update { listOf(order) + it }
        return order
    }

    override suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean {
        val current = orderById(orderId) ?: return false
        if (!current.isEditable) return false
        _orders.update { list ->
            list.map {
                if (it.id == current.id) {
                    it.copy(
                        clientName = draft.clientName.trim(),
                        productName = draft.productName.trim(),
                        quantityTons = draft.quantityTons,
                        pallets = OrderValidator.estimatePallets(draft.quantityTons),
                        deliveryDate = draft.deliveryDate
                    )
                } else it
            }
        }
        return true
    }

    override suspend fun deleteOrder(orderId: String): DeleteOrderResult {
        val order = orderById(orderId) ?: return DeleteOrderResult.NOT_FOUND
        if (!order.canBeDeleted()) return DeleteOrderResult.WINDOW_EXPIRED
        _orders.update { list -> list.filterNot { it.id == order.id } }
        return DeleteOrderResult.SUCCESS
    }

    /** Lot labels look like "LOT-FX-1042-PALTA": the order id is inside. */
    override fun validateLotCode(code: String): LotScanResult {
        val clean = code.trim().uppercase()
        val orderId = LOT_REGEX.find(clean)?.groupValues?.get(1)
            ?: return LotScanResult.NotRecognized(code)
        val order = orderById(orderId) ?: return LotScanResult.NotRecognized(code)
        return if (order.status == OrderStatus.ON_ROUTE) {
            LotScanResult.Matched(code, order)
        } else {
            LotScanResult.NotOnRoute(code, order)
        }
    }

    override suspend fun confirmReception(orderId: String): Boolean {
        val order = orderById(orderId) ?: return false
        if (order.status != OrderStatus.ON_ROUTE) return false
        _orders.update { list -> list.filterNot { it.id == order.id } }
        _history.update {
            listOf(
                OrderHistoryItem(
                    id = order.id,
                    clientName = order.clientName,
                    producerName = PRODUCER_BY_ORDER[order.id] ?: "—",
                    productName = order.productName,
                    completedAt = LocalDateTime.now(),
                    status = OrderHistoryStatus.DELIVERED
                )
            ) + it
        }
        return true
    }

    companion object {
        val instance: FakeOrderRepository by lazy { FakeOrderRepository() }

        /** Codes for the scanner demo (the camera does not decode labels yet). */
        /** Lot of FL-102, the truck waiting at the hub gate. */
        const val DEMO_VALID_CODE = "LOT-FX-1041-UVA"
        const val DEMO_INVALID_CODE = "DAMAGED-CODE"

        private val LOT_REGEX = Regex("^LOT-(FX-\\d+)")

        private val PRODUCER_BY_ORDER = mapOf(
            "FX-1042" to "Fundo Santa Rosa",
            "FX-1041" to "Viñedos de Ica",
            "FX-1038" to "Campos del Norte",
            "FX-1040" to "Agro Valle Verde",
            "FX-1039" to "Campos del Norte"
        )

        private fun initialOrders(): List<DistributorOrder> {
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            return listOf(
                DistributorOrder(
                    id = "FX-1042",
                    clientName = "Supermercados Lima Norte",
                    productName = "Palta Hass",
                    quantityTons = 12.0,
                    pallets = 18,
                    deliveryDate = today,
                    status = OrderStatus.ON_ROUTE,
                    createdAt = now.minusHours(40)
                ),
                // FL-102 (Ica): at the hub gate now
                DistributorOrder(
                    id = "FX-1041",
                    clientName = "Supermercados Lima Centro",
                    productName = "Uva Red Globe",
                    quantityTons = 10.4,
                    pallets = 16,
                    deliveryDate = today,
                    status = OrderStatus.ON_ROUTE,
                    createdAt = now.minusHours(52)
                ),
                // FL-219 (Piura): delayed at a weigh station
                DistributorOrder(
                    id = "FX-1038",
                    clientName = "Plaza Vea Callao",
                    productName = "Mango Kent",
                    quantityTons = 15.0,
                    pallets = 20,
                    deliveryDate = today.plusDays(1),
                    status = OrderStatus.ON_ROUTE,
                    createdAt = now.minusHours(70)
                ),
                DistributorOrder(
                    id = "FX-1040",
                    clientName = "Mercados Lima Sur",
                    productName = "Fresa",
                    quantityTons = 11.2,
                    pallets = 14,
                    deliveryDate = today.plusDays(1),
                    status = OrderStatus.IN_PREPARATION,
                    createdAt = now.minusHours(30)
                ),
                DistributorOrder(
                    id = "FX-1039",
                    clientName = "Distribuidora Andina",
                    productName = "Mango Kent",
                    quantityTons = 15.0,
                    pallets = 20,
                    deliveryDate = today.plusDays(4),
                    status = OrderStatus.PENDING,
                    createdAt = now.minusHours(8)
                )
            )
        }

        private fun initialHistory(): List<OrderHistoryItem> {
            val now = LocalDateTime.now()
            return listOf(
                OrderHistoryItem(
                    id = "FX-1035",
                    clientName = "Supermercados Lima Centro",
                    producerName = "Finca Los Andes",
                    productName = "Uva Red Globe",
                    completedAt = now.minusDays(3).withHour(16).withMinute(20),
                    status = OrderHistoryStatus.DELIVERED
                ),
                OrderHistoryItem(
                    id = "FX-1032",
                    clientName = "Distribuidora Pacífico",
                    producerName = "Agro Valle Verde",
                    productName = "Arándano",
                    completedAt = now.minusDays(6).withHour(10).withMinute(45),
                    status = OrderHistoryStatus.DELIVERED
                ),
                OrderHistoryItem(
                    id = "FX-1028",
                    clientName = "Mercados del Sur",
                    producerName = "Campos del Norte",
                    productName = "Mango Kent",
                    completedAt = now.minusDays(10).withHour(9).withMinute(10),
                    status = OrderHistoryStatus.CANCELLED
                )
            )
        }
    }
}
