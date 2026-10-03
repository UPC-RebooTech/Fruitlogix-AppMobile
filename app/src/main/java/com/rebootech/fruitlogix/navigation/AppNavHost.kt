package com.rebootech.fruitlogix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.billing.presentation.InvoicesScreen
import com.rebootech.fruitlogix.dashboard.presentation.HomeScreen
import com.rebootech.fruitlogix.logistics.presentation.FleetScreen
import com.rebootech.fruitlogix.orders.presentation.OrdersScreen
import com.rebootech.fruitlogix.profiles.presentation.MoreScreen

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
