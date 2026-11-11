package com.rebootech.fruitlogix.distributor.profilesManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.ProducersScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.ProfileScreen

fun NavGraphBuilder.profilesManagementGraph(
    navController: NavHostController,
    onSignOut: () -> Unit = {}
) {
    composable(ProfilesManagementRoutes.Producers) {
        ProducersScreen()
    }
    composable(ProfilesManagementRoutes.Profile) {
        ProfileScreen(
            onSignOut = onSignOut
        )
    }
}