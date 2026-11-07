package com.rebootech.fruitlogix.producer.orderManagement.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.logisticsMonitoring.ProducerLogisticsMonitoringRoutes
import com.rebootech.fruitlogix.producer.navigation.ProducerRoute
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.orderManagement.domain.model.OrderStatus
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.ActiveFilterChipsRow
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.NoOrdersFoundCard
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.OrderCard
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.OrderSearchBar
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.OrderStatusChips
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.OrderFilterSheet
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.OrderSummaryTiles
import com.rebootech.fruitlogix.producer.orderManagement.presentation.components.PendingAllCaughtUpCard
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.EmptyState
import com.rebootech.fruitlogix.shared.ui.components.ErrorState
import com.rebootech.fruitlogix.shared.ui.components.OfflineBanner
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun ProducerOrdersScreen(
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProducerOrdersViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { orderId ->
            snackbarHostState.showSnackbar("Pedido #$orderId aceptado con éxito")
            viewModel.onSnackbarDismissed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
    ) {
        when {
            uiState.isLoading -> {
                OrdersSkeletonLoading()
            }
            uiState.errorMessage != null -> {
                ErrorState(
                    message = uiState.errorMessage ?: "",
                    onRetry = { viewModel.loadOrders() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = FruitLogixTheme.spacing.sm)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OfflineBanner(isOffline = uiState.isOffline)
                    }

                    // 1. Barra de búsqueda y botón de filtros
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        OrderSearchBar(
                            query = uiState.filters.query,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            onScanClick = {
                                onNavigate(ProducerQualityControlRoutes.ScanLotAny)
                            },
                            onFilterClick = {
                                viewModel.onFilterButtonClick()
                            },
                            hasActiveFilters = uiState.filters.hasActiveFilters
                        )
                    }

                    // 1.1 Fila de píldoras de filtros activos (si existen)
                    if (uiState.filters.hasActiveFilters) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            ActiveFilterChipsRow(
                                filters = uiState.filters,
                                onRemoveStatus = { viewModel.onRemoveStatusFilter(it) },
                                onRemoveClient = { viewModel.onRemoveClientFilter(it) },
                                onClearQuery = { viewModel.onClearQuery() },
                                onRemoveDateRange = { viewModel.onRemoveDateRangeFilter() },
                            )
                        }
                    }

                    // 2. Tres tiles de resumen
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OrderSummaryTiles(
                            awaitingCount = uiState.awaitingActionCount,
                            inShedCount = uiState.inShedCount,
                            activeVolumeKg = uiState.activeVolumeKg
                        )
                    }

                    // 3. Fila scrolleable de chips de estado
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        OrderStatusChips(
                            selectedStatuses = uiState.filters.statuses,
                            statusCounts = uiState.statusCounts,
                            onStatusSelected = { viewModel.onStatusChipSelect(it) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 4. Lista de pedidos filtrados o Estados Vacíos (Casos A, B, C)
                    if (uiState.filteredOrders.isEmpty()) {
                        val isPendingTabOnly = uiState.filters.statuses == setOf(OrderStatus.PENDING) &&
                                uiState.filters.query.isBlank() &&
                                uiState.filters.clientIds.isEmpty()

                        if (isPendingTabOnly) {
                            // CASO B: tab Pending sin pedidos
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                PendingAllCaughtUpCard(
                                    distributorName = uiState.distributorName,
                                    inShedCount = uiState.inShedCount,
                                    lastAcceptedOrder = uiState.lastAcceptedOrder,
                                    lastAcceptedTime = uiState.lastAcceptedTimeFormatted,
                                    onViewInPrep = { viewModel.onApplySuggestedInPrep() },
                                    onCheckStock = { onNavigate(ProducerRoute.Stock.route) }
                                )
                            }
                        } else if (uiState.filters.hasActiveFilters) {
                            // CASO A: sin resultados de búsqueda o filtros activos
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                NoOrdersFoundCard(
                                    onClearFilters = { viewModel.onClearAllFilters() },
                                    onApplyInPrep = { viewModel.onApplySuggestedInPrep() },
                                    onApplyInTransit = { viewModel.onApplySuggestedInTransit() }
                                )
                            }
                        } else {
                            // CASO C: cualquier otro tab vacío o lista general
                            item {
                                Spacer(modifier = Modifier.height(24.dp))
                                EmptyState(
                                    title = stringResource(R.string.producer_orders_empty_title),
                                    message = stringResource(R.string.producer_orders_empty_desc)
                                )
                            }
                        }
                    } else {
                        items(
                            items = uiState.filteredOrders,
                            key = { it.id }
                        ) { order ->
                            OrderCard(
                                order = order,
                                stockShortage = uiState.stockShortages[order.id],
                                onCardClick = {
                                    // Si el pedido está en preparación o tránsito, abre el recojo del camión
                                    if (order.status == OrderStatus.IN_PREPARATION || order.status == OrderStatus.IN_TRANSIT) {
                                        onNavigate(ProducerLogisticsMonitoringRoutes.loadTruck(order.id))
                                    } else {
                                        onNavigate(ProducerOrderManagementRoutes.orderDetail(order.id))
                                    }
                                },
                                onAcceptOrder = {
                                    viewModel.onAcceptOrderClick(order)
                                },
                                onRespondOrder = {
                                    onNavigate(ProducerOrderManagementRoutes.respondOrder(order.id))
                                },
                                onReviewOrder = {
                                    onNavigate(ProducerOrderManagementRoutes.orderDetail(order.id))
                                },
                                onPrepareOrder = {
                                    onNavigate(ProducerQualityControlRoutes.prepareOrder(order.id))
                                },
                                onLoadTruck = {
                                    onNavigate(ProducerLogisticsMonitoringRoutes.loadTruck(order.id))
                                },
                                onViewReport = {
                                    onNavigate(ProducerQualityControlRoutes.reportDetail(order.id))
                                },
                                onReinspect = {
                                    onNavigate(ProducerQualityControlRoutes.inspection(order.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // 5. Pie de lista con padding inferior para no solapar la BottomNavBar
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.producer_orders_footer_loaded),
                                style = FruitLogixTheme.typography.bodySmall,
                                color = FruitLogixTheme.colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.lg + 80.dp))
                    }
                }
            }
        }

        // Diálogo de confirmación para Aceptar Pedido
        if (uiState.showAcceptConfirmDialog && uiState.pendingAcceptOrder != null) {
            val order = uiState.pendingAcceptOrder!!
            ConfirmDialog(
                title = stringResource(R.string.producer_orders_accept_dialog_title, order.id),
                message = stringResource(R.string.producer_orders_accept_dialog_desc),
                confirmText = stringResource(R.string.producer_orders_accept_confirm),
                dismissText = stringResource(R.string.producer_orders_accept_cancel),
                onConfirm = { viewModel.onConfirmAcceptOrder() },
                onDismiss = { viewModel.onDismissAcceptDialog() }
            )
        }

        // Hoja Modal de Filtros (OrderFilterSheet)
        if (uiState.showFilterSheet) {
            OrderFilterSheet(
                initialFilters = uiState.filters,
                allOrders = uiState.allOrders,
                statusCounts = uiState.statusCounts,
                onDismiss = { viewModel.onDismissFilterSheet() },
                onApplyFilters = { newFilters ->
                    viewModel.onApplyFilters(newFilters)
                }
            )
        }

        // Host de snackbars
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
        )
    }
}

@Composable
private fun OrdersSkeletonLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FruitLogixTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        // Search bar skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(FruitLogixTheme.colors.surfaceLight)
        )
        // Summary tiles skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(FruitLogixTheme.colors.surfaceTint.copy(alpha = 0.5f))
        )
        // Chips skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(FruitLogixTheme.shapes.Pill)
                .background(FruitLogixTheme.colors.surfaceLight)
        )
        // Card skeletons
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.2f))
            )
        }
    }
}
