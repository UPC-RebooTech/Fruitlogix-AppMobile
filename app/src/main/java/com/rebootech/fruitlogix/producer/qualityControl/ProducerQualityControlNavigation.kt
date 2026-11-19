package com.rebootech.fruitlogix.producer.qualityControl

import androidx.navigation.NavGraph.Companion.findStartDestination
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
import com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation.ProducerInspectionScreen
import com.rebootech.fruitlogix.producer.qualityControl.review.presentation.ProducerReviewResultScreen
import com.rebootech.fruitlogix.producer.navigation.ProducerRoute
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation.ProducerScanLotScreen

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
            },
            onNavigateToInventory = {
                // Opened from Stock: go back to it. Opened from the More menu: switch to the Stock tab.
                if (!navController.popBackStack(ProducerRoute.Stock.route, inclusive = false)) {
                    navController.navigate(ProducerRoute.Stock.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                    }
                }
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
        ProducerInspectionScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() },
            onReviewReport = {
                navController.navigate(ProducerQualityControlRoutes.reviewResult(orderId))
            }
        )
    }

    // Escaneo desde la barra de búsqueda (sin ID inicial)
    composable(route = ProducerQualityControlRoutes.ScanLotAny) {
        ProducerScanLotScreen(
            orderId = null,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToInspection = { scannedOrderId ->
                navController.navigate(ProducerQualityControlRoutes.inspection(scannedOrderId))
            }
        )
    }

    // Escaneo desde un pedido específico (con orderId)
    composable(
        route = ProducerQualityControlRoutes.ScanLot,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerScanLotScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToInspection = { scannedOrderId ->
                navController.navigate(ProducerQualityControlRoutes.inspection(scannedOrderId))
            }
        )
    }

    composable(
        route = ProducerQualityControlRoutes.Inspection,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerInspectionScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() },
            onReviewReport = {
                // Navega hacia la revisión o resultado de calidad
                navController.navigate(ProducerQualityControlRoutes.reviewResult(orderId))
            }
        )
    }

    composable(
        route = ProducerQualityControlRoutes.ReviewResult,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerReviewResultScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() },
            onEditInspection = { navController.popBackStack() },
            onBackToOrder = {
                // Return to this order's detail if it is in the back stack; otherwise open it
                if (!navController.popBackStack(ProducerOrderManagementRoutes.OrderDetail, inclusive = false)) {
                    navController.navigate(ProducerOrderManagementRoutes.orderDetail(orderId)) {
                        popUpTo(ProducerQualityControlRoutes.ReviewResult) { inclusive = true }
                    }
                }
            },
            onOpenChat = {
                navController.navigate(ProducerOrderManagementRoutes.chat(orderId))
            }
        )
    }
}