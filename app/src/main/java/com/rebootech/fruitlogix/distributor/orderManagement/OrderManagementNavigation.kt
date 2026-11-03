package com.rebootech.fruitlogix.distributor.orderManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.CreateOrderScreen
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.EditOrderScreen
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.OrderHistoryScreen
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.ScanReceptionScreen

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

    composable(OrderManagementRoutes.OrderHistory) {
        OrderHistoryScreen(
            onBackClick = {
                navController.popBackStack()
            }
        )
    }

    composable(OrderManagementRoutes.ScanReception) {
        ScanReceptionScreen(
            onBackClick = {
                navController.popBackStack()
            }
        )
    }

}