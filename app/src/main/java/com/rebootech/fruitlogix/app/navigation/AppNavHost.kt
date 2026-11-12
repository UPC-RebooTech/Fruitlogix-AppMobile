package com.rebootech.fruitlogix.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.dashboard.presentation.HomeScreen
import com.rebootech.fruitlogix.distributor.fleetManagement.fleetManagementGraph
import com.rebootech.fruitlogix.distributor.infrastructureIot.infrastructureIotGraph
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.LogisticsMonitoringRoutes
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.logisticsMonitoringGraph
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.FleetScreen
import com.rebootech.fruitlogix.distributor.orderManagement.OrderManagementRoutes
import com.rebootech.fruitlogix.distributor.orderManagement.orderManagementGraph
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.OrdersScreen
import com.rebootech.fruitlogix.distributor.paymentManagement.PaymentManagementRoutes
import com.rebootech.fruitlogix.distributor.paymentManagement.paymentManagementGraph
import com.rebootech.fruitlogix.distributor.paymentManagement.presentation.InvoicesScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.profilesManagementGraph
import com.rebootech.fruitlogix.distributor.qualityControl.qualityControlGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    startDestination: String = Route.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            // Switching to another tab keeps each tab's own back stack
            fun openTab(route: String) = navController.navigate(route) {
                popUpTo(Route.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            HomeScreen(
                onScanReception = { navController.navigate(OrderManagementRoutes.ScanReception) },
                onNewOrder = { navController.navigate(OrderManagementRoutes.CreateOrder) },
                onOpenOrders = { openTab(Route.Orders.route) },
                onOpenFleet = { openTab(Route.Fleet.route) },
                onOpenSensors = {
                    navController.navigate(
                        com.rebootech.fruitlogix.distributor.infrastructureIot.InfrastructureIotRoutes.SensorsAlerts
                    )
                },
                onOpenUnit = { unitId ->
                    navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId))
                }
            )
        }
        composable(Route.Orders.route) {
            OrdersScreen(
                onNewOrderClick = {
                    navController.navigate(OrderManagementRoutes.CreateOrder)
                },
                onOrderClick = { orderId ->
                    navController.navigate(
                        OrderManagementRoutes.editOrder(orderId)
                    )
                },
                onHistoryClick = {
                    navController.navigate(
                        OrderManagementRoutes.OrderHistory
                    )
                },
                onScanReceptionClick = {
                    navController.navigate(
                        OrderManagementRoutes.ScanReception
                    )
                }
            )
        }
        composable(Route.Fleet.route) {
            FleetScreen(
                onOpenDispatch = { unitId ->
                    navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId))
                },
                // A truck at the gate: same reception screen as Orders
                onStartReception = { navController.navigate(OrderManagementRoutes.ScanReception) },
                onOpenSensors = {
                    navController.navigate(
                        com.rebootech.fruitlogix.distributor.infrastructureIot.InfrastructureIotRoutes.SensorsAlerts
                    )
                }
            )
        }
        composable(Route.Invoices.route) {
            InvoicesScreen(
                onInvoiceClick = { invoiceId ->
                    navController.navigate(
                        PaymentManagementRoutes.invoiceDetail(
                            invoiceId
                        )
                    )
                }
            )
        }

        // Bounded context nav graphs
        infrastructureIotGraph(navController, onOpenUnit = { unitId -> navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId)) })
        qualityControlGraph(navController)
        profilesManagementGraph(navController, onSignOut = onSignOut)
        fleetManagementGraph(navController)
        orderManagementGraph(navController)
        logisticsMonitoringGraph(navController)
        paymentManagementGraph(navController)
    }
}