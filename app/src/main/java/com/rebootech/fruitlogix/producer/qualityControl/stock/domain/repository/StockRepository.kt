package com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockMovement
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockMovementType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Inventory lot shown in the Stock tab and its detail.
 * @param chamberTempC Current cold-room temperature, when the lot is in a monitored chamber.
 */
data class StockItemModel(
    val id: String,
    val productName: String,
    val variety: String,
    val availableKg: Int,
    val totalCapacityKg: Int,
    val status: String,
    val chamberLocation: String,
    val minThresholdKg: Int = 100,
    val reservedKg: Int = 0,
    val chamberTempC: Double? = null,
    /** Product id on the server (GET /products), null only before it reaches the server. */
    val remoteId: Int? = null,
    /** Price per kg (server products). */
    val unitPrice: Double? = null,
    val minOrderKg: Int? = null,
    /** false = deactivated on the server: clients do not see it. */
    val isActive: Boolean = true
) {
    val isCritical: Boolean get() = availableKg < minThresholdKg
    val freeKg: Int get() = (availableKg - reservedKg).coerceAtLeast(0)
}

interface StockRepository {
    fun observeStock(): Flow<List<StockItem>>
    suspend fun getStockItem(productId: String): StockItem?

    companion object {
        /** The producer's products from the server (/products); no sample lots. */
        val stockItems = mutableStateListOf<StockItemModel>()

        /** Movements by lot id, newest first. */
        val movements = mutableStateMapOf<String, List<StockMovement>>()

        /** Sets the available kg and records the difference as a manual adjustment. */
        fun updateStockKg(id: String, newKg: Int) {
            val index = stockItems.indexOfFirst { it.id == id }
            if (index != -1) {
                val current = stockItems[index]
                val target = newKg.coerceAtLeast(0)
                val diff = target - current.availableKg
                if (diff == 0) return
                stockItems[index] = current.copy(availableKg = target)
                movements[id] = listOf(
                    StockMovement(StockMovementType.ADJUSTMENT, null, diff, LocalDateTime.now())
                ) + movements[id].orEmpty()
            }
        }

        fun updateThreshold(id: String, minKg: Int) {
            val index = stockItems.indexOfFirst { it.id == id }
            if (index != -1) {
                stockItems[index] = stockItems[index].copy(minThresholdKg = minKg.coerceAtLeast(0))
            }
        }
    }
}
