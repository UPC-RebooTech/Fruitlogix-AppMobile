package com.rebootech.fruitlogix.orderManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.orderManagement.presentation.CreateOrderScreen

fun NavGraphBuilder.orderManagementGraph(
    navController: NavHostController
) {
    composable(OrderManagementRoutes.CreateOrder) {
        CreateOrderScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onOrderRegistered = {
                navController.popBackStack()
            }
        )
    }
}