package com.rebootech.fruitlogix.distributor

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.app.navigation.AppNavHost
import com.rebootech.fruitlogix.app.navigation.BottomNavBar
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.distributor.infrastructureIot.InfrastructureIotRoutes
import com.rebootech.fruitlogix.distributor.profilesManagement.ProfilesManagementRoutes
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Distributor frame: one top bar for every screen (language, alerts, profile), like the
 * producer, plus the bottom navigation.
 */
@Composable
fun DistributorShell(
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {}
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = LocalActivity.current
    // Read once; changing the language recreates the activity.
    val selectedLanguage = remember { AppLocale.current(context) }
    val session by DataStoreSessionRepository.getInstance(context).session.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FruitLogixTopBar(
                title = stringResource(R.string.topbar_title),
                subtitle = stringResource(R.string.topbar_subtitle),
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { language -> activity?.let { AppLocale.apply(it, language) } },
                onNotificationClick = { navController.navigate(InfrastructureIotRoutes.SensorsAlerts) },
                onProfileClick = { navController.navigate(ProfilesManagementRoutes.Profile) },
                compactLanguageToggle = true,
                userInitials = initialsOf(session?.displayName.orEmpty()).ifBlank { "FL" },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FruitLogixTheme.colors.appbar)
                    .statusBarsPadding()
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            onSignOut = onSignOut,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
