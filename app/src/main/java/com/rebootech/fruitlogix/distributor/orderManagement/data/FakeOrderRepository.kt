package com.rebootech.fruitlogix.distributor.orderManagement.data

import android.util.Log
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
import com.rebootech.fruitlogix.shared.core.network.CreateOrderRequestDto
import com.rebootech.fruitlogix.shared.core.network.OrderItemRequestDto
import com.rebootech.fruitlogix.shared.core.network.OrderResponseDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.UpdateOrderRequestDto
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
import java.util.concurrent.ConcurrentHashMap

class FakeOrderRepository private constructor() : OrderRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var nextOrderNumber = 1043

    // Mapa real entre el código visual ("FX-1043") y el ID primario (Int) de PostgreSQL en Render
    private val backendIds = ConcurrentHashMap<String, Int>()
    private val deliveryIdByOrderCode = ConcurrentHashMap<String, Int>()

    private val _orders = MutableStateFlow(initialOrders())
    override val orders: StateFlow<List<DistributorOrder>> = _orders.asStateFlow()

    private val _history = MutableStateFlow(initialHistory())
    override val history: StateFlow<List<OrderHistoryItem>> = _history.asStateFlow()

    init {
        scope.launch { refresh() }
    }

    override fun orderById(orderId: String): DistributorOrder? =
        _orders.value.firstOrNull { it.id.equals(orderId, ignoreCase = true) }

    override fun backendIdFor(orderId: String): Int? = backendIds[orderId.uppercase()]

    override suspend fun createOrder(draft: OrderDraft): DistributorOrder = withContext(Dispatchers.IO) {
        val requestDto = draft.toCreateRequestDto()
        val remoteResult = runCatching { RetrofitClient.api.createOrder(requestDto) }

        var assignedCode = "FX-${nextOrderNumber++}"
        remoteResult.onSuccess { response ->
            if (response.isSuccessful) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                val body = response.body()
                val realId = body?.id
                val serverCode = body?.orderNumber?.takeIf { it.isNotBlank() }
                    ?: realId?.let { "FX-$it" }
                if (serverCode != null) assignedCode = serverCode
                if (realId != null) backendIds[assignedCode.uppercase()] = realId
            } else {
                RetrofitClient.updateSyncStatus(
                    BackendSyncStatus.OFFLINE_FALLBACK,
                    "POST /orders rechazado (HTTP ${response.code()})"
                )
            }
        }.onFailure { err ->
            RetrofitClient.updateSyncStatus(
                BackendSyncStatus.OFFLINE_FALLBACK,
                "POST /orders sin conexión: ${err.message}"
            )
        }

        val createdOrder = DistributorOrder(
            id = assignedCode,
            clientName = draft.clientName.trim(),
            productName = draft.productName.trim(),
            quantityTons = draft.quantityTons,
            pallets = OrderValidator.estimatePallets(draft.quantityTons),
            deliveryDate = draft.deliveryDate,
            status = OrderStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
        _orders.update { listOf(createdOrder) + it.filterNot { existing -> existing.id == createdOrder.id } }
        createdOrder
    }

    override suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean = withContext(Dispatchers.IO) {
        val current = orderById(orderId) ?: return@withContext false
        if (!current.isEditable) return@withContext false

        val realId = backendIds[current.id.uppercase()]
        if (realId != null) {
            val responseResult = runCatching {
                RetrofitClient.api.updateOrder(realId, draft.toUpdateRequestDto(current.id))
            }
            val response = responseResult.getOrNull()
            if (response != null && !response.isSuccessful) {
                // El servidor rechazó explícitamente la edición: no fingir éxito
                RetrofitClient.updateSyncStatus(
                    BackendSyncStatus.ONLINE,
                    "PUT /orders/$realId rechazado (HTTP ${response.code()})"
                )
                return@withContext false
            }
        } else {
            Log.w("FruitLogix-API", "Orden ${current.id} es sólo local (sin ID en BD); actualizando en memoria.")
        }

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
        true
    }

    override suspend fun deleteOrder(orderId: String): DeleteOrderResult = withContext(Dispatchers.IO) {
        val order = orderById(orderId) ?: return@withContext DeleteOrderResult.NOT_FOUND
        if (!order.canBeDeleted()) return@withContext DeleteOrderResult.WINDOW_EXPIRED

        val realId = backendIds[order.id.uppercase()]
        if (realId != null) {
            val responseResult = runCatching { RetrofitClient.api.deleteOrder(realId) }
            val response = responseResult.getOrNull()
            if (response != null && !response.isSuccessful && response.code() != 404) {
                Log.e("FruitLogix-API", "DELETE /orders/$realId falló con HTTP ${response.code()}")
                return@withContext DeleteOrderResult.NOT_FOUND
            }
            backendIds.remove(order.id.uppercase())
        }

        _orders.update { list -> list.filterNot { it.id == order.id } }
        DeleteOrderResult.SUCCESS
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

    override suspend fun confirmReception(orderId: String): Boolean = withContext(Dispatchers.IO) {
        val order = orderById(orderId) ?: return@withContext false
        if (order.status != OrderStatus.ON_ROUTE) return@withContext false

        // Utilizar el endpoint de completado de entrega en Deliveries (no un PUT genérico a orders)
        val realOrderId = backendIds[order.id.uppercase()]
        val knownDeliveryId = deliveryIdByOrderCode[order.id.uppercase()] ?: realOrderId
        if (knownDeliveryId != null) {
            runCatching {
                RetrofitClient.api.completeDelivery(knownDeliveryId)
            }.onSuccess { response ->
                if (!response.isSuccessful) {
                    Log.w("FruitLogix-API", "POST /deliveries/$knownDeliveryId/complete devolvió HTTP ${response.code()}")
                }
            }.onFailure { err ->
                Log.w("FruitLogix-API", "No se pudo sincronizar recepción remota: ${err.message}")
            }
        }

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
        true
    }

    override suspend fun refresh() {
        val remote = withContext(Dispatchers.IO) { runCatching { RetrofitClient.api.getOrders() } }
        remote.onSuccess { response ->
            if (response.isSuccessful) {
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                val dtos = response.body().orEmpty()
                val mappedActive = mutableListOf<DistributorOrder>()
                val mappedHistory = mutableListOf<OrderHistoryItem>()

                dtos.forEachIndexed { index, dto ->
                    val statusUpper = dto.status?.uppercase().orEmpty()
                    if (statusUpper == "DELIVERED" || statusUpper == "COMPLETED" || statusUpper == "CANCELLED") {
                        dto.toHistoryItem(index)?.let { mappedHistory.add(it) }
                    } else {
                        dto.toDomainOrder(index)?.let { mappedActive.add(it) }
                    }
                }

                if (mappedActive.isNotEmpty()) {
                    _orders.update { local ->
                        (mappedActive + local).distinctBy { it.id.uppercase() }
                    }
                }
                if (mappedHistory.isNotEmpty()) {
                    _history.update { local ->
                        (mappedHistory + local).distinctBy { it.id.uppercase() }
                    }
                }
            } else {
                RetrofitClient.updateSyncStatus(
                    BackendSyncStatus.OFFLINE_FALLBACK,
                    "GET /orders devolvió HTTP ${response.code()}"
                )
            }
        }.onFailure { err ->
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, err.message)
        }
    }

    private fun OrderDraft.toCreateRequestDto(): CreateOrderRequestDto {
        val clientNameClean = clientName.trim()
        val prodNameClean = productName.trim()
        val kg = (quantityTons * 1000.0).coerceAtLeast(1.0)
        val unitPrice = UNIT_PRICE_BY_PRODUCT[prodNameClean] ?: 8.50
        val subtotal = kg * unitPrice

        return CreateOrderRequestDto(
            commercialClientId = resolveClientId(clientNameClean),
            deliveryDueDate = deliveryDate.atTime(14, 0).toString(),
            deliveryAddress = ADDRESS_BY_CLIENT[clientNameClean] ?: "Lima Central Hub - Av. Argentina 3090, Callao",
            totalAmount = subtotal,
            notes = "Cliente: $clientNameClean | Pallets estimados: ${OrderValidator.estimatePallets(quantityTons)}",
            items = listOf(
                OrderItemRequestDto(
                    productId = resolveProductId(prodNameClean),
                    productName = prodNameClean,
                    quantityKg = kg,
                    unitPrice = unitPrice,
                    subtotal = subtotal
                )
            )
        )
    }

    private fun OrderDraft.toUpdateRequestDto(orderCode: String): UpdateOrderRequestDto {
        val createDto = toCreateRequestDto()
        return UpdateOrderRequestDto(
            commercialClientId = createDto.commercialClientId,
            deliveryDueDate = createDto.deliveryDueDate,
            deliveryAddress = createDto.deliveryAddress,
            totalAmount = createDto.totalAmount,
            notes = "Orden $orderCode | ${createDto.notes.orEmpty()}",
            items = createDto.items
        )
    }

    private fun OrderResponseDto.toDomainOrder(index: Int): DistributorOrder? {
        val realId = id
        val code = orderNumber?.takeIf { it.isNotBlank() }
            ?: realId?.let { "FX-$it" }
            ?: "FX-${1100 + index}"

        if (realId != null) {
            backendIds[code.uppercase()] = realId
        }

        val firstItem = items?.firstOrNull()
        val prodName = firstItem?.productName?.takeIf { it.isNotBlank() } ?: "Palta Hass"
        val kg = firstItem?.quantityKg ?: 10000.0
        val tons = (kg / 1000.0).coerceAtLeast(0.5)

        val resolvedClient = clientName?.takeIf { it.isNotBlank() }
            ?: notes?.substringAfter("Cliente: ", "")?.substringBefore("|")?.trim()?.takeIf { it.isNotBlank() }
            ?: CLIENT_BY_ID[commercialClientId]
            ?: "Supermercados Lima Norte"

        val parsedDueDate = deliveryDueDate?.take(10)?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        } ?: LocalDate.now().plusDays(2)

        val parsedCreatedAt = createdAt?.take(19)?.let {
            runCatching { LocalDateTime.parse(it) }.getOrNull()
        } ?: LocalDateTime.now().minusHours(1)

        val mappedStatus = when (status?.uppercase()) {
            "ON_ROUTE", "IN_TRANSIT", "DISPATCHED" -> OrderStatus.ON_ROUTE
            "IN_PREPARATION", "PREPARING", "ASSIGNED" -> OrderStatus.IN_PREPARATION
            else -> OrderStatus.PENDING
        }

        return DistributorOrder(
            id = code,
            clientName = resolvedClient,
            productName = prodName,
            quantityTons = tons,
            pallets = OrderValidator.estimatePallets(tons),
            deliveryDate = parsedDueDate,
            status = mappedStatus,
            createdAt = parsedCreatedAt
        )
    }

    private fun OrderResponseDto.toHistoryItem(index: Int): OrderHistoryItem? {
        val domain = toDomainOrder(index) ?: return null
        val histStatus = if (status?.uppercase() == "CANCELLED") {
            OrderHistoryStatus.CANCELLED
        } else {
            OrderHistoryStatus.DELIVERED
        }
        return OrderHistoryItem(
            id = domain.id,
            clientName = domain.clientName,
            producerName = producerId?.let { "Productor #$it" } ?: (PRODUCER_BY_ORDER[domain.id] ?: "Fundo Santa Rosa"),
            productName = domain.productName,
            completedAt = domain.createdAt,
            status = histStatus
        )
    }

    companion object {
        val instance: FakeOrderRepository by lazy { FakeOrderRepository() }

        const val DEMO_VALID_CODE = "LOT-FX-1041-UVA"
        const val DEMO_INVALID_CODE = "DAMAGED-CODE"

        private val LOT_REGEX = Regex("^LOT-(FX-\\d+)")

        private val CLIENT_ID_BY_NAME = mapOf(
            "Supermercados Lima Norte" to 101,
            "Supermercados Lima Centro" to 102,
            "Mercados Lima Sur" to 103,
            "Plaza Vea Callao" to 104,
            "Distribuidora Andina" to 105,
            "Distribuidora Pacífico" to 106
        )

        private val CLIENT_BY_ID = CLIENT_ID_BY_NAME.entries.associate { (k, v) -> v to k }

        private val ADDRESS_BY_CLIENT = mapOf(
            "Supermercados Lima Norte" to "Av. Alfredo Mendiola 3698, Independencia, Lima",
            "Supermercados Lima Centro" to "Av. Alfonso Ugarte 1140, Breña, Lima",
            "Mercados Lima Sur" to "Panamericana Sur Km 18.5, San Juan de Miraflores, Lima",
            "Plaza Vea Callao" to "Av. Sáenz Peña 1250, Callao",
            "Distribuidora Andina" to "Mercado Mayorista Santa Anita, Pabellón A, Lima"
        )

        private val PRODUCT_ID_BY_NAME = mapOf(
            "Palta Hass" to 1,
            "Uva Red Globe" to 2,
            "Mango Kent" to 3,
            "Arándano" to 4,
            "Fresa" to 5
        )

        private val UNIT_PRICE_BY_PRODUCT = mapOf(
            "Palta Hass" to 9.40,
            "Uva Red Globe" to 8.60,
            "Mango Kent" to 7.50,
            "Arándano" to 18.50,
            "Fresa" to 6.80
        )

        fun resolveClientId(clientName: String): Int =
            CLIENT_ID_BY_NAME[clientName] ?: ((clientName.lowercase().hashCode() and 0x7FFFFFFF) % 900 + 100)

        fun resolveProductId(productName: String): Int =
            PRODUCT_ID_BY_NAME[productName] ?: ((productName.lowercase().hashCode() and 0x7FFFFFFF) % 50 + 1)

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