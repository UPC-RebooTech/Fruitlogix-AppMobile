package com.rebootech.fruitlogix.producer.orderManagement.data

import android.util.Log
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.AssignedTruck
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.DeliveryMode
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderItem
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.QualityStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.shared.ui.components.SyncState
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.DeliveryResponseDto
import com.rebootech.fruitlogix.shared.core.network.HarvestBatchDto
import com.rebootech.fruitlogix.shared.core.network.OrderResponseDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.UpdateOrderStatusRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * The producer's orders.
 * - Server orders (GET /orders returns only the producer's own) are shown as "FX-<id>". Accepting,
 *   rejecting and dispatching them (start-dispatch of their delivery) are sent to the server.
 * - The quality status comes from the harvest batch linked to the order: Approved/Rejected by the
 *   distributor, otherwise "in review".
 * - There are no sample orders: without connection the list stays as it was last read.
 */
class FakeOrderRepository : OrderRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _orders = MutableStateFlow<List<Order>>(emptyList())

    init {
        scope.launch { refresh() }
    }

    override fun observeOrders(): Flow<List<Order>> = _orders.asStateFlow()

    /** The orders as last read (for the weekly forecast). */
    fun orderSnapshot(): List<Order> = _orders.value

    override fun orderById(orderId: String): Order? {
        val id = orderId.removePrefix("#")
        return _orders.value.firstOrNull { it.id.removePrefix("#").equals(id, ignoreCase = true) }
    }

    /** The server order linked to a harvest lot (for a scanned "LOT-12"). */
    fun orderByBatch(batchId: Int?): Order? = batchId?.let { id -> _orders.value.firstOrNull { it.batchId == id } }

    override suspend fun acceptOrder(orderId: String): Boolean {
        val order = orderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING) return false
        val remoteId = order.remoteId
        if (remoteId != null) {
            val result = withContext(Dispatchers.IO) {
                apiCall { RetrofitClient.api.updateOrderStatus(remoteId, UpdateOrderStatusRequestDto("accept")) }
            }
            if (result !is ApiResult.Ok) return false
            replace(order.id) { map(result.value, it) ?: it }
            return true
        }
        replace(order.id) { it.copy(status = OrderStatus.IN_PREPARATION) }
        return true
    }

    override suspend fun rejectOrder(orderId: String, reason: String): Boolean {
        val order = orderById(orderId) ?: return false
        if (order.status != OrderStatus.PENDING && order.status != OrderStatus.IN_PREPARATION) return false
        val remoteId = order.remoteId
        if (remoteId != null) {
            val result = withContext(Dispatchers.IO) {
                apiCall { RetrofitClient.api.updateOrderStatus(remoteId, UpdateOrderStatusRequestDto("reject", reason.trim())) }
            }
            if (result !is ApiResult.Ok) return false
        }
        replace(order.id) { it.copy(status = OrderStatus.REJECTED, rejectionReason = reason.trim()) }
        return true
    }

    override suspend fun submitQualityReport(orderId: String): Boolean {
        val order = orderById(orderId) ?: return false
        replace(order.id) { it.copy(qualityStatus = QualityStatus.PENDING, qualityRejectionReason = null, rejectionReason = null) }
        // The lot and its inspection were saved on the server: read the order again (batchId)
        if (order.remoteId != null) refresh()
        return true
    }

    override suspend fun dispatchOrder(orderId: String): Boolean {
        val order = orderById(orderId) ?: return false
        if (!order.isReadyToLoad) return false
        val deliveryId = order.deliveryId
        if (order.remoteId != null) {
            // Without a delivery from the distributor there is nothing to dispatch on the server
            if (deliveryId == null) return false
            val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.startDelivery(deliveryId) } }
            if (result !is ApiResult.Ok) return false
        }
        replace(order.id) { it.copy(status = OrderStatus.IN_TRANSIT, dispatchedAtFormatted = null) }
        return true
    }

    override suspend fun refresh() {
        val result = withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getOrders() } }
        if (result !is ApiResult.Ok) {
            Log.w(TAG, "GET orders failed")
            RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK, "GET /orders")
            return
        }
        RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
        // Batches are not filtered by the server: only the ones linked to these orders are used
        val batches = (withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getHarvestBatches() } } as? ApiResult.Ok)
            ?.value.orEmpty().associateBy { it.id }
        val previous = _orders.value.filter { it.remoteId != null }.associateBy { it.remoteId }
        val fromServer = result.value.mapNotNull { dto ->
            val delivery = if (dto.status.equals("InPreparation", true) || dto.status.equals("InTransit", true)) {
                dto.id?.let { deliveryFor(it) }
            } else null
            map(dto, previous[dto.id], dto.batchId?.let { batches[it] }, delivery)
        }
        // The server list is the whole list
        _orders.value = fromServer
    }

    private suspend fun deliveryFor(orderId: Int): DeliveryResponseDto? =
        (withContext(Dispatchers.IO) { apiCall { RetrofitClient.api.getDeliveriesByOrder(orderId) } } as? ApiResult.Ok)
            ?.value?.maxByOrNull { it.id }

    private fun replace(orderId: String, block: (Order) -> Order) =
        _orders.update { list -> list.map { if (it.id == orderId) block(it) else it } }

    /**
     * OrderResource → producer order. The client is shown by commercialClientName, or by its number
     * when the server has no name. Unknown statuses are kept as pending.
     */
    private fun map(
        dto: OrderResponseDto,
        previous: Order?,
        batch: HarvestBatchDto? = null,
        delivery: DeliveryResponseDto? = null
    ): Order? {
        val id = dto.id ?: return null
        val due = dto.deliveryDueDate?.take(10)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
        val status = when (dto.status?.lowercase(Locale.ROOT)) {
            "inpreparation" -> OrderStatus.IN_PREPARATION
            "intransit" -> OrderStatus.IN_TRANSIT
            "delivered", "deliveredwithincidence" -> OrderStatus.DELIVERED
            "rejected" -> OrderStatus.REJECTED
            "cancelled" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
        val batchId = dto.batchId ?: previous?.batchId
        val quality = when {
            batchId == null -> previous?.qualityStatus.takeIf { status == OrderStatus.IN_PREPARATION }
            batch?.status.equals("Approved", true) -> QualityStatus.APPROVED
            batch?.status.equals("Rejected", true) -> QualityStatus.REJECTED
            else -> QualityStatus.PENDING
        }
        val truck = delivery?.let {
            AssignedTruck(
                plate = it.vehiclePlate.orEmpty(),
                vehicleType = it.vehicleType.orEmpty(),
                driverName = it.driverName.orEmpty(),
                driverPhone = it.driverPhone.orEmpty(),
                etaFormatted = parseDateTime(it.estimatedTimeOfArrival)?.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
            )
        } ?: previous?.assignedTruck
        return Order(
            id = "FX-$id",
            clientName = dto.commercialClientName?.takeIf { it.isNotBlank() } ?: "#${dto.commercialClientId ?: "?"}",
            distributorName = previous?.distributorName.orEmpty(),
            items = dto.items.orEmpty().mapIndexed { index, item ->
                OrderItem(
                    productId = item.productId?.toString() ?: "item-$index",
                    productName = item.productName.orEmpty(),
                    kg = (item.quantityKg ?: 0.0).toInt()
                )
            },
            dueDate = due,
            status = status,
            qualityStatus = quality,
            assignedTruck = truck,
            rejectionReason = dto.cancellationReason,
            distributorNote = dto.notes,
            dispatchedAtFormatted = parseDateTime(delivery?.dispatchedAt)?.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
            deliveredAtFormatted = parseDateTime(dto.deliveredAt)?.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
            syncState = SyncState.SYNCED,
            deliveryMode = DeliveryMode.FARM_PICKUP,
            remoteId = id,
            batchId = batchId,
            deliveryId = delivery?.id ?: previous?.deliveryId
        )
    }

    private fun parseDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
            ?: runCatching { LocalDateTime.parse(value) }.getOrNull()
    }

    companion object {
        private const val TAG = "FruitLogix-ProducerOrders"

        val instance: FakeOrderRepository by lazy { FakeOrderRepository() }
    }
}
