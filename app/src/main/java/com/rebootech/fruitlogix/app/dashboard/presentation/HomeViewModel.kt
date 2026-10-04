package com.rebootech.fruitlogix.app.dashboard.presentation

import androidx.lifecycle.ViewModel
import com.rebootech.fruitlogix.app.dashboard.data.FakeDashboardRepository
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val repository = FakeDashboardRepository()

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        val data = repository.getDashboardData()
        _uiState.update {
            it.copy(
                isLoading = false,
                greeting = data.greeting,
                priorityActions = data.priorityActions,
                priorityReadyCount = data.priorityActions.size,
                predictiveAlert = data.predictiveAlert,
                kpis = data.kpis,
                actionItems = data.actionItems,
                fleetUnits = data.fleetUnits,
                fleetInTransitCount = data.fleetInTransitCount,
                complianceSummary = data.complianceSummary
            )
        }
    }

    fun onLanguageSelected(language: com.rebootech.fruitlogix.shared.ui.components.AppLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }
}
