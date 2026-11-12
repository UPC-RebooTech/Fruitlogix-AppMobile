package com.rebootech.fruitlogix.distributor.logisticsMonitoring

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.DispatchDetailScreen

object LogisticsMonitoringRoutes {
    /** Arrivals are a tab of the Fleet screen now, not a separate route. */
    const val DispatchDetail = "dispatch/{id}"
    fun dispatchDetail(unitId: String) = "dispatch/$unitId"
}

fun NavGraphBuilder.logisticsMonitoringGraph(navController: NavHostController) {
    composable(
        route = LogisticsMonitoringRoutes.DispatchDetail,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) {
        // DispatchDetailViewModel reads "id" from SavedStateHandle
        DispatchDetailScreen(onBackClick = { navController.popBackStack() })
    }
}
