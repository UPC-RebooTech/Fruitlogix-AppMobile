package com.rebootech.fruitlogix.logisticsMonitoring.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.logisticsMonitoring.data.FakeFleetRepository
import com.rebootech.fruitlogix.logisticsMonitoring.domain.model.ChartTimeFilter
import com.rebootech.fruitlogix.logisticsMonitoring.domain.repository.FleetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DispatchDetailViewModel(
    private val repository: FleetRepository = FakeFleetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DispatchDetailUiState>(DispatchDetailUiState.Loading)
    val uiState: StateFlow<DispatchDetailUiState> = _uiState.asStateFlow()

    fun loadDispatchDetail(unitId: String) {
        viewModelScope.launch {
            _uiState.value = DispatchDetailUiState.Loading
            val detail = repository.getDispatchDetail(unitId)
            if (detail != null) {
                val defaultTooltip = detail.detailedTemperaturePoints.firstOrNull { it.isExcursion }?.excursionTooltip
                _uiState.value = DispatchDetailUiState.Success(
                    detail = detail,
                    timeFilter = ChartTimeFilter.FULL_TRIP,
                    selectedTooltip = defaultTooltip
                )
            } else {
                _uiState.value = DispatchDetailUiState.Error("Dispatch detail not found for unit $unitId")
            }
        }
    }

    fun setTimeFilter(filter: ChartTimeFilter) {
        val currentState = _uiState.value
        if (currentState is DispatchDetailUiState.Success) {
            _uiState.value = currentState.copy(timeFilter = filter)
        }
    }

    fun toggleTooltip(tooltip: String?) {
        val currentState = _uiState.value
        if (currentState is DispatchDetailUiState.Success) {
            val nextTooltip = if (currentState.selectedTooltip == tooltip) null else tooltip
            _uiState.value = currentState.copy(selectedTooltip = nextTooltip)
        }
    }
}

