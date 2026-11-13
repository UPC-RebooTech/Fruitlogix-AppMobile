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
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.OrderDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class FakeOrderRepository private constructor() : OrderRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextOrderNumber = 1043

    // Guarda la relación entre el ID visual ("FX-1043") y el ID numérico de la BD en Render
    private val backendIdByCode = mutableMapOf<String, String>()

    private val _orders = MutableStateFlow(initialOrders())
    override val orders: StateFlow<List<DistributorOrder>> = _orders.asStateFlow()

    private val _history = MutableStateFlow(initialHistory())
    override val history: StateFlow<List<OrderHistoryItem>> = _history.asStateFlow()

    init {
        scope.launch { refresh() }
    }

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
        _orders.update { listOf(order) + it }

        // Guardar en el backend Render (POST /api/v1/orders)
        scope.launch {
            runCatching {
                RetrofitClient.api.createOrder(order.toDto())
            }.onSuccess { createdDto ->
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                createdDto.id?.let { backendIdByCode[order.id] = it.toString() }
            }
        }
        return order
    }

    override suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean {
        val current = orderById(orderId) ?: return false
        if (!current.isEditable) return false
        var updatedOrder: DistributorOrder? = null
        _orders.update { list ->
            list.map {
                if (it.id == current.id) {
                    it.copy(
                        clientName = draft.clientName.trim(),
                        productName = draft.productName.trim(),
                        quantityTons = draft.quantityTons,
                        pallets = OrderValidator.estimatePallets(draft.quantityTons),
                        deliveryDate = draft.deliveryDate
                    ).also { u -> updatedOrder = u }
                } else it
            }
        }
        updatedOrder?.let { u ->
            val remoteId = backendIdByCode[u.id] ?: u.id.removePrefix("FX-")
            scope.launch {
                runCatching { RetrofitClient.api.updateOrder(remoteId, u.toDto()) }
            }
        }
        return true
    }

    override suspend fun deleteOrder(orderId: String): DeleteOrderResult {
        val order = orderById(orderId) ?: return DeleteOrderResult.NOT_FOUND
        if (!order.canBeDeleted()) return DeleteOrderResult.WINDOW_EXPIRED
        _orders.update { list -> list.filterNot { it.id == order.id } }

        val remoteId = backendIdByCode[order.id] ?: order.id.removePrefix("FX-")
        scope.launch {
            runCatching { RetrofitClient.api.deleteOrder(remoteId) }
        }
        return DeleteOrderResult.SUCCESS
    }

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
                    producerName = PRODUCER_BY_ORDER[order.id] ?: "Fundo Santa Rosa",
                    productName = order.productName,
                    completedAt = LocalDateTime.now(),
                    status = OrderHistoryStatus.DELIVERED
                )
            ) + it
        }

        val remoteId = backendIdByCode[order.id] ?: order.id.removePrefix("FX-")
        scope.launch {
            runCatching {
                RetrofitClient.api.updateOrder(
                    remoteId,
                    order.toDto().copy(status = "DELIVERED")
                )
            }
        }
        return true
    }

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getOrders() } }
        remote.onSuccess { dtos ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
            val mapped = dtos.mapIndexedNotNull { index, dto -> dto.toDomain(index) }
            if (mapped.isNotEmpty()) {
                _orders.update { current ->
                    (mapped + current).distinctBy { it.id }
                }
            }
        }.onFailure {
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
        }
    }

    private fun DistributorOrder.toDto(): OrderDto = OrderDto(
        orderNumber = id,
        clientId = "1",
        clientName = clientName,
        producerId = "1",
        productName = productName,
        quantityKg = quantityTons * 1000.0,
        totalAmount = quantityTons * 1000.0 * 8.5,
        status = status.name,
        deliveryDate = deliveryDate.toString(),
        createdAt = createdAt.toString()
    )

    private fun OrderDto.toDomain(index: Int): DistributorOrder? {
        val code = orderNumber?.takeIf { it.isNotBlank() } ?: "FX-${id ?: (1100 + index)}"
        id?.let { backendIdByCode[code] = it.toString() }
        val tons = when {
            quantityKg != null && quantityKg > 100.0 -> quantityKg / 1000.0
            quantityKg != null && quantityKg > 0.0 -> quantityKg
            else -> 10.0
        }
        val parsedDate = deliveryDate?.take(10)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        } ?: LocalDate.now().plusDays(2)

        val mappedStatus = when (status?.uppercase()) {
            "ON_ROUTE", "IN_TRANSIT" -> OrderStatus.ON_ROUTE
            "IN_PREPARATION", "PREPARING" -> OrderStatus.IN_PREPARATION
            else -> OrderStatus.PENDING
        }

        return DistributorOrder(
            id = code,
            clientName = clientName?.takeIf { it.isNotBlank() } ?: "Supermercados Lima Norte",
            productName = productName?.takeIf { it.isNotBlank() } ?: "Palta Hass",
            quantityTons = tons,
            pallets = OrderValidator.estimatePallets(tons),
            deliveryDate = parsedDate,
            status = mappedStatus,
            createdAt = LocalDateTime.now().minusHours(2)
        )
    }

    companion object {
        val instance: FakeOrderRepository by lazy { FakeOrderRepository() }

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