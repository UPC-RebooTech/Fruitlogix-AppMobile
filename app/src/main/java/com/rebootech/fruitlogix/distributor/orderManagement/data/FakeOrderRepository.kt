package com.rebootech.fruitlogix.distributor.orderManagement.data

import android.util.Log
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderCatalog
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderSaveException
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderCatalogRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.distributor.orderManagement.domain.validation.OrderValidator
import com.rebootech.fruitlogix.shared.core.network.AssignProducerRequestDto
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.CancelOrderRequestDto
import com.rebootech.fruitlogix.shared.core.network.ConfirmReceptionRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
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
import kotlin.coroutines.cancellation.CancellationException

/**
 * Distributor orders.
 * - Server orders (/api/v1/orders) are shown as "FX-<id>". Creating, changing the delivery date,
 *   deleting and assigning a producer are sent to the server.
 * - Sample orders (FX-1038…FX-1042) only live on this phone, so Fleet, Home and Invoices keep
 *   their sample trips with or without connection.
 */
class FakeOrderRepository private constructor(
    /** Clients (/users) and products (/products) offered when creating an order. */
    private val catalog: OrderCatalogRepository
) : OrderRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _orders = MutableStateFlow(initialOrders())
    override val orders: StateFlow<List<DistributorOrder>> = _orders.asStateFlow()

    private val _history = MutableStateFlow(initialHistory())
    override val history: StateFlow<List<OrderHistoryItem>> = _history.asStateFlow()

    /** Last server copy of each order, so an update keeps the address, total and notes. */
    private val serverCopies = ConcurrentHashMap<Int, OrderResponseDto>()

    init {
        scope.launch { refresh() }
    }

    override fun orderById(orderId: String): DistributorOrder? =
        _orders.value.firstOrNull { it.id.equals(orderId, ignoreCase = true) }

    override fun backendIdFor(orderId: String): Int? = orderById(orderId)?.remoteId

    override fun orderCodeFor(backendId: Int): String? =
        _orders.value.firstOrNull { it.remoteId == backendId }?.id
            ?: _history.value.firstOrNull { it.id == OrderMapper.codeFor(backendId) }?.id

    override suspend fun createOrder(draft: OrderDraft): DistributorOrder {
        val product = catalog.products.value.firstOrNull { it.id == draft.productId }
            ?: throw OrderSaveException(OrderSaveException.Reason.REJECTED)
        val request = OrderMapper.toCreateRequest(draft, product)
        val dto = when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.createOrder(request) } }) {
            is ApiResult.Ok -> result.value
            is ApiResult.Failed -> {
                if (result.failure.isNetwork) {
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "POST /orders")
                    throw OrderSaveException(OrderSaveException.Reason.NO_CONNECTION)
                }
                // 400 (below the minimum, inactive product, two producers) or 409 (not enough stock)
                throw OrderSaveException(OrderSaveException.Reason.REJECTED, result.failure.serverMessage)
            }
        }
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
        dto.id?.let { serverCopies[it] = dto }
        val created = (OrderMapper.map(dto, catalog::clientName) as? OrderMapper.Mapped.Active)?.order
            ?: throw OrderSaveException(OrderSaveException.Reason.REJECTED)
        _orders.update { listOf(created) + it.filterNot { existing -> existing.id == created.id } }

        // Catalog products bring their producer; otherwise the producer picked in the form is assigned
        if (created.producerId == null) draft.producerId?.let { assignProducer(created.id, it) }
        return orderById(created.id) ?: created
    }

    override suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean {
        val current = orderById(orderId) ?: return false
        if (!current.isEditable) return false
        val remoteId = current.remoteId

        if (remoteId != null) {
            // The server only changes the delivery date (and keeps address, total and notes)
            val copy = serverCopies[remoteId]
            val request = UpdateOrderRequestDto(
                deliveryDueDate = draft.deliveryDate.toString(),
                deliveryAddress = copy?.deliveryAddress ?: OrderCatalog.client(current.clientId)?.deliveryAddress.orEmpty(),
                totalAmount = copy?.totalAmount ?: 0.0,
                notes = copy?.notes
            )
            val ok = withContext(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.api.updateOrder(remoteId, request)
                    if (!response.isSuccessful) Log.w(TAG, "PUT orders/$remoteId answered ${response.code()}")
                    response.isSuccessful
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Log.w(TAG, "PUT orders/$remoteId failed: ${e.javaClass.simpleName}")
                    false
                }
            }
            if (!ok) return false
            replace(current.id) { it.copy(deliveryDate = draft.deliveryDate) }
            return true
        }

        // Sample orders: everything can change on this phone
        replace(current.id) {
            it.copy(
                clientName = catalog.clientName(draft.clientId) ?: OrderCatalog.client(draft.clientId)?.name ?: it.clientName,
                clientId = draft.clientId,
                productName = draft.productName.trim(),
                quantityKg = draft.quantityKg,
                pallets = OrderValidator.estimatePallets(draft.quantityKg),
                deliveryDate = draft.deliveryDate
            )
        }
        return true
    }

    override suspend fun deleteOrder(orderId: String): DeleteOrderResult {
        val order = orderById(orderId) ?: return DeleteOrderResult.NOT_FOUND
        if (!order.canBeDeleted()) return DeleteOrderResult.WINDOW_EXPIRED
        val remoteId = order.remoteId
        if (remoteId != null) {
            when (val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.deleteOrder(remoteId) } }) {
                is ApiResult.Ok -> Unit
                is ApiResult.Failed -> when {
                    // Someone else already removed it
                    result.failure.isNotFound -> Unit
                    // It has a delivery, invoice or payment: the server asks to cancel it instead
                    result.failure.isConflict -> return DeleteOrderResult.HAS_DEPENDENCIES
                    else -> return DeleteOrderResult.NOT_FOUND
                }
            }
            serverCopies.remove(remoteId)
        }
        _orders.update { list -> list.filterNot { it.id == order.id } }
        return DeleteOrderResult.SUCCESS
    }

    override suspend fun cancelOrder(orderId: String, reason: String): Boolean {
        val order = orderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.IN_PREPARATION) return false
        val remoteId = order.remoteId
        if (remoteId != null) {
            val result = withContext(Dispatchers.IO) {
                apiCall { RetrofitClient.api.cancelOrder(remoteId, CancelOrderRequestDto(reason.trim())) }
            }
            if (result !is ApiResult.Ok) return false
        }
        moveToHistory(order, OrderHistoryStatus.CANCELLED)
        return true
    }

    override suspend fun assignProducer(orderId: String, producerId: Int): Boolean {
        val order = orderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.IN_PREPARATION) return false
        val remoteId = order.remoteId
        if (remoteId != null) {
            val dto = withContext(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.api.assignProducer(remoteId, AssignProducerRequestDto(producerId))
                    if (!response.isSuccessful) Log.w(TAG, "PATCH orders/$remoteId/assign-producer answered ${response.code()}")
                    if (response.isSuccessful) response.body() else null
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    Log.w(TAG, "PATCH assign-producer failed: ${e.javaClass.simpleName}")
                    null
                }
            } ?: return false
            serverCopies[remoteId] = dto
        }
        val producerName = order.remoteId?.let { serverCopies[it]?.producerName }.orEmpty()
        replace(order.id) { it.copy(producerId = producerId, producerName = producerName.ifBlank { it.producerName }, status = OrderStatus.IN_PREPARATION) }
        return true
    }

    override fun validateLotCode(code: String): LotScanResult {
        val clean = code.trim().uppercase()
        val orderId = LOT_REGEX.find(clean)?.groupValues?.get(1)
            ?: return LotScanResult.NotRecognized(code)
        val order = orderById(orderId) ?: return LotScanResult.NotRecognized(code)
        return if (order.status == OrderStatus.ON_ROUTE) LotScanResult.Matched(code, order) else LotScanResult.NotOnRoute(code, order)
    }

    override suspend fun confirmReception(orderId: String): Boolean {
        val order = orderById(orderId) ?: return false
        if (order.status != OrderStatus.ON_ROUTE) return false
        val remoteId = order.remoteId
        if (remoteId != null) {
            // Received at the hub without incidents and without rating (rating null = not rated).
            // The server moves the order to Delivered and its delivery too (409 if not in transit).
            val result = withContext(Dispatchers.IO) {
                apiCall { RetrofitClient.api.confirmOrderReception(remoteId, ConfirmReceptionRequestDto(rating = null, hasIncidence = false, sealAndTemperatureOk = true)) }
            }
            if (result !is ApiResult.Ok) return false
        }
        moveToHistory(order, OrderHistoryStatus.DELIVERED)
        return true
    }

    private fun moveToHistory(order: DistributorOrder, status: OrderHistoryStatus) {
        _orders.update { list -> list.filterNot { it.id == order.id } }
        _history.update {
            listOf(
                OrderHistoryItem(
                    id = order.id,
                    clientName = order.clientName,
                    clientId = order.clientId,
                    producerName = order.producerName.ifBlank { PRODUCER_BY_ORDER[order.id].orEmpty() },
                    productName = order.productName,
                    completedAt = LocalDateTime.now(),
                    status = status
                )
            ) + it
        }
    }

    override suspend fun refresh() {
        val response = withContext(Dispatchers.IO) {
            try {
                RetrofitClient.api.getOrders()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Log.w(TAG, "GET orders failed: ${e.javaClass.simpleName}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "GET /orders: ${e.javaClass.simpleName}")
                null
            }
        } ?: return
        if (!response.isSuccessful) {
            Log.w(TAG, "GET orders answered ${response.code()}")
            return
        }
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
        val dtos = response.body().orEmpty()
        serverCopies.clear()
        val active = mutableListOf<DistributorOrder>()
        val finished = mutableListOf<OrderHistoryItem>()
        dtos.forEach { dto ->
            dto.id?.let { serverCopies[it] = dto }
            when (val mapped = OrderMapper.map(dto, catalog::clientName)) {
                is OrderMapper.Mapped.Active -> active += mapped.order
                is OrderMapper.Mapped.Finished -> finished += mapped.item
                null -> Unit
            }
        }
        val serverCodes = (active.map { it.id } + finished.map { it.id }).toSet()
        // The server list replaces the previous server copy; sample orders stay
        _orders.update { local -> active + local.filter { it.remoteId == null && it.id !in serverCodes } }
        _history.update { local -> finished + local.filter { it.id !in serverCodes && isSampleCode(it.id) } }
    }

    /** Sample orders and their history live only on this phone. */
    private fun isSampleCode(code: String) = code in SAMPLE_HISTORY_CODES || code in PRODUCER_BY_ORDER.keys

    private fun replace(orderId: String, block: (DistributorOrder) -> DistributorOrder) =
        _orders.update { list -> list.map { if (it.id == orderId) block(it) else it } }

    companion object {
        private const val TAG = "FruitLogix-Orders"

        /** Clients and products for new orders (shared with the composition root). */
        val catalog: OrderCatalogRepository by lazy { RemoteOrderCatalogRepository() }

        val instance: FakeOrderRepository by lazy { FakeOrderRepository(catalog) }

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

        private val SAMPLE_HISTORY_CODES = setOf("FX-1035", "FX-1032", "FX-1028")

        private fun sample(
            id: String, clientId: Int, product: String, kg: Double, pallets: Int,
            delivery: LocalDate, status: OrderStatus, createdAt: LocalDateTime
        ) = DistributorOrder(
            id = id,
            clientName = OrderCatalog.client(clientId)?.name.orEmpty(),
            productName = product,
            quantityKg = kg,
            pallets = pallets,
            deliveryDate = delivery,
            status = status,
            createdAt = createdAt,
            clientId = clientId
        )

        private fun initialOrders(): List<DistributorOrder> {
            val today = LocalDate.now()
            val now = LocalDateTime.now()
            return listOf(
                sample("FX-1042", 101, "Palta Hass", 12_000.0, 18, today, OrderStatus.ON_ROUTE, now.minusHours(40)),
                sample("FX-1041", 102, "Uva Red Globe", 10_400.0, 16, today, OrderStatus.ON_ROUTE, now.minusHours(52)),
                sample("FX-1038", 104, "Mango Kent", 15_000.0, 20, today.plusDays(1), OrderStatus.ON_ROUTE, now.minusHours(70)),
                sample("FX-1040", 103, "Fresa", 11_200.0, 14, today.plusDays(1), OrderStatus.IN_PREPARATION, now.minusHours(30)),
                sample("FX-1039", 105, "Mango Kent", 15_000.0, 20, today.plusDays(4), OrderStatus.PENDING, now.minusHours(8))
            )
        }

        private fun initialHistory(): List<OrderHistoryItem> {
            val now = LocalDateTime.now()
            return listOf(
                OrderHistoryItem("FX-1035", "Supermercados Lima Centro", 102, "Finca Los Andes", "Uva Red Globe",
                    now.minusDays(3).withHour(16).withMinute(20), OrderHistoryStatus.DELIVERED),
                OrderHistoryItem("FX-1032", "Distribuidora Pacífico", 106, "Agro Valle Verde", "Arándano",
                    now.minusDays(6).withHour(10).withMinute(45), OrderHistoryStatus.DELIVERED),
                OrderHistoryItem("FX-1028", "Mercados Lima Sur", 103, "Campos del Norte", "Mango Kent",
                    now.minusDays(10).withHour(9).withMinute(10), OrderHistoryStatus.CANCELLED)
            )
        }
    }
}
