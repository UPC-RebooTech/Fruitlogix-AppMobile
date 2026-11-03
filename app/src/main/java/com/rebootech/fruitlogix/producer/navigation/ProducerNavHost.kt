package com.rebootech.fruitlogix.producer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.harvests.presentation.ProducerHarvestsScreen
import com.rebootech.fruitlogix.producer.home.presentation.ProducerHomeScreen
import com.rebootech.fruitlogix.producer.messaging.presentation.ProducerMessagingScreen
import com.rebootech.fruitlogix.producer.notificationsProfile.presentation.ProducerProfileScreen
import com.rebootech.fruitlogix.producer.orderFulfillment.presentation.ProducerOrdersScreen
import com.rebootech.fruitlogix.producer.payouts.presentation.ProducerPayoutsScreen
import com.rebootech.fruitlogix.producer.qualityInspection.presentation.ProducerQualityScreen
import com.rebootech.fruitlogix.producer.stockManagement.presentation.ProducerStockScreen

@Composable
fun ProducerNavHost(
    navController: NavHostController,
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = ProducerRoutes.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(ProducerRoutes.Home) {
            ProducerHomeScreen(onSwitchRole = onSwitchRole)
        }
        composable(ProducerRoutes.Orders) {
            ProducerOrdersScreen()
        }
        composable(ProducerRoutes.Stock) {
            ProducerStockScreen()
        }
        composable(ProducerRoutes.Reports) {
            ProducerQualityScreen()
        }
        composable(ProducerRoutes.Harvests) {
            ProducerHarvestsScreen()
        }
        composable(ProducerRoutes.Payouts) {
            ProducerPayoutsScreen()
        }
        composable(ProducerRoutes.Messaging) {
            ProducerMessagingScreen()
        }
        composable(ProducerRoutes.Profile) {
            ProducerProfileScreen(onSwitchRole = onSwitchRole)
        }
    }
}

