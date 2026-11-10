package com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.producer.logisticsMonitoring.domain.ProducerPickupDispatch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProducerDeliveryUiState(
    val currentDispatch: ProducerPickupDispatch? = null,
    val isDispatched: Boolean = false
)

class ProducerDeliveryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProducerDeliveryUiState(
            currentDispatch = ProducerPickupDispatch(
                id = "DSP-882",
                orderCode = "ORD-2026-091",
                distributorName = "Distribuidora Frutícola Lima Centro",
                driverName = "Carlos Mendoza",
                vehiclePlate = "BCF-412 (Furgón Refrigerado)",
                driverPhone = "+51 987 654 321",
                etaMinutes = 25,
                assignedFruit = "Arándano Ventura - 80 Cajas",
                totalWeightKg = 1200.0,
                status = "EN_CAMINO"
            )
        )
    )
    val uiState: StateFlow<ProducerDeliveryUiState> = _uiState.asStateFlow()

    fun onConfirmPickup() {
        _uiState.update { current ->
            current.copy(
                isDispatched = true,
                currentDispatch = current.currentDispatch?.copy(status = "DESPACHADO")
            )
        }
    }
}