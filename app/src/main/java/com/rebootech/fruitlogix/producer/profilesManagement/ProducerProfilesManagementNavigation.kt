package com.rebootech.fruitlogix.producer.profilesManagement

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.ProducerProfileScreen
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.edit.EditProfileScreen
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings.HelpSupportScreen
import com.rebootech.fruitlogix.shared.ui.settings.LanguageSettingsScreen
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings.NotificationSettingsScreen
import com.rebootech.fruitlogix.producer.profilesManagement.presentation.settings.SyncSettingsScreen

fun NavGraphBuilder.producerProfilesManagementGraph(
    navController: NavHostController,
    onSignOut: () -> Unit
) {
    val back: () -> Unit = { navController.popBackStack() }

    composable(ProducerProfilesManagementRoutes.Profile) {
        ProducerProfileScreen(
            onSignOut = onSignOut,
            onEditProfile = { navController.navigate(ProducerProfilesManagementRoutes.EditProfile) },
            onOpenNotifications = { navController.navigate(ProducerProfilesManagementRoutes.SettingsNotifications) },
            onOpenLanguage = { navController.navigate(ProducerProfilesManagementRoutes.SettingsLanguage) },
            onOpenSync = { navController.navigate(ProducerProfilesManagementRoutes.SettingsSync) },
            onOpenHelp = { navController.navigate(ProducerProfilesManagementRoutes.Help) }
        )
    }
    composable(ProducerProfilesManagementRoutes.EditProfile) {
        EditProfileScreen(onNavigateBack = back)
    }
    composable(ProducerProfilesManagementRoutes.SettingsNotifications) {
        NotificationSettingsScreen(onNavigateBack = back)
    }
    composable(ProducerProfilesManagementRoutes.SettingsLanguage) {
        LanguageSettingsScreen(onNavigateBack = back)
    }
    composable(ProducerProfilesManagementRoutes.SettingsSync) {
        SyncSettingsScreen(onNavigateBack = back)
    }
    composable(ProducerProfilesManagementRoutes.Help) {
        HelpSupportScreen(
            onNavigateBack = back,
            onOpenMessages = { navController.navigate(ProducerOrderManagementRoutes.Inbox) }
        )
    }
}
