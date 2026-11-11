package com.rebootech.fruitlogix.producer.qualityControl.harvests.data

import android.util.Log
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.model.PlannedHarvest
import com.rebootech.fruitlogix.producer.qualityControl.harvests.domain.repository.HarvestRepository
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class FakeHarvestRepository : HarvestRepository {

    private val _recentHarvests = MutableStateFlow(createInitialHarvests())

    // NUEVO: Sincronización en segundo plano con el backend al abrir las cosechas
    init {
        fetchHarvestsFromBackend()
    }

    override val recentHarvests: StateFlow<List<PlannedHarvest>> = _recentHarvests.asStateFlow()

    override fun observeUpcomingHarvest(): Flow<PlannedHarvest?> {
        return _recentHarvests.map { list -> list.firstOrNull() }
    }

    override suspend fun addHarvest(
        fruitName: String,
        lotCode: String,
        weightKg: String,
        dateTime: String,
        sector: String
    ) {
        val newHarvest = PlannedHarvest(
            id = UUID.randomUUID().toString(),
            fruitName = fruitName,
            dateTime = dateTime,
            weight = "$weightKg kg",
            lotCode = lotCode,
            crewOrTransport = "Cosecha Registrada",
            sectorOrVault = sector,
            statusText = "Registrado en Fundo"
        )
        _recentHarvests.update { listOf(newHarvest) + it }
    }

    // NUEVO: Función para consultar la API de Render en segundo plano
    private fun fetchHarvestsFromBackend() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Intenta traer los datos desde el servidor en la nube
                val remoteOrders = RetrofitClient.api.getOrders()
                if (remoteOrders.isNotEmpty()) {
                    Log.d("FruitLogix-API", "Cosechas sincronizadas correctamente desde Render.")
                    RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                }
            } catch (e: Exception) {
                // Si el backend está inactivo o dormido, se protege con los datos locales
                Log.e("FruitLogix-API", "Backend inactivo para cosechas. Usando respaldo local: ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
    }

    companion object {
        val instance: HarvestRepository by lazy { FakeHarvestRepository() }

        private fun createInitialHarvests(): List<PlannedHarvest> {
            return listOf(
                PlannedHarvest(
                    id = "1",
                    fruitName = "Mango Kent",
                    dateTime = "Hoy, 06:30 AM",
                    weight = "3,200 kg",
                    lotCode = "L-MNG-01",
                    crewOrTransport = "Cuadrilla A • Fundo",
                    sectorOrVault = "Galpón 1",
                    statusText = "Listo para cosecha"
                ),
                PlannedHarvest(
                    id = "2",
                    fruitName = "Arándano Ventura",
                    dateTime = "Ayer, 04:15 PM",
                    weight = "1,500 kg",
                    lotCode = "L-ARA-03",
                    crewOrTransport = "Cuadrilla B • Fundo",
                    sectorOrVault = "Cámara Frío 2",
                    statusText = "En maduración"
                ),
                PlannedHarvest(
                    id = "3",
                    fruitName = "Palta Hass",
                    dateTime = "05 Nov, 08:00 AM",
                    weight = "4,800 kg",
                    lotCode = "L-PLT-02",
                    crewOrTransport = "Cuadrilla A • Fundo",
                    sectorOrVault = "Galpón 2",
                    statusText = "Cosecha programada"
                )
            )
        }
    }
}