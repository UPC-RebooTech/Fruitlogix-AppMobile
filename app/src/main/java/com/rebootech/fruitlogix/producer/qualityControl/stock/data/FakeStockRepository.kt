package com.rebootech.fruitlogix.producer.qualityControl.stock.data

import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeStockRepository : StockRepository {
    private val _stock = MutableStateFlow(createInitialStock())

    override fun observeStock(): Flow<List<StockItem>> = _stock.asStateFlow()

    override suspend fun getStockItem(productId: String): StockItem? {
        return _stock.value.find { it.productId == productId }
    }

    companion object {
        val instance: FakeStockRepository by lazy { FakeStockRepository() }

        private fun createInitialStock(): List<StockItem> {
            return listOf(
                StockItem(
                    productId = "mango",
                    productName = "Kent Mango",
                    availableKg = 315,
                    reservedKg = 0,
                    minThresholdKg = 100
                ),
                StockItem(
                    productId = "hass",
                    productName = "Hass Avocado",
                    availableKg = 420,
                    reservedKg = 0,
                    minThresholdKg = 100
                ),
                StockItem(
                    productId = "blueberries",
                    productName = "Blueberries",
                    availableKg = 140,
                    reservedKg = 0,
                    minThresholdKg = 100
                ),
                StockItem(
                    productId = "grapes",
                    productName = "Red Globe Grapes",
                    availableKg = 85,
                    reservedKg = 0,
                    minThresholdKg = 100
                )
            )
        }
    }
}

