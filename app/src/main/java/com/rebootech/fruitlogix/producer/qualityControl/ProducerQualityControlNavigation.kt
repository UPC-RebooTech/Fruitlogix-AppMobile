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

fun NavGraphBuilder.producerQualityControlGraph(
    navController: NavHostController
) {
    // 1. Pantalla principal de Stock (Inventario)
    // *Asegúrate de agregar const val Stock = "quality_stock_main" en tu objeto ProducerQualityControlRoutes si no la tienes*
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

    // 2. Pantalla de Cosechas (Pestaña Harvests)
    composable(route = ProducerQualityControlRoutes.Harvests) {
        ProducerHarvestsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToRegisterHarvest = {
                navController.navigate(ProducerQualityControlRoutes.RegisterHarvest)
            }
        )
    }

    // 3. Registrar Cosecha
    composable(route = ProducerQualityControlRoutes.RegisterHarvest) {
        ProducerHarvestsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 4. Detalle de Stock por Producto (Usa tu constante y argumento)
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

    // 5. Demás rutas de calidad / pedidos
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
        route = ProducerQualityControlRoutes.ReportDetail,
        arguments = listOf(navArgument("reportId") { type = NavType.StringType })
    ) { backStackEntry ->
        val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
        ProducerPlaceholderScreen(
            title = "Detalle de Reporte $reportId",
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