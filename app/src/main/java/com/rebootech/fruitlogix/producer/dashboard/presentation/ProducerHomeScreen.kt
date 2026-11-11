package com.rebootech.fruitlogix.producer.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.HeroActionCard
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.OrderPickerSheet
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.PipelineBar
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.QuickActionButton
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.StockHealthSection
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.TaskRow
import com.rebootech.fruitlogix.producer.dashboard.presentation.components.UpcomingHarvestCard
import com.rebootech.fruitlogix.producer.infrastructureIot.ProducerInfrastructureIotRoutes
import com.rebootech.fruitlogix.producer.logisticsMonitoring.ProducerLogisticsMonitoringRoutes
import com.rebootech.fruitlogix.producer.navigation.ProducerRoute
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.paymentManagement.ProducerPaymentManagementRoutes
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.ConfirmDialog
import com.rebootech.fruitlogix.shared.ui.components.ErrorState
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.OfflineBanner
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ProducerHomeScreen(
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProducerHomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onSnackbarDismissed()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FruitLogixTheme.colors.bg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = FruitLogixTheme.colors.primary)
            }
        } else if (uiState.errorMessage != null) {
            ErrorState(
                message = uiState.errorMessage ?: "",
                onRetry = { viewModel.loadDashboard() },
                modifier = Modifier
                    .fillMaxSize()
                    .background(FruitLogixTheme.colors.bg)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FruitLogixTheme.colors.bg)
                    .padding(horizontal = FruitLogixTheme.spacing.sm)
            ) {
                // Banner offline
                item {
                    OfflineBanner(isOffline = uiState.isOffline)
                }

                // 1. Saludo y Finca (con corrección de colisión: chip fecha en fila independiente)
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.greeting,
                            fontFamily = PoppinsFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = FruitLogixTheme.typography.displayLarge,
                            color = FruitLogixTheme.colors.textOnLight,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(
                                    id = R.drawable.ic_truck_pin,
                                    contentDescription = null,
                                    tint = FruitLogixTheme.colors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.farmName,
                                    style = FruitLogixTheme.typography.bodyMedium,
                                    color = FruitLogixTheme.colors.textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Chip fecha en píldora clara
                            Box(
                                modifier = Modifier
                                    .clip(FruitLogixTheme.shapes.Pill)
                                    .background(FruitLogixTheme.colors.surfaceTint)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AppIcon(
                                        id = R.drawable.ic_schedule,
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.textOnLight,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = uiState.currentDateFormatted,
                                        style = FruitLogixTheme.typography.labelSmall,
                                        color = FruitLogixTheme.colors.textOnLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Hero "Next action" (White card)
                item {
                    HeroActionCard(
                        order = uiState.heroOrder,
                        shortageWarning = uiState.heroStockShortage,
                        onAcceptClick = { order -> viewModel.onAcceptOrderClick(order) },
                        onReviewDetailsClick = { order ->
                            onNavigate(ProducerOrderManagementRoutes.orderDetail(order.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 3. Acciones rápidas (4 columnas con weight 1f y texto centrado en 2 líneas máx)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            iconRes = R.drawable.ic_radar,
                            label = "Escanear lote",
                            onClick = { onNavigate(ProducerQualityControlRoutes.ScanLotAny) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            iconRes = R.drawable.ic_task_check,
                            label = "Nueva inspección",
                            onClick = {
                                onNavigate(ProducerRoute.Orders.route)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            iconRes = R.drawable.ic_inventory,
                            label = "Actualizar stock",
                            onClick = { onNavigate(ProducerRoute.Stock.route) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            iconRes = R.drawable.ic_fleet_truck,
                            label = "Registrar cosecha",
                            onClick = { onNavigate(ProducerQualityControlRoutes.RegisterHarvest) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 4. To do today (Dark card)
                item {
                    FruitLogixCard(
                        variant = CardVariant.Dark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tareas de hoy",
                                    fontFamily = PoppinsFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = FruitLogixTheme.typography.titleMedium,
                                    color = FruitLogixTheme.colors.textOnDark
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(FruitLogixTheme.colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.tasks.size.toString(),
                                        style = FruitLogixTheme.typography.labelSmall,
                                        color = FruitLogixTheme.colors.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            uiState.tasks.take(5).forEachIndexed { index, task ->
                                TaskRow(
                                    task = task,
                                    onClick = { onNavigate(task.destinationRoute) }
                                )
                                if (index < uiState.tasks.take(5).lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigate(ProducerInfrastructureIotRoutes.Notifications) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Ver todas las notificaciones →",
                                    style = FruitLogixTheme.typography.bodySmall,
                                    color = FruitLogixTheme.colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 5. KPIs 2x2 (KpiCard dark)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiCard(
                                caption = "PEDIDOS PENDIENTES",
                                value = uiState.summary.pendingOrdersCount.toString(),
                                icon = {
                                    AppIcon(
                                        id = R.drawable.ic_orders,
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.primary
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                caption = "LOTES DESPACHADOS",
                                value = uiState.summary.lotsDispatchedThisMonth.toString(),
                                icon = {
                                    AppIcon(
                                        id = R.drawable.ic_dispatch_truck,
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.primary
                                    )
                                },
                                footer = {
                                    Text(
                                        text = "este mes",
                                        style = FruitLogixTheme.typography.bodySmall,
                                        color = FruitLogixTheme.colors.textMuted
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiCard(
                                caption = "APROBACIÓN DE CALIDAD",
                                value = "${uiState.summary.qualityApprovalPct}%",
                                icon = {
                                    AppIcon(
                                        id = R.drawable.ic_shield_check,
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.primary
                                    )
                                },
                                deltaPill = {
                                    StatusBadge(
                                        text = "Óptimo",
                                        type = StatusBadgeType.SUCCESS,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            KpiCard(
                                caption = "PAGOS PENDIENTES",
                                value = "S/ ${"%,.0f".format(uiState.summary.pendingPaymentsAmount)}",
                                icon = {
                                    AppIcon(
                                        id = R.drawable.ic_invoices,
                                        contentDescription = null,
                                        tint = FruitLogixTheme.colors.primary
                                    )
                                },
                                footer = {
                                    Text(
                                        text = "Cobrado S/ ${"%,.0f".format(uiState.summary.paidPaymentsAmount)}",
                                        style = FruitLogixTheme.typography.bodySmall,
                                        color = FruitLogixTheme.colors.textMuted
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 6. Order pipeline (White card)
                item {
                    PipelineBar(
                        pipeline = uiState.pipeline,
                        onSegmentClick = { onNavigate(ProducerRoute.Orders.route) },
                        onNavigateOrdersClick = { onNavigate(ProducerRoute.Orders.route) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 7. Stock health (White card)
                item {
                    StockHealthSection(
                        stockItems = uiState.stockItems,
                        onItemClick = { item ->
                            onNavigate(ProducerQualityControlRoutes.stockDetail(item.productId))
                        },
                        onNavigateStockClick = { onNavigate(ProducerRoute.Stock.route) }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 8. Upcoming harvest (Tint card)
                item {
                    UpcomingHarvestCard(
                        harvest = uiState.upcomingHarvest,
                        onClick = { onNavigate(ProducerQualityControlRoutes.Harvests) }
                    )
                    // Spacing.lg + 80.dp para que no lo tape la BottomNavBar
                    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.lg + 80.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
        )
    }

    // Modal Bottom Sheet para "Nueva inspección"

//    if (uiState.showOrderPickerSheet) {
//        OrderPickerSheet(
//            orders = uiState.inspectingOrdersList,
//            onOrderSelected = { order ->
//                viewModel.onDismissOrderPicker()
//                onNavigate(ProducerQualityControlRoutes.inspection(order.id))
//            },
//            onDismiss = { viewModel.onDismissOrderPicker() }
//        )
//    }

    // Dialogo de confirmación para Aceptar Pedido
    if (uiState.showAcceptConfirmDialog && uiState.pendingAcceptOrder != null) {
        val order = uiState.pendingAcceptOrder!!
        ConfirmDialog(
            title = "¿Aceptar Pedido ${order.id}?",
            message = "Al aceptar el pedido de ${order.clientName}, los productos pasarán a estado 'En preparación'.",
            confirmText = "Aceptar",
            onConfirm = { viewModel.onConfirmAcceptOrder() },
            onDismiss = { viewModel.onDismissAcceptDialog() }
        )
    }
}
