package com.rebootech.fruitlogix.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.app.dashboard.presentation.HomeScreen
import com.rebootech.fruitlogix.fleetManagement.fleetManagementGraph
import com.rebootech.fruitlogix.infrastructureIot.infrastructureIotGraph
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.FleetScreen
import com.rebootech.fruitlogix.orderManagement.presentation.OrdersScreen
import com.rebootech.fruitlogix.paymentManagement.presentation.InvoicesScreen
import com.rebootech.fruitlogix.profilesManagement.profilesManagementGraph
import com.rebootech.fruitlogix.qualityControl.qualityControlGraph
import com.rebootech.fruitlogix.orderManagement.OrderManagementRoutes
import com.rebootech.fruitlogix.orderManagement.orderManagementGraph

import com.rebootech.fruitlogix.logisticsMonitoring.LogisticsMonitoringRoutes
import com.rebootech.fruitlogix.logisticsMonitoring.logisticsMonitoringGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Route.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Route.Home.route) {
            HomeScreen()
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
                }
            )
        }
        composable(Route.Fleet.route) {
            FleetScreen(
                onDispatchClick = { unitId ->
                    navController.navigate(LogisticsMonitoringRoutes.dispatchDetail(unitId))
                }
            )
        }
        composable(Route.Invoices.route) {
            InvoicesScreen()
        }

        // Bounded context nav graphs
        infrastructureIotGraph(navController)
        qualityControlGraph(navController)
        profilesManagementGraph(navController)
        fleetManagementGraph(navController)
        orderManagementGraph(navController)
        logisticsMonitoringGraph(navController)
    }
}
