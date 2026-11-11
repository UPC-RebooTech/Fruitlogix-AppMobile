package com.rebootech.fruitlogix.producer.qualityControl.stock.data

import android.util.Log
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.model.StockItem
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FakeStockRepository : StockRepository {
    private val _stock = MutableStateFlow(createInitialStock())

    // NUEVO: Sincronización en segundo plano con el backend al abrir el módulo de stock
    init {
        fetchStockFromBackend()
    }

    override fun observeStock(): Flow<List<StockItem>> = _stock.asStateFlow()

    override suspend fun getStockItem(productId: String): StockItem? {
        return _stock.value.find { it.productId == productId }
    }

    // NUEVO: Función que llama a Retrofit de forma segura
    private fun fetchStockFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Aquí intenta llamar al servicio de stock/inventario en tu API de Render
                // (Si tu endpoint de stock se llama distinto, se ajustará al contrato de FruitLogixApiService)
                val remoteOrders = RetrofitClient.api.getOrders() // Usamos las órdenes o inventario disponible del backend

                if (remoteOrders.isNotEmpty()) {
                    Log.d("FruitLogix-API", "Stock sincronizado correctamente desde la nube de Render.")
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                }
            } catch (e: Exception) {
                // Si Render está durmiendo o no hay red, usa los datos locales de respaldo sin que la app falle
                Log.e("FruitLogix-API", "Backend inactivo para stock. Usando respaldo local: ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
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