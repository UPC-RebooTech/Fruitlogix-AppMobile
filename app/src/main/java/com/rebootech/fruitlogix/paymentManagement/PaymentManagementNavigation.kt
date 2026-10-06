package com.rebootech.fruitlogix.paymentManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.paymentManagement.presentation.InvoiceDetailScreen

fun NavGraphBuilder.paymentManagementGraph(
    navController: NavHostController
) {
    composable(
        route = PaymentManagementRoutes.InvoiceDetail
    ) { backStackEntry ->

        val invoiceId =
            backStackEntry.arguments
                ?.getString("invoiceId")
                .orEmpty()

        InvoiceDetailScreen(
            invoiceId = invoiceId,
            onBackClick = {
                navController.popBackStack()
            }
        )
    }
}