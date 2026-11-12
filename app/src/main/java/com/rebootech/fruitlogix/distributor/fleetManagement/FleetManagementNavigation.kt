package com.rebootech.fruitlogix.distributor.fleetManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.fleetManagement.presentation.DriversVehiclesScreen

fun NavGraphBuilder.fleetManagementGraph(navController: NavHostController) {
    composable(FleetManagementRoutes.FleetResources) {
        DriversVehiclesScreen(onBack = { navController.popBackStack() })
    }
}
