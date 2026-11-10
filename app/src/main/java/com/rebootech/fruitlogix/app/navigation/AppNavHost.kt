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
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.logisticsMonitoringArrivalsGraph
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
    onSwitchRole: () -> Unit = {},
    startDestination: String = Route.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            HomeScreen(
                onScanLotClick = {
                    navController.navigate(OrderManagementRoutes.ScanReception)
                },
                onDispatchFleetClick = {
                    navController.navigate(Route.Fleet.route) {
                        popUpTo(Route.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSensorsClick = {
                    navController.navigate(
                        com.rebootech.fruitlogix.distributor.infrastructureIot.InfrastructureIotRoutes.SensorsAlerts
                    )
                },
                onReviewReeferClick = { unitId ->
                    navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId))
                },
                onFleetUnitClick = { unitId ->
                    navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId))
                },
                onProfileClick = {
                    navController.navigate(
                        com.rebootech.fruitlogix.distributor.profilesManagement.ProfilesManagementRoutes.Profile
                    )
                }
            )
        }
        composable(Route.Orders.route) {
            OrdersScreen(
                onNewOrderClick = {
                    navController.navigate(OrderManagementRoutes.CreateOrder)
                },
                onEditOrderClick = { orderId ->
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
                onDispatchClick = { unitId ->
                    navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId))
                },
                onArrivalsClick = {
                    navController.navigate(LogisticsMonitoringRoutes.Arrivals)
                },
                onLimitsClick = {
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
        infrastructureIotGraph(navController)
        qualityControlGraph(navController)
        profilesManagementGraph(navController, onSwitchRole = onSwitchRole)
        fleetManagementGraph(navController)
        orderManagementGraph(navController)
        logisticsMonitoringGraph(navController)
        logisticsMonitoringArrivalsGraph(navController)
        paymentManagementGraph(navController)
    }
}