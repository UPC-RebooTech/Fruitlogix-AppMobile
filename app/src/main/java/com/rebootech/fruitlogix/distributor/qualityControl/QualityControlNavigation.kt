package com.rebootech.fruitlogix.distributor.qualityControl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.qualityControl.presentation.QualityControlScreen

fun NavGraphBuilder.qualityControlGraph(navController: NavHostController) {
    composable(QualityControlRoutes.QualityControl) {
        QualityControlScreen(onBack = { navController.popBackStack() })
    }
}
