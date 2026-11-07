package com.rebootech.fruitlogix.producer.logisticsMonitoring

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.producer.orderManagement.ProducerPlaceholderScreen

fun NavGraphBuilder.producerLogisticsMonitoringGraph(
    navController: NavHostController
) {
    // TODO(producer): Cargar en Camión
    composable(
        route = ProducerLogisticsMonitoringRoutes.LoadTruck,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Cargar Pedido  en Camión",
            onBack = { navController.popBackStack() }
        )
    }
}