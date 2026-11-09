package com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository

import androidx.compose.runtime.mutableStateListOf
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import kotlinx.coroutines.flow.Flow

data class StockItemModel(
    val id: String,
    val productName: String,
    val variety: String,
    val availableKg: Int,
    val totalCapacityKg: Int,
    val status: String,
    val chamberLocation: String
)

interface StockRepository {
    fun observeStock(): Flow<List<StockItem>>
    suspend fun getStockItem(productId: String): StockItem?

    companion object {
        val stockItems = mutableStateListOf(
            StockItemModel("lot-104", "Kent Mango", "Grade A Export", 640, 900, "Optimal", "Chamber 01-A"),
            StockItemModel("lot-882", "Hass Avocado", "Premium Caliber", 320, 500, "Optimal", "Chamber 02-B"),
            StockItemModel("lot-094", "Blueberries", "Biloxi Fresh", 80, 150, "Review", "Staging Zone 4B"),
            StockItemModel("lot-402", "Red Globe Grapes", "Table Grapes", 120, 300, "Optimal", "Vault Section C")
        )

        fun updateStockKg(id: String, newKg: Int) {
            val index = stockItems.indexOfFirst { it.id == id }
            if (index != -1) {
                val current = stockItems[index]
                stockItems[index] = current.copy(availableKg = newKg.coerceAtLeast(0))
            }
        }
    }
}