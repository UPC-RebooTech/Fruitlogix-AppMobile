package com.rebootech.fruitlogix.logisticsMonitoring

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.infrastructureIot.InfrastructureIotRoutes
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.ArrivalsScreen
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.DispatchDetailScreen

object LogisticsMonitoringRoutes {
    const val DispatchDetail = "dispatch/{id}"
    fun dispatchDetail(unitId: String) = "dispatch/$unitId"
    const val Arrivals = "arrivals"
}

fun NavGraphBuilder.logisticsMonitoringGraph(navController: NavHostController) {
    composable(
        route = LogisticsMonitoringRoutes.DispatchDetail,
        arguments = listOf(
            navArgument("id") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val unitId = backStackEntry.arguments?.getString("id") ?: "FL-408"
        DispatchDetailScreen(
            unitId = unitId,
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToAlertDetail = {
                navController.navigate(InfrastructureIotRoutes.SensorsAlerts)
            }
        )
    }

    // Secondary route alias for dispatch_detail/{unitId} compatibility
    composable(
        route = "dispatch_detail/{unitId}",
        arguments = listOf(
            navArgument("unitId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val unitId = backStackEntry.arguments?.getString("unitId") ?: "FL-408"
        DispatchDetailScreen(
            unitId = unitId,
            onBackClick = {
                navController.popBackStack()
            },
            onNavigateToAlertDetail = {
                navController.navigate(InfrastructureIotRoutes.SensorsAlerts)
            }
        )
    }
}

fun NavGraphBuilder.logisticsMonitoringArrivalsGraph(navController: NavHostController) {
    composable(route = LogisticsMonitoringRoutes.Arrivals) {
        ArrivalsScreen(
            onBackClick = { navController.popBackStack() },
            onNavigateToReception = { route ->
                navController.navigate(route)
            }
        )
    }
}
