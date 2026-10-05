package com.rebootech.fruitlogix.fleetManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.fleetManagement.presentation.FleetResourcesScreen

fun NavGraphBuilder.fleetManagementGraph(navController: NavHostController) {
    composable(FleetManagementRoutes.FleetResources) {
        FleetResourcesScreen()
    }
}
