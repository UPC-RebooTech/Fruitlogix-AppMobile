package com.rebootech.fruitlogix.producer.infrastructureIot

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.infrastructureIot.presentation.ProducerNotificationsScreen

fun NavGraphBuilder.producerInfrastructureIotGraph(
    navController: NavHostController
) {
    composable(ProducerInfrastructureIotRoutes.Notifications) {
        ProducerNotificationsScreen(
            onNavigate = { route ->
                navController.navigate(route)
            }
        )
    }
}

