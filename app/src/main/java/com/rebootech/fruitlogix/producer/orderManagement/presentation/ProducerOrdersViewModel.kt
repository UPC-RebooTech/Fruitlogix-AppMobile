package com.rebootech.fruitlogix.producer.orderManagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rebootech.fruitlogix.producer.orderManagement.data.FakeOrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.Order
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderFilters
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.StockShortage
import com.rebootech.fruitlogix.producer.orderManagement.domain.repository.OrderRepository
import com.rebootech.fruitlogix.producer.orderManagement.domain.usecase.FilterOrdersUseCase
import com.rebootech.fruitlogix.producer.qualityControl.stock.data.FakeStockRepository
import com.rebootech.fruitlogix.producer.qualityControl.stock.domain.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProducerOrdersViewModel(
    private val orderRepository: OrderRepository = FakeOrderRepository.instance,
    private val stockRepository: StockRepository = FakeStockRepository.instance,
    private val filterOrdersUseCase: FilterOrdersUseCase = FilterOrdersUseCase()
) : ViewModel() {

    private val _filters = MutableStateFlow(OrderFilters())
    private val _uiState = MutableStateFlow(ProducerOrdersUiState())
    val uiState: StateFlow<ProducerOrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            combine(
                orderRepository.observeOrders(),
                stockRepository.observeStock(),
                _filters
            ) { orders, stock, filters ->
                val awaitingCount = orders.count { it.status == OrderStatus.PENDING }
                val inShedCount = orders.count { it.status == OrderStatus.IN_PREPARATION }
                val activeVolume = orders
                    .filter { it.status in setOf(OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.IN_TRANSIT) }
                    .sumOf { it.totalKg }

                val counts = mutableMapOf<OrderStatus?, Int>()
                counts[null] = orders.size
                for (status in OrderStatus.values()) {
                    counts[status] = orders.count { it.status == status }
                }

                val shortages = mutableMapOf<String, StockShortage>()
                for (order in orders.filter { it.status == OrderStatus.PENDING }) {
                    for (item in order.items) {
                        val currentStock = stock.find {
                            it.productId == item.productId || it.productName.equals(item.productName, ignoreCase = true)
                        }
                        val available = currentStock?.availableKg ?: 0
                        if (available < item.kg) {
                            val diff = item.kg - available
                            shortages[order.id] = StockShortage(
                                productName = item.productName,
                                shortKg = diff
                            )
                            break
                        }
                    }
                }

                val filtered = filterOrdersUseCase(orders, filters)

                Triple(orders, filtered, Triple(awaitingCount, inShedCount, activeVolume)) to (counts to shortages)
            }
                .catch { ex ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = ex.message ?: "Error al cargar pedidos"
                        )
                    }
                }
                .collect { (ordersData, metaData) ->
                    val (allOrders, filteredOrders, metrics) = ordersData
                    val (awaiting, inShed, volume) = metrics
                    val (counts, shortages) = metaData

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = null,
                            allOrders = allOrders,
                            filteredOrders = filteredOrders,
                            filters = _filters.value,
                            awaitingActionCount = awaiting,
                            inShedCount = inShed,
                            activeVolumeKg = volume,
                            statusCounts = counts,
                            stockShortages = shortages
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _filters.update { it.copy(query = query) }
    }

    fun onStatusChipSelect(status: OrderStatus?) {
        _filters.update {
            if (status == null) {
                it.copy(statuses = emptySet())
            } else {
                it.copy(statuses = setOf(status))
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
            val success = orderRepository.acceptOrder(order.id)
            if (success) {
                _uiState.update {
                    it.copy(snackbarMessage = order.id)
                }
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onFilterButtonClick() {
        // TODO(producer): Abrir OrderFilterSheet en siguiente prompt
    }
}