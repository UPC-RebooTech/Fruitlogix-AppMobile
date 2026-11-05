package com.rebootech.fruitlogix.producer.qualityControl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation.ProducerHarvestsScreen

fun NavGraphBuilder.producerQualityControlGraph(
    navController: NavHostController
) {
    composable(ProducerQualityControlRoutes.Harvests) {
        ProducerHarvestsScreen()
    }
}

