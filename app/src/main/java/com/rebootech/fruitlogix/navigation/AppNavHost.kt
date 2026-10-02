package com.rebootech.fruitlogix.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.dashboard.presentation.HomeScreen
import com.rebootech.fruitlogix.logistics.presentation.FleetScreen
import com.rebootech.fruitlogix.orders.presentation.OrdersScreen
import com.rebootech.fruitlogix.profiles.presentation.MoreScreen
import com.rebootech.fruitlogix.profiles.presentation.ProducersScreen

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
        composable(Route.Producers.route) {
            ProducersScreen()
        }
        composable(Route.More.route) {
            MoreScreen()
        }
    }
}
