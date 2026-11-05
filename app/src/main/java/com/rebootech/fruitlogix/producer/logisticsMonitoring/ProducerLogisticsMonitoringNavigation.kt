package com.rebootech.fruitlogix.producer.logisticsMonitoring

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.logisticsMonitoring.presentation.ProducerDeliveryScreen

fun NavGraphBuilder.producerLogisticsMonitoringGraph(
    navController: NavHostController
) {
    composable(ProducerLogisticsMonitoringRoutes.LoadTruck) {
        ProducerDeliveryScreen()
    }
}

