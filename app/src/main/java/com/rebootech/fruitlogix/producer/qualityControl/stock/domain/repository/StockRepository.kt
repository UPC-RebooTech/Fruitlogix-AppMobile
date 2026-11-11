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
    val chamberTempC: Double? = null
) {
    val isCritical: Boolean get() = availableKg < minThresholdKg
    val freeKg: Int get() = (availableKg - reservedKg).coerceAtLeast(0)
}

interface StockRepository {
    fun observeStock(): Flow<List<StockItem>>
    suspend fun getStockItem(productId: String): StockItem?

    companion object {
        private val now = LocalDateTime.now()

        val stockItems = mutableStateListOf(
            StockItemModel("lot-104", "Kent Mango", "Grade A Export", 640, 900, "Optimal", "Chamber 01-A", reservedKg = 500, chamberTempC = 3.8),
            StockItemModel("lot-882", "Hass Avocado", "Premium Caliber", 320, 500, "Optimal", "Chamber 02-B", reservedKg = 300, chamberTempC = 5.5),
            StockItemModel("lot-094", "Blueberries", "Biloxi Fresh", 80, 150, "Review", "Staging Zone 4B", reservedKg = 80, chamberTempC = 2.1),
            StockItemModel("lot-402", "Red Globe Grapes", "Table Grapes", 120, 300, "Optimal", "Vault Section C", chamberTempC = 1.5)
        )

        /** Movements by lot id, newest first. */
        val movements = mutableStateMapOf(
            "lot-104" to listOf(
                StockMovement(StockMovementType.HARVEST_INTAKE, "Cuadrilla A", 320, now.minusHours(5)),
                StockMovement(StockMovementType.DISPATCH, "#1030", -350, now.minusDays(1))
            ),
            "lot-882" to listOf(
                StockMovement(StockMovementType.HARVEST_INTAKE, "Cuadrilla B", 180, now.minusDays(1))
            ),
            "lot-094" to listOf(
                StockMovement(StockMovementType.DISPATCH, "#1041", -120, now.minusHours(20)),
                StockMovement(StockMovementType.HARVEST_INTAKE, "Cuadrilla B", 200, now.minusDays(2))
            ),
            "lot-402" to listOf(
                StockMovement(StockMovementType.DISPATCH, "#1036", -150, now.minusHours(9)),
                StockMovement(StockMovementType.HARVEST_INTAKE, "Cuadrilla A", 120, now.minusDays(3))
            )
        )

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
