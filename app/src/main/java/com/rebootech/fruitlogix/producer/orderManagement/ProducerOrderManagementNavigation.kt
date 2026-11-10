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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation.ProducerChatScreen
import com.rebootech.fruitlogix.producer.orderManagement.messaging.presentation.ProducerMessagingScreen
import com.rebootech.fruitlogix.producer.orderManagement.presentation.ProducerOrderDetailScreen
import com.rebootech.fruitlogix.producer.orderManagement.presentation.ProducerOrdersScreen
import com.rebootech.fruitlogix.producer.orderManagement.presentation.ProducerOrdersViewModel
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

fun NavGraphBuilder.producerOrderManagementGraph(
    navController: NavHostController
) {
    composable(ProducerOrderManagementRoutes.Inbox) {
        ProducerMessagingScreen()
    }

    // Detalle de Pedido
    composable(
        route = ProducerOrderManagementRoutes.OrderDetail,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId")?.removePrefix("#").orEmpty()
        val ordersViewModel: ProducerOrdersViewModel = viewModel()

        ProducerOrderDetailScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() },
            onAcceptOrder = {
                ordersViewModel.onAcceptOrderById(orderId)
                navController.popBackStack()
            },
            onOpenChat = { targetOrderId ->
                navController.navigate(ProducerOrderManagementRoutes.chat(targetOrderId))
            }
        )
    }

    // Chat con el distribuidor (US23)
    composable(
        route = ProducerOrderManagementRoutes.Chat,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
    ) { backStackEntry ->
        val orderId = backStackEntry.arguments?.getString("orderId")?.removePrefix("#").orEmpty()
        ProducerChatScreen(
            orderId = orderId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Inbox de Mensajería
    composable(ProducerOrderManagementRoutes.Inbox) {
        ProducerMessagingScreen(
            onOpenChat = { orderId ->
                navController.navigate(ProducerOrderManagementRoutes.chat(orderId))
            }
        )
    }

    // Historial de Órdenes
    composable(route = ProducerOrderManagementRoutes.OrderHistory) {
        ProducerOrdersScreen()
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