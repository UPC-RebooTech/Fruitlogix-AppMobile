package com.rebootech.fruitlogix.distributor.orderManagement.domain.repository

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CatalogProduct
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CommercialClient
import kotlinx.coroutines.flow.StateFlow

/** Clients and products offered when creating an order. */
interface OrderCatalogRepository {
    val clients: StateFlow<List<CommercialClient>>

    /** Active products with stock. */
    val products: StateFlow<List<CatalogProduct>>

    /** Name of a client account (for "Client #12" → "cliente1"). */
    fun clientName(id: Int?): String? = clients.value.firstOrNull { it.id == id }?.name

    suspend fun refresh()
}
