package com.rebootech.fruitlogix.qualityControl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.qualityControl.presentation.QualityControlScreen

fun NavGraphBuilder.qualityControlGraph(navController: NavHostController) {
    composable(QualityControlRoutes.QualityControl) {
        QualityControlScreen()
    }
}
