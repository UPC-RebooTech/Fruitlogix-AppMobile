package com.rebootech.fruitlogix.producer.qualityControl

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.producer.orderManagement.ProducerPlaceholderScreen
import com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation.ProducerHarvestsScreen
import com.rebootech.fruitlogix.producer.qualityControl.stock.presentation.ProducerStockScreen
import com.rebootech.fruitlogix.producer.qualityControl.stock.presentation.ProducerStockDetailScreen
import com.rebootech.fruitlogix.producer.qualityControl.harvests.presentation.ProducerRegisterHarvestScreen
import com.rebootech.fruitlogix.producer.qualityControl.reports.presentation.ProducerReportDetailScreen
import com.rebootech.fruitlogix.producer.qualityControl.reports.presentation.ProducerQualityScreen

fun NavGraphBuilder.producerQualityControlGraph(
    navController: NavHostController
) {
    // 1. Pantalla principal de Stock (Inventario)
    composable(route = "quality_stock_main") {
        ProducerStockScreen(
            onNavigateToDetail = { productId ->
                navController.navigate(ProducerQualityControlRoutes.stockDetail(productId))
            },
            onNavigateToRegisterHarvest = {
                navController.navigate(ProducerQualityControlRoutes.RegisterHarvest)
            },
            onNavigateToHarvestsTab = {
                navController.navigate(ProducerQualityControlRoutes.Harvests)
            }
        )
    }

    // 2. Pantalla principal de Reportes de Calidad (Lista)
    composable(route = ProducerQualityControlRoutes.Reports) {
        ProducerQualityScreen(
            onNavigateToDetail = { reportId ->
                navController.navigate(ProducerQualityControlRoutes.reportDetail(reportId))
            }
        )
    }

    // 3. Pantalla de Cosechas (Pestaña Harvests)
    composable(route = ProducerQualityControlRoutes.Harvests) {
        ProducerHarvestsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRegisterHarvest = {
                navController.navigate(ProducerQualityControlRoutes.RegisterHarvest)
            }
        )
    }

    // 4. Registrar Cosecha
    composable(route = ProducerQualityControlRoutes.RegisterHarvest) {
        ProducerRegisterHarvestScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 5. Detalle de Stock por Producto
    composable(
        route = ProducerQualityControlRoutes.StockDetail,
        arguments = listOf(navArgument("productId") { type = NavType.StringType })
    ) { backStackEntry ->
        val productId = backStackEntry.arguments?.getString("productId") ?: ""
        ProducerStockDetailScreen(
            lotId = productId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 6. Detalle del Reporte de Calidad
    composable(
        route = ProducerQualityControlRoutes.ReportDetail,
        arguments = listOf(navArgument("reportId") { type = NavType.StringType })
    ) { backStackEntry ->
        val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
        ProducerReportDetailScreen(
            reportId = reportId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 7. Demás rutas de calidad / pedidos
    composable(
        route = ProducerQualityControlRoutes.PrepareOrder,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Preparar Pedido $orderId",
            onBack = { navController.popBackStack() }
        )
    }

    composable(route = ProducerQualityControlRoutes.ScanLotAny) {
        ProducerPlaceholderScreen(
            title = "Escanear Lote",
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = ProducerQualityControlRoutes.ScanLot,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Escanear Lote para $orderId",
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = ProducerQualityControlRoutes.Inspection,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Inspección de Calidad $orderId",
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = ProducerQualityControlRoutes.ReviewResult,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Resultado de Calidad $orderId",
            onBack = { navController.popBackStack() }
        )
    }
}