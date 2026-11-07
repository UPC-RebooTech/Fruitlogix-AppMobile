package com.rebootech.fruitlogix.producer.profilesManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.ProducerProfileScreen

fun NavGraphBuilder.producerProfilesManagementGraph(
    navController: NavHostController,
    onSwitchRole: () -> Unit
) {
    composable(ProducerProfilesManagementRoutes.Profile) {
        ProducerProfileScreen(onSwitchRole = onSwitchRole)
    }
}

