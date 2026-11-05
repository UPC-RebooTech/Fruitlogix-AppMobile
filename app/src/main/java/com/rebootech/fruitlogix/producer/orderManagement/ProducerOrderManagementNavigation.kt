package com.rebootech.fruitlogix.producer.orderManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation.ProducerMessagingScreen

fun NavGraphBuilder.producerOrderManagementGraph(
    navController: NavHostController
) {
    composable(ProducerOrderManagementRoutes.Inbox) {
        ProducerMessagingScreen()
    }
}

