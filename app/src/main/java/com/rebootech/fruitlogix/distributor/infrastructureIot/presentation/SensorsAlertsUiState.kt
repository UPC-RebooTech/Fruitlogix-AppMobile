package com.rebootech.fruitlogix.distributor.infrastructureIot.presentation

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.FruitThreshold
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice

data class SensorsAlertsUiState(
    val isLoading: Boolean = false,
    val devices: List<IotDevice> = emptyList(),
    val alerts: List<IotAlert> = emptyList(),
    val thresholds: List<FruitThreshold> = emptyList(),
    val editingThreshold: FruitThreshold? = null,
    val thresholdError: String? = null,
    val feedbackMessage: String? = null,
    val isRefreshing: Boolean = false
)