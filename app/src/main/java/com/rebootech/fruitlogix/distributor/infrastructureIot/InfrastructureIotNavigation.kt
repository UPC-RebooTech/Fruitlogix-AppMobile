package com.rebootech.fruitlogix.distributor.infrastructureIot

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.infrastructureIot.presentation.SensorsAlertsScreen

/**
 * @param onOpenUnit opens a truck's trip. Passed in by the app navigation so IoT does not
 * depend on Logistics.
 */
fun NavGraphBuilder.infrastructureIotGraph(
    navController: NavHostController,
    onOpenUnit: (unitId: String) -> Unit
) {
    composable(InfrastructureIotRoutes.SensorsAlerts) {
        SensorsAlertsScreen(
            onBack = { navController.popBackStack() },
            onOpenUnit = onOpenUnit
        )
    }
}
