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

    suspend fun createOrder(draft: OrderDraft): DistributorOrder

    suspend fun updateOrder(orderId: String, draft: OrderDraft): Boolean

    suspend fun deleteOrder(orderId: String): DeleteOrderResult

    fun validateLotCode(code: String): LotScanResult

    suspend fun confirmReception(orderId: String): Boolean

    /** Sincroniza pedidos desde el backend en Render (/api/v1/orders). */
    suspend fun refresh() {}
}