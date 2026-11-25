package com.rebootech.fruitlogix.producer.qualityControl.stock.data

import androidx.compose.runtime.snapshotFlow
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Stock per product, read from the producer's catalog on the server (/products, kept in
 * [StockRepository.stockItems] by [ProducerProductSync]). There is no sample stock.
 */
class FakeStockRepository : StockRepository {

    init {
        // Home and Orders read the stock too, not only the Stock tab
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch { ProducerProductSync.refresh() }
    }

    override fun observeStock(): Flow<List<StockItem>> = snapshotFlow {
        StockRepository.stockItems.filter { it.isActive }.map { item ->
            StockItem(
                productId = item.id,
                productName = item.productName,
                availableKg = item.availableKg,
                reservedKg = item.reservedKg,
                minThresholdKg = item.minThresholdKg
            )
        }
    }

    override suspend fun getStockItem(productId: String): StockItem? =
        observeStock().first().find { it.productId == productId }

    companion object {
        val instance: FakeStockRepository by lazy { FakeStockRepository() }
    }
}
