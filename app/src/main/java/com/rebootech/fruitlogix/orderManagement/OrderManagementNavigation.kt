package com.rebootech.fruitlogix.orderManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.orderManagement.presentation.CreateOrderScreen
import com.rebootech.fruitlogix.orderManagement.presentation.EditOrderScreen

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

    composable(OrderManagementRoutes.EditOrder) {
        EditOrderScreen(
            onBackClick = {
                navController.popBackStack()
            },
            onOrderUpdated = {
                navController.popBackStack()
            }
        )
    }

}