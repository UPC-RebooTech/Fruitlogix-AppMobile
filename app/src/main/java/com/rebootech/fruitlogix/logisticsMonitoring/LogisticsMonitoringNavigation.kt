package com.rebootech.fruitlogix.logisticsMonitoring

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.logisticsMonitoring.presentation.DispatchDetailScreen

object LogisticsMonitoringRoutes {
    const val DispatchDetail = "dispatch_detail/{unitId}"
    fun dispatchDetail(unitId: String) = "dispatch_detail/$unitId"
}

fun NavGraphBuilder.logisticsMonitoringGraph(navController: NavHostController) {
    composable(
        route = LogisticsMonitoringRoutes.DispatchDetail,
        arguments = listOf(
            navArgument("unitId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val unitId = backStackEntry.arguments?.getString("unitId") ?: "FL-408"
        DispatchDetailScreen(
            unitId = unitId,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}
