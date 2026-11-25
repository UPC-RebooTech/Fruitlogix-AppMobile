package com.rebootech.fruitlogix.distributor.orderManagement.data

import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CatalogProduct
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.CommercialClient
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderCatalog
import com.rebootech.fruitlogix.distributor.orderManagement.domain.repository.OrderCatalogRepository
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Clients from GET /users (a Distributor sees every account; the server does not say which ones are
 * commercial clients, so the list shows usernames) and products from GET /products?available=true.
 * Without connection the offline list of [OrderCatalog] is used.
 */
class RemoteOrderCatalogRepository : OrderCatalogRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _clients = MutableStateFlow(OrderCatalog.clients)
    override val clients: StateFlow<List<CommercialClient>> = _clients.asStateFlow()

    private val _products = MutableStateFlow(OrderCatalog.products)
    override val products: StateFlow<List<CatalogProduct>> = _products.asStateFlow()

    init {
        scope.launch { refresh() }
    }

    override suspend fun refresh() {
        withContext(Dispatchers.IO) {
            (apiCall { RetrofitClient.api.getUsers() } as? ApiResult.Ok)?.value
                ?.filter { !it.username.isNullOrBlank() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { users -> _clients.value = users.map { CommercialClient(it.id, it.username!!) }.sortedBy { it.name } }

            (apiCall { RetrofitClient.api.getProducts(available = true) } as? ApiResult.Ok)?.value
                ?.filter { it.isActive != false && !it.name.isNullOrBlank() }
                ?.takeIf { it.isNotEmpty() }
                ?.let { list ->
                    _products.value = list.map {
                        CatalogProduct(
                            id = it.id,
                            name = it.name!!,
                            pricePerKg = it.unitPrice ?: 0.0,
                            producerId = it.producerId,
                            producerName = it.producerName.orEmpty(),
                            minOrderKg = it.minOrderKg ?: 0.0,
                            availableKg = it.availableKg
                        )
                    }
                }
        }
    }
}
