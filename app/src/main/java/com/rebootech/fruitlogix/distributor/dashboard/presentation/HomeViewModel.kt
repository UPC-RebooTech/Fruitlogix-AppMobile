package com.rebootech.fruitlogix.distributor.dashboard.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.distributor.DistributorDependencies
import com.rebootech.fruitlogix.distributor.dashboard.data.FakeDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dashboardRepository = FakeDashboardRepository()
    private val sessionRepository = DataStoreSessionRepository.getInstance(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(dashboardRepository.observeDashboard(), sessionRepository.session) { data, session ->
                data to session?.displayName.orEmpty().trim().substringBefore(' ')
            }.collect { (data, firstName) ->
                _uiState.update { it.copy(isLoading = false, data = data, userName = firstName) }
            }
        }
        viewModelScope.launch { DistributorDependencies.orderRepository.refresh() }
        viewModelScope.launch { DistributorDependencies.billingRepository.refresh() }
        viewModelScope.launch { DistributorDependencies.profilesRepository.refresh() }
        viewModelScope.launch { DistributorDependencies.qualityRepository.refresh() }
        viewModelScope.launch { DistributorDependencies.fleetRepository.refresh() }
        viewModelScope.launch { DistributorDependencies.iotRepository.refresh() }
    }
}