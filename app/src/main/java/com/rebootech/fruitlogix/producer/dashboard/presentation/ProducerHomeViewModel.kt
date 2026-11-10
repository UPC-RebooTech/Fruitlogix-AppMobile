package com.rebootech.fruitlogix.producer.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.dashboard.data.FakeDashboardRepository
import com.rebootech.fruitlogix.producer.dashboard.domain.repository.DashboardRepository
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

            // Saludo según la hora
            val greetingText = when (LocalTime.now().hour) {
                in 6..11 -> "Buenos días, Alexandra"
                in 12..18 -> "Buenas tardes, Alexandra"
                else -> "Buenas noches, Alexandra"
            }

            val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM", Locale("es", "ES"))
            val todayStr = LocalDate.now().format(dateFormatter).uppercase()

            dashboardRepository.observeDashboard()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al cargar el panel"
                        )
                    }
                }
                .collect { data ->
                    val inspectingList = FakeOrderRepository.instance.let { repo ->
                        // Pedidos IN_PREPARATION sin reporte enviado (#1048, #1044)
                        val all = (data.heroOrder?.let { listOf(it) } ?: emptyList())
                        // Obtenemos los de preparación de los pedidos
                        emptyList<Order>()
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            greeting = greetingText,
                            currentDateFormatted = todayStr,
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
                _uiState.update { it.copy(snackbarMessage = "Pedido ${order.id} aceptado exitosamente") }
            }
        }
    }

    fun onQuickActionNewInspectionClick(orders: List<Order>) {
        _uiState.update {
            it.copy(
                // Only orders in preparation with NO report yet need a new inspection.
                // PENDING = report submitted (in review); REJECTED has its own task.
                // #1044 (REJECTED) intentionally excluded here.
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

