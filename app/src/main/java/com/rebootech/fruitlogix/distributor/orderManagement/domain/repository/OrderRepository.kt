package com.rebootech.fruitlogix.distributor.orderManagement.domain.repository

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DeleteOrderResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.DistributorOrder
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.LotScanResult
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderHistoryItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Flows instead of one-off reads, so every screen (Home, Orders, History) updates by itself
 * after creating, editing, deleting or receiving an order.
 */
interface OrderRepository {
    val orders: StateFlow<List<DistributorOrder>>
    val history: StateFlow<List<OrderHistoryItem>>

    fun orderById(orderId: String): DistributorOrder?

    suspend fun createOrder(draft: OrderDraft): DistributorOrder

    /** @return false if the order does not exist or is no longer editable. */
    suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean

    suspend fun deleteOrder(orderId: String): DeleteOrderResult

    fun validateLotCode(code: String): LotScanResult

    /** The lot arrived at the warehouse: the order is delivered and moves to history. */
    suspend fun confirmReception(orderId: String): Boolean
}
