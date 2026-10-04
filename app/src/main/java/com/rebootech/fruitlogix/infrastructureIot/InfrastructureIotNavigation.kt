package com.rebootech.fruitlogix.infrastructureIot

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.infrastructureIot.presentation.SensorsAlertsScreen

fun NavGraphBuilder.infrastructureIotGraph(navController: NavHostController) {
    composable(InfrastructureIotRoutes.SensorsAlerts) {
        SensorsAlertsScreen()
    }
}
