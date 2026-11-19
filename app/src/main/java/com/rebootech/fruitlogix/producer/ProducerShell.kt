package com.rebootech.fruitlogix.producer

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
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
import com.rebootech.fruitlogix.app.navigation.BottomNavBar
import com.rebootech.fruitlogix.producer.infrastructureIot.ProducerInfrastructureIotRoutes
import com.rebootech.fruitlogix.producer.infrastructureIot.data.FakeIotAlertRepository
import com.rebootech.fruitlogix.producer.navigation.ProducerNavHost
import com.rebootech.fruitlogix.producer.navigation.producerMoreItems
import com.rebootech.fruitlogix.producer.navigation.producerNavTabs
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.orderManagement.messaging.data.FakeChatRepository
import com.rebootech.fruitlogix.producer.profilesManagement.ProducerProfilesManagementRoutes
import com.rebootech.fruitlogix.producer.profilesManagement.data.FakeProfileRepository
import com.rebootech.fruitlogix.shared.ui.format.initialsOf
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.shared.ui.components.SyncState
import com.rebootech.fruitlogix.shared.ui.components.SyncStatusStrip
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun ProducerShell(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = LocalActivity.current
    // Read once per composition of the activity; changing the language recreates the activity.
    val selectedLanguage = remember { AppLocale.current(context) }
    val profile by FakeProfileRepository.instance.profile.collectAsState()
    val alerts by FakeIotAlertRepository.instance.alerts.collectAsState()
    val unreadByOrder by FakeChatRepository.instance.unreadByOrder.collectAsState()

    // TODO(producer): connect to the real sync/connectivity source. The strip only appears
    // when the state is not SYNCED (offline, pending changes, syncing or conflict).
    val syncState = SyncState.SYNCED
    val pendingChanges = 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FruitLogixTheme.colors.appbar)
                    .statusBarsPadding()
            ) {
                FruitLogixTopBar(
                    title = stringResource(R.string.topbar_title),
                    subtitle = stringResource(R.string.producer_topbar_subtitle),
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { language ->
                        activity?.let { AppLocale.apply(it, language) }
                    },
                    onNotificationClick = {
                        navController.navigate(ProducerInfrastructureIotRoutes.Notifications)
                    },
                    onProfileClick = {
                        navController.navigate(ProducerProfilesManagementRoutes.Profile)
                    },
                    hasUnreadNotifications = alerts.any { !it.isRead },
                    showLanguageToggle = true,
                    compactLanguageToggle = true,
                    unreadMessages = unreadByOrder.values.sum(),
                    onMessagesClick = {
                        navController.navigate(ProducerOrderManagementRoutes.Inbox)
                    },
                    userInitials = initialsOf(profile.fullName)
                )

                SyncStatusStrip(
                    state = syncState,
                    pendingChanges = pendingChanges
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                tabs = producerNavTabs,
                moreItems = producerMoreItems
            )
        }
    ) { innerPadding ->
        ProducerNavHost(
            navController = navController,
            onSignOut = onSignOut,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
