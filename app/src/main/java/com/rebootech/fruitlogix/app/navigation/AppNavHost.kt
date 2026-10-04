package com.rebootech.fruitlogix.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.paymentManagement.presentation.InvoicesScreen
import com.rebootech.fruitlogix.app.dashboard.presentation.HomeScreen
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.FleetScreen
import com.rebootech.fruitlogix.orderManagement.presentation.OrdersScreen
import com.rebootech.fruitlogix.profilesManagement.presentation.MoreScreen

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
            OrdersScreen()
        }
        composable(Route.Fleet.route) {
            FleetScreen()
        }
        composable(Route.Invoices.route) {
            InvoicesScreen()
        }
        composable(Route.More.route) {
            MoreScreen()
        }
    }
}
