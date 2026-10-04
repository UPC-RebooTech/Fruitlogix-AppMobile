package com.rebootech.fruitlogix.profilesManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.profilesManagement.presentation.ProducersScreen
import com.rebootech.fruitlogix.profilesManagement.presentation.ProfileScreen

fun NavGraphBuilder.profilesManagementGraph(navController: NavHostController) {
    composable(ProfilesManagementRoutes.Producers) {
        ProducersScreen()
    }
    composable(ProfilesManagementRoutes.Profile) {
        ProfileScreen()
    }
}
