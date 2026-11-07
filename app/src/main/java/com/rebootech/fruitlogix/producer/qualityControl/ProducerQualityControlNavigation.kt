package com.rebootech.fruitlogix.producer.qualityControl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.producer.orderManagement.ProducerPlaceholderScreen
import com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation.ProducerHarvestsScreen

fun NavGraphBuilder.producerQualityControlGraph(
    navController: NavHostController
) {
    composable(ProducerQualityControlRoutes.Harvests) {
        ProducerHarvestsScreen()
    }

    // TODO(producer): Preparar Pedido
    composable(
        route = ProducerQualityControlRoutes.PrepareOrder,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Preparar Pedido ",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Escáner de Lote (General)
    composable(ProducerQualityControlRoutes.ScanLotAny) {
        ProducerPlaceholderScreen(
            title = "Escanear Lote",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Escáner de Lote por Pedido
    composable(
        route = ProducerQualityControlRoutes.ScanLot,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Escanear Lote para ",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Inspección de Calidad
    composable(
        route = ProducerQualityControlRoutes.Inspection,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Inspección de Calidad ",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Detalle de Reporte de Calidad
    composable(
        route = ProducerQualityControlRoutes.ReportDetail,
        arguments = listOf(navArgument("reportId") { type = NavType.StringType })
    ) { backStackEntry ->
        val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
        ProducerPlaceholderScreen(
            title = "Detalle de Reporte ",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Resultado / Revisión de Reporte
    composable(
        route = ProducerQualityControlRoutes.ReviewResult,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Resultado de Calidad ",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Detalle de Stock de Producto
    composable(
        route = ProducerQualityControlRoutes.StockDetail,
        arguments = listOf(navArgument("productId") { type = NavType.StringType })
    ) { backStackEntry ->
        val productId = backStackEntry.arguments?.getString("productId") ?: ""
        ProducerPlaceholderScreen(
            title = "Detalle de Stock: ",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Registrar Cosecha
    composable(ProducerQualityControlRoutes.RegisterHarvest) {
        ProducerPlaceholderScreen(
            title = "Registrar Cosecha",
            onBack = { navController.popBackStack() }
        )
    }
}