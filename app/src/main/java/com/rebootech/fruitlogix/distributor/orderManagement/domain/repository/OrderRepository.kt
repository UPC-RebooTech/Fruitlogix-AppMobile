package com.rebootech.fruitlogix.distributor.orderManagement.domain.repository

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import kotlinx.coroutines.flow.StateFlow

interface OrderRepository {
    val orders: StateFlow<List<DistributorOrder>>
    val history: StateFlow<List<OrderHistoryItem>>

    fun orderById(orderId: String): DistributorOrder?

    /** Devuelve el ID numérico real de la base de datos en Render si la orden ya está persistida. */
    fun backendIdFor(orderId: String): Int? = null

    /** The order code (e.g. "FX-1043") of a server order id; null when the order is not known. */
    fun orderCodeFor(backendId: Int): String? = null

    suspend fun createOrder(draft: OrderDraft): DistributorOrder

    suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean

    suspend fun deleteOrder(orderId: String): DeleteOrderResult

    /**
     * Assigns a producer of the network (server id). On the server this moves a pending order to
     * "in preparation" (PATCH /orders/{id}/assign-producer). @return false when it could not be saved.
     */
    suspend fun assignProducer(orderId: String, producerId: Int): Boolean = false

    fun validateLotCode(code: String): LotScanResult

    suspend fun confirmReception(orderId: String): Boolean

    suspend fun refresh() {}
}