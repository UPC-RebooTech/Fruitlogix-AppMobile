package com.rebootech.fruitlogix.producer.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.data.FakeDashboardRepository
import com.rebootech.fruitlogix.producer.dashboard.domain.repository.DashboardRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.shared.core.model.UiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class ProducerHomeViewModel(
    private val dashboardRepository: DashboardRepository = FakeDashboardRepository.instance
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProducerHomeUiState())
    val uiState: StateFlow<ProducerHomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val dayPart = when (LocalTime.now().hour) {
                in 6..11 -> DayPart.MORNING
                in 12..18 -> DayPart.AFTERNOON
                else -> DayPart.EVENING
            }

            dashboardRepository.observeDashboard()
                .catch {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = UiText.StringResource(R.string.producer_home_error_loading)
                        )
                    }
                }
                .collect { data ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            dayPart = dayPart,
                            today = LocalDate.now(),
                            heroOrder = data.heroOrder,
                            heroStockShortage = data.heroStockShortage,
                            tasks = data.tasks,
                            summary = data.summary,
                            pipeline = data.pipeline,
                            stockItems = data.stockItems,
                            upcomingHarvest = data.upcomingHarvest
                        )
                    }
                }
        }
    }

    fun onAcceptOrderClick(order: Order) {
        _uiState.update { it.copy(showAcceptConfirmDialog = true, pendingAcceptOrder = order) }
    }

    fun onDismissAcceptDialog() {
        _uiState.update { it.copy(showAcceptConfirmDialog = false, pendingAcceptOrder = null) }
    }

    fun onConfirmAcceptOrder() {
        val order = _uiState.value.pendingAcceptOrder ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(showAcceptConfirmDialog = false, pendingAcceptOrder = null) }
            val success = dashboardRepository.acceptOrder(order.id)
            if (success) {
                _uiState.update {
                    it.copy(
                        snackbarMessage = UiText.StringResource(
                            R.string.producer_home_order_accepted,
                            arrayOf(order.id)
                        )
                    )
                }
            }
        }
    }

    fun onQuickActionNewInspectionClick(orders: List<Order>) {
        _uiState.update {
            it.copy(
                // Only orders in preparation with NO report yet need a new inspection.
                // PENDING = report submitted (in review); REJECTED has its own task.
                inspectingOrdersList = orders.filter { o ->
                    o.status == OrderStatus.IN_PREPARATION && o.qualityStatus == null
                },
                showOrderPickerSheet = true
            )
        }
    }

    fun onDismissOrderPicker() {
        _uiState.update { it.copy(showOrderPickerSheet = false) }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
