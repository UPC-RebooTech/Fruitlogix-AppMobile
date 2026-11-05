package com.rebootech.fruitlogix.producer.paymentManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.paymentManagement.presentation.ProducerPayoutsScreen

fun NavGraphBuilder.producerPaymentManagementGraph(
    navController: NavHostController
) {
    composable(ProducerPaymentManagementRoutes.Payments) {
        ProducerPayoutsScreen()
    }
}

