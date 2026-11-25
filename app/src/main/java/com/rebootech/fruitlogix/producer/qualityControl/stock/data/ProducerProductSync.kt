package com.rebootech.fruitlogix.producer.qualityControl.stock.data

import android.util.Log
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockItemModel
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import com.rebootech.fruitlogix.shared.core.network.ApiResult
import com.rebootech.fruitlogix.shared.core.network.ProductDto
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import com.rebootech.fruitlogix.shared.core.network.SaveProductRequestDto
import com.rebootech.fruitlogix.shared.core.network.apiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/** What the producer types to publish a product (POST /products). */
data class NewProduct(
    val name: String,
    val fruitType: String,
    val variety: String,
    val description: String,
    val unitPrice: Double,
    val availableKg: Double,
    val minOrderKg: Double
)

data class ProductSaveResult(val ok: Boolean, val serverMessage: String? = null)

/**
 * The producer's catalog on the server (/products). For a Producer, GET /products returns only
 * their own products, inactive ones too. Each one is shown in the Stock list as "prod-<id>", (no sample lots).
 */
object ProducerProductSync {
    private const val TAG = "ProducerProducts"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val products = mutableMapOf<Int, ProductDto>()
    private val pendingKg = mutableMapOf<Int, Job>()

    suspend fun refresh() {
        val result = apiCall { RetrofitClient.api.getProducts() }
        if (result !is ApiResult.Ok) {
            Log.w(TAG, "GET /products failed: ${(result as ApiResult.Failed).failure}")
            return
        }
        products.clear()
        result.value.forEach { products[it.id] = it }
        withContext(Dispatchers.Main) {
            val local = StockRepository.stockItems.filter { it.remoteId == null }
            val previous = StockRepository.stockItems.filter { it.remoteId != null }.associateBy { it.remoteId }
            StockRepository.stockItems.clear()
            StockRepository.stockItems.addAll(result.value.map { toStock(it, previous[it.id]) } + local)
        }
    }

    suspend fun create(product: NewProduct): ProductSaveResult {
        val result = apiCall {
            RetrofitClient.api.createProduct(
                SaveProductRequestDto(
                    name = product.name.trim(),
                    fruitType = product.fruitType.trim(),
                    unitPrice = product.unitPrice,
                    availableKg = product.availableKg,
                    variety = product.variety.trim().ifBlank { null },
                    description = product.description.trim().ifBlank { null },
                    minOrderKg = product.minOrderKg,
                    // The server takes the producer from the account
                    producerId = null
                )
            )
        }
        return when (result) {
            is ApiResult.Ok -> {
                products[result.value.id] = result.value
                withContext(Dispatchers.Main) { StockRepository.stockItems.add(0, toStock(result.value, null)) }
                ProductSaveResult(true)
            }
            is ApiResult.Failed -> ProductSaveResult(false, result.failure.serverMessage)
        }
    }

    /**
     * Sends the new available kg of a server product. Quick +/- taps are joined: only the last
     * value is sent, half a second after the last tap.
     */
    fun pushKg(item: StockItemModel) {
        val id = item.remoteId ?: return
        pendingKg.remove(id)?.cancel()
        pendingKg[id] = scope.launch {
            delay(500)
            val current = StockRepository.stockItems.firstOrNull { it.remoteId == id } ?: return@launch
            save(id) { it.copy(availableKg = current.availableKg.toDouble()) }
        }
    }

    /** DELETE deactivates the product; PUT isActive = true brings it back. */
    suspend fun setActive(item: StockItemModel, active: Boolean): ProductSaveResult {
        val id = item.remoteId ?: return ProductSaveResult(false)
        if (!active) {
            val result = apiCall { RetrofitClient.api.deleteProduct(id) }
            if (result is ApiResult.Failed) return ProductSaveResult(false, result.failure.serverMessage)
            products[id]?.let { products[id] = it.copy(isActive = false) }
            replace(id) { it.copy(isActive = false) }
            return ProductSaveResult(true)
        }
        return save(id) { it.copy(isActive = true) }
    }

    private suspend fun save(id: Int, change: (SaveProductRequestDto) -> SaveProductRequestDto): ProductSaveResult {
        val dto = products[id] ?: return ProductSaveResult(false)
        val body = change(
            SaveProductRequestDto(
                name = dto.name.orEmpty(),
                fruitType = dto.fruitType.orEmpty(),
                unitPrice = dto.unitPrice ?: 0.0,
                availableKg = dto.availableKg ?: 0.0,
                variety = dto.variety,
                description = dto.description,
                minOrderKg = dto.minOrderKg ?: 0.0,
                imageUrl = dto.imageUrl,
                isActive = dto.isActive ?: true
            )
        )
        return when (val result = apiCall { RetrofitClient.api.updateProduct(id, body) }) {
            is ApiResult.Ok -> {
                products[id] = result.value
                replace(id) { toStock(result.value, it) }
                ProductSaveResult(true)
            }
            is ApiResult.Failed -> {
                Log.w(TAG, "PUT /products/$id failed: ${result.failure}")
                ProductSaveResult(false, result.failure.serverMessage)
            }
        }
    }

    private suspend fun replace(id: Int, block: (StockItemModel) -> StockItemModel) = withContext(Dispatchers.Main) {
        val index = StockRepository.stockItems.indexOfFirst { it.remoteId == id }
        if (index != -1) StockRepository.stockItems[index] = block(StockRepository.stockItems[index])
    }

    /** ProductResource → Stock row. Threshold and chamber are kept from the previous copy (phone only). */
    private fun toStock(dto: ProductDto, previous: StockItemModel?): StockItemModel {
        val kg = (dto.availableKg ?: 0.0).roundToInt()
        return StockItemModel(
            id = "prod-${dto.id}",
            productName = dto.name.orEmpty().ifBlank { dto.fruitType.orEmpty() },
            variety = dto.variety.orEmpty().ifBlank { dto.fruitType.orEmpty() },
            availableKg = kg,
            totalCapacityKg = maxOf(kg, previous?.totalCapacityKg ?: kg, 1),
            status = if (dto.isActive == false) "Inactive" else "Optimal",
            chamberLocation = previous?.chamberLocation ?: "—",
            minThresholdKg = previous?.minThresholdKg ?: (dto.minOrderKg ?: 0.0).roundToInt(),
            reservedKg = 0,
            remoteId = dto.id,
            unitPrice = dto.unitPrice,
            minOrderKg = dto.minOrderKg?.roundToInt(),
            isActive = dto.isActive ?: true
        )
    }
}
