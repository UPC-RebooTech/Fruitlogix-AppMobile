package com.rebootech.fruitlogix.producer.orderManagement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation.ProducerMessagingScreen
import com.rebootech.fruitlogix.producer.orderManagement.presentation.ProducerOrderDetailScreen
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

fun NavGraphBuilder.producerOrderManagementGraph(
    navController: NavHostController
) {
    composable(ProducerOrderManagementRoutes.Inbox) {
        ProducerMessagingScreen()
    }

    // TODO(producer): Pantalla real de Responder Pedido
    composable(
        route = ProducerOrderManagementRoutes.RespondOrder,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
        ProducerPlaceholderScreen(
            title = "Responder Pedido $orderId",
            onBack = { navController.popBackStack() }
        )
    }

    // TODO(producer): Pantalla real de Detalle de Pedido
    composable(
        route = ProducerOrderManagementRoutes.OrderDetail,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId") ?: "1052"
        ProducerOrderDetailScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() },
            onAcceptOrder = {
                navController.popBackStack()
            },
            onRespondOrder = {
                navController.navigate(ProducerOrderManagementRoutes.respondOrder(orderId))
            }
        )
    }

    // TODO(producer): Pantalla real de Historial de Pedidos
    composable(ProducerOrderManagementRoutes.OrderHistory) {
        ProducerPlaceholderScreen(
            title = "Historial de Pedidos",
            onBack = { navController.popBackStack() }
        )
    }
}

@Composable
fun ProducerPlaceholderScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FruitLogixTheme.colors.bg)
            .padding(FruitLogixTheme.spacing.md),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            style = FruitLogixTheme.typography.titleMedium,
            color = FruitLogixTheme.colors.textOnLight
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Pantalla en construcción // TODO(producer)",
            style = FruitLogixTheme.typography.bodySmall,
            color = FruitLogixTheme.colors.textMuted
        )
        Spacer(modifier = Modifier.height(16.dp))
        SecondaryButton(
            text = "Volver",
            onClick = onBack
        )
    }
}
