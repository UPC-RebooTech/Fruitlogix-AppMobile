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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                val distributorName = orders.firstOrNull()?.distributorName ?: "Carlos Mendoza"
                val awaitingCount = orders.count { it.status == OrderStatus.PENDING }
                val inShedCount = orders.count { it.status == OrderStatus.IN_PREPARATION }
                val activeVolume = orders
                    .filter { it.status in setOf(OrderStatus.PENDING, OrderStatus.IN_PREPARATION, OrderStatus.IN_TRANSIT) }
                    .sumOf { it.totalKg }

                // Base filtered by search & clients to dynamically derive status counts
                val baseFilteredOrders = orders.filter { order ->
                    val matchesQuery = if (filters.query.isBlank()) true else {
                        val q = filters.query.trim().lowercase()
                        order.id.lowercase().contains(q) ||
                        order.clientName.lowercase().contains(q) ||
                        order.items.any { it.productName.lowercase().contains(q) }
                    }
                    val matchesClients = if (filters.clientIds.isEmpty()) true else {
                        order.clientName in filters.clientIds
                    }
                    matchesQuery && matchesClients
                }

                val counts = mutableMapOf<OrderStatus?, Int>()
                counts[null] = baseFilteredOrders.size
                for (status in OrderStatus.values()) {
                    counts[status] = baseFilteredOrders.count { it.status == status }
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

                Triple(orders, filtered, Triple(awaitingCount, inShedCount, activeVolume)) to (counts to (shortages to distributorName))
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
                    val (counts, secondMeta) = metaData
                    val (shortages, distributorName) = secondMeta

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
                            stockShortages = shortages,
                            distributorName = distributorName
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _filters.update { it.copy(query = query) }
    }

    fun onClearQuery() {
        _filters.update { it.clearQuery() }
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

    fun onRemoveStatusFilter(status: OrderStatus) {
        _filters.update { it.removeStatus(status) }
    }

    fun onRemoveClientFilter(clientName: String) {
        _filters.update { it.removeClient(clientName) }
    }

    fun onClearAllFilters() {
        _filters.update { it.clearAll() }
    }

    fun onApplySuggestedInPrep() {
        _filters.update { it.copy(statuses = setOf(OrderStatus.IN_PREPARATION)) }
    }

    fun onApplySuggestedInTransit() {
        _filters.update { it.copy(statuses = setOf(OrderStatus.IN_TRANSIT)) }
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
                val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                _uiState.update {
                    it.copy(
                        snackbarMessage = order.id,
                        lastAcceptedOrder = order,
                        lastAcceptedTimeFormatted = nowTime
                    )
                }
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onRemoveDateRangeFilter() {
        _filters.update { it.clearDateRange() }
    }

    fun onFilterButtonClick() {
        _uiState.update { it.copy(showFilterSheet = true) }
    }

    fun onDismissFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun onApplyFilters(newFilters: OrderFilters) {
        _filters.update { current ->
            newFilters.copy(query = current.query)
        }
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun onAcceptOrderById(orderId: String) {
        val cleanId = orderId.removePrefix("#")
        viewModelScope.launch {
            val targetOrder = _uiState.value.allOrders.find {
                it.id.removePrefix("#") == cleanId
            }
            val idToAccept = targetOrder?.id ?: cleanId
            val success = orderRepository.acceptOrder(idToAccept)
            if (success && targetOrder != null) {
                val nowTime = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                _uiState.update {
                    it.copy(
                        snackbarMessage = targetOrder.id,
                        lastAcceptedOrder = targetOrder,
                        lastAcceptedTimeFormatted = nowTime
                    )
                }
            }
        }
    }
}
