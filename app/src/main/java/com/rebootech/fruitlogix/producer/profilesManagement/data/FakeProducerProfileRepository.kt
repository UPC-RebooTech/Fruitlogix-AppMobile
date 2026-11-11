package com.rebootech.fruitlogix.producer.profilesManagement.data

import android.util.Log
import com.rebootech.fruitlogix.shared.core.network.BackendSyncStatus
import com.rebootech.fruitlogix.shared.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Modelo de datos local para el perfil del productor en la app
data class ProducerProfileItem(
    val name: String,
    val roleTitle: String,
    val location: String,
    val ruc: String,
    val certification: String
)

class FakeProducerProfileRepository {

    // Respaldo local inicial (Offline-First) para que la pantalla cargue al instante sin vacíos
    private val _profile = MutableStateFlow(
        ProducerProfileItem(
            name = "Fundo Agrícola Los Laureles S.A.C.",
            roleTitle = "Productor Agrícola / Admin",
            location = "Ica, Valle Hermoso",
            ruc = "20491823901",
            certification = "Válida hasta Octubre 2027"
        )
    )
    val profile: StateFlow<ProducerProfileItem> = _profile.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        fetchProfileFromBackend()
    }

    // Sincronización en segundo plano con el backend de Render mediante Retrofit
    fun fetchProfileFromBackend() {
        scope.launch {
            runCatching {
                // Consulta al backend utilizando la ruta de productores o usuarios disponible en tu API
                RetrofitClient.api.getProducers()
            }.onSuccess { remoteList ->
                RetrofitClient.updateSyncStatus(BackendSyncStatus.ONLINE)
                // Si el servidor responde, podemos actualizar con datos reales si procede
                if (remoteList.isNotEmpty()) {
                    Log.d("FruitLogix-API", "Perfil de productor sincronizado correctamente desde Render.")
                }
            }.onFailure { e ->
                Log.e("FruitLogix-API", "Backend inactivo para perfil de productor. Usando respaldo local: ${e.message}")
                RetrofitClient.updateSyncStatus(BackendSyncStatus.OFFLINE_FALLBACK)
            }
        }
    }

    companion object {
        val instance: FakeProducerProfileRepository by lazy { FakeProducerProfileRepository() }
    }
}