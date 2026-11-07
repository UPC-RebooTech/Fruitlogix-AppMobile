package com.rebootech.fruitlogix.distributor.profilesManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.distributor.profilesManagement.data.FakeProfilesRepository
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerItem
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerKpiSummary
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.repository.ProfilesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class ProducersUiState(
    val searchQuery: String = "",
    val producers: List<ProducerItem> = emptyList(),
    val filteredProducers: List<ProducerItem> = emptyList(),
    val kpis: ProducerKpiSummary = ProducerKpiSummary(),
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

class ProducersViewModel(
    private val repository: ProfilesRepository = FakeProfilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProducersUiState())
    val uiState: StateFlow<ProducersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.producers.collect { list ->
                recomputeState(list, _uiState.value.searchQuery)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        recomputeState(repository.producers.value, query)
    }

    fun registerProducer(
        name: String,
        farmName: String,
        location: String,
        cropType: String,
        email: String,
        phone: String,
        entityType: ProducerEntityType,
        onSuccess: () -> Unit
    ) {
        val result = repository.registerProducer(name, farmName, location, cropType, email, phone, entityType)
        result.fold(
            onSuccess = { created ->
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Productor '${created.name}' registrado con tipo estricto: ${created.entityType.backendValue}",
                        errorMessage = null
                    )
                }
                onSuccess()
            },
            onFailure = { err ->
                _uiState.update { it.copy(errorMessage = err.message) }
            }
        )
    }

    fun inviteProducer(
        name: String,
        emailOrPhone: String,
        location: String,
        cropType: String,
        onShareLinkReady: (String) -> Unit
    ) {
        val result = repository.inviteProducer(name, emailOrPhone, location, cropType)
        result.fold(
            onSuccess = { invited ->
                val inviteText = "¡Hola ${invited.name}! Carlos Mendoza te invita a unirte a la red de abastecimiento FruitLogix para el cultivo de ${invited.cropType}. Completa tu registro aquí: https://upc-rebootech.github.io/Fruitlogix-website/?invite=${invited.id}"
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Invitación generada para ${invited.name}",
                        errorMessage = null
                    )
                }
                onShareLinkReady(inviteText)
            },
            onFailure = { err ->
                _uiState.update { it.copy(errorMessage = err.message) }
            }
        )
    }

    fun updateProducer(updated: ProducerItem, onSuccess: () -> Unit) {
        val result = repository.updateProducer(updated)
        result.fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        feedbackMessage = "Ficha técnica de '${updated.name}' actualizada correctamente.",
                        errorMessage = null
                    )
                }
                onSuccess()
            },
            onFailure = { err ->
                _uiState.update { it.copy(errorMessage = err.message) }
            }
        )
    }

    fun deleteProducer(producerId: String) {
        repository.deleteProducer(producerId)
        _uiState.update {
            it.copy(feedbackMessage = "Productor eliminado de la red activa.", errorMessage = null)
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    private fun recomputeState(list: List<ProducerItem>, query: String) {
        val filtered = if (query.isBlank()) {
            list
        } else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.farmName.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true) ||
                        it.cropType.contains(query, ignoreCase = true)
            }
        }

        val avgQuality = if (list.isEmpty()) 0.0 else {
            String.format(Locale.US, "%.1f", list.map { it.qualityRating }.average()).toDouble()
        }
        val totalTons = String.format(Locale.US, "%.1f", list.sumOf { it.weeklyTons }).toDouble()
        val totalIncidents = list.sumOf { it.incidents30d }

        _uiState.update {
            it.copy(
                searchQuery = query,
                producers = list,
                filteredProducers = filtered,
                kpis = ProducerKpiSummary(
                    totalProducers = list.size,
                    averageQuality = avgQuality,
                    weeklyProductionTons = totalTons,
                    incidentsLast30d = totalIncidents
                )
            )
        }
    }
}