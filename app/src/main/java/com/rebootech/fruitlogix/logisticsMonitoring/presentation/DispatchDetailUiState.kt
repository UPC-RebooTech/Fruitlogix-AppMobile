package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ChartTimeFilter
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.DispatchDetail

sealed interface DispatchDetailUiState {
    object Loading : DispatchDetailUiState
    data class Success(
        val detail: DispatchDetail,
        val timeFilter: ChartTimeFilter = ChartTimeFilter.FULL_TRIP,
        val selectedTooltip: String? = null
    ) : DispatchDetailUiState
    data class Error(val message: String) : DispatchDetailUiState
}

