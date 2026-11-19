package com.rebootech.fruitlogix.producer.logisticsMonitoring

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation.ProducerLoadTruckScreen
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes

fun NavGraphBuilder.producerLogisticsMonitoringGraph(
    navController: NavHostController
) {
    composable(
        route = ProducerLogisticsMonitoringRoutes.LoadTruck,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) {
        // LoadTruckViewModel reads "orderId" from SavedStateHandle
        ProducerLoadTruckScreen(
            onNavigateBack = { navController.popBackStack() },
            // Back to where it was opened (orders list, order detail or home), now showing "in transit"
            onDone = { navController.popBackStack() },
            onOpenChat = { orderId -> navController.navigate(ProducerOrderManagementRoutes.chat(orderId)) }
        )
    }
}
