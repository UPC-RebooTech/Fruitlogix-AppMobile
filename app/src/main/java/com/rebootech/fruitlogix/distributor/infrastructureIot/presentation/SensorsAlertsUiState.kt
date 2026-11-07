package com.rebootech.fruitlogix.distributor.infrastructureIot.presentation

import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotAlert
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.IotDevice

data class SensorsAlertsUiState(
    val isLoading: Boolean = false,
    val devices: List<IotDevice> = emptyList(),
    val alerts: List<IotAlert> = emptyList(),
    val isRefreshing: Boolean = false
)