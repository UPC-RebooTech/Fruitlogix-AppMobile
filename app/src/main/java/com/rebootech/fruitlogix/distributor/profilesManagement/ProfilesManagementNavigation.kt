package com.rebootech.fruitlogix.distributor.profilesManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.ProducersScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.ProfileScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.edit.EditProfileScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.settings.HelpSupportScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.settings.NotificationSettingsScreen
import com.rebootech.fruitlogix.distributor.profilesManagement.presentation.settings.SyncSettingsScreen
import com.rebootech.fruitlogix.shared.ui.settings.LanguageSettingsScreen

fun NavGraphBuilder.profilesManagementGraph(
    navController: NavHostController,
    onSignOut: () -> Unit = {}
) {
    val back: () -> Unit = { navController.popBackStack() }

    composable(ProfilesManagementRoutes.Producers) {
        ProducersScreen(onBack = back)
    }
    composable(ProfilesManagementRoutes.Profile) {
        ProfileScreen(
            onBack = back,
            onSignOut = onSignOut,
            onEditProfile = { navController.navigate(ProfilesManagementRoutes.EditProfile) },
            onOpenNotifications = { navController.navigate(ProfilesManagementRoutes.SettingsNotifications) },
            onOpenLanguage = { navController.navigate(ProfilesManagementRoutes.SettingsLanguage) },
            onOpenSync = { navController.navigate(ProfilesManagementRoutes.SettingsSync) },
            onOpenHelp = { navController.navigate(ProfilesManagementRoutes.Help) }
        )
    }
    composable(ProfilesManagementRoutes.EditProfile) {
        EditProfileScreen(onNavigateBack = back)
    }
    composable(ProfilesManagementRoutes.SettingsNotifications) {
        NotificationSettingsScreen(onNavigateBack = back)
    }
    composable(ProfilesManagementRoutes.SettingsLanguage) {
        LanguageSettingsScreen(onNavigateBack = back)
    }
    composable(ProfilesManagementRoutes.SettingsSync) {
        SyncSettingsScreen(onNavigateBack = back)
    }
    composable(ProfilesManagementRoutes.Help) {
        HelpSupportScreen(
            onNavigateBack = back,
            onOpenProducers = { navController.navigate(ProfilesManagementRoutes.Producers) }
        )
    }
}
