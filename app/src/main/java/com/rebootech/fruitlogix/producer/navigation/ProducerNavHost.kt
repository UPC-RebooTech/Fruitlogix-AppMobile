package com.rebootech.fruitlogix.producer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.dashboard.presentation.ProducerHomeScreen
import com.rebootech.fruitlogix.producer.infrastructureIot.producerInfrastructureIotGraph
import com.rebootech.fruitlogix.producer.logisticsMonitoring.producerLogisticsMonitoringGraph
import com.rebootech.fruitlogix.producer.orderManagement.presentation.ProducerOrdersScreen
import com.rebootech.fruitlogix.producer.orderManagement.producerOrderManagementGraph
import com.rebootech.fruitlogix.producer.paymentManagement.producerPaymentManagementGraph
import com.rebootech.fruitlogix.producer.profilesManagement.producerProfilesManagementGraph
import com.rebootech.fruitlogix.producer.qualityControl.ProducerQualityControlRoutes
import com.rebootech.fruitlogix.producer.qualityControl.presentation.ProducerQualityScreen
import com.rebootech.fruitlogix.producer.qualityControl.producerQualityControlGraph
import com.rebootech.fruitlogix.producer.qualityControl.stock.presentation.ProducerStockScreen

@Composable
fun ProducerNavHost(
    navController: NavHostController,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = ProducerRoute.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ProducerRoute.Home.route) {
            ProducerHomeScreen(
                onSwitchRole = onSwitchRole,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(ProducerRoute.Orders.route) {
            ProducerOrdersScreen(onNavigate = { route -> navController.navigate(route) })
        }
        composable(ProducerRoute.Stock.route) {
            ProducerStockScreen(
                onNavigateToDetail = { productId ->
                    navController.navigate(route = ProducerQualityControlRoutes.stockDetail(productId))
                },
                onNavigateToRegisterHarvest = {
                    navController.navigate(route = ProducerQualityControlRoutes.RegisterHarvest)
                },
                onNavigateToHarvestsTab = {
                    navController.navigate(route = ProducerQualityControlRoutes.Harvests)
                }
            )
        }
        composable(ProducerRoute.Reports.route) {
            ProducerQualityScreen()
        }

        // Bounded context subgraphs
        producerOrderManagementGraph(navController)
        producerQualityControlGraph(navController)
        producerLogisticsMonitoringGraph(navController)
        producerInfrastructureIotGraph(navController)
        producerPaymentManagementGraph(navController)
        producerProfilesManagementGraph(navController, onSwitchRole = onSwitchRole)
    }
}
