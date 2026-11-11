package com.rebootech.fruitlogix.producer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.app.navigation.BottomNavBar
import com.rebootech.fruitlogix.producer.infrastructureIot.ProducerInfrastructureIotRoutes
import com.rebootech.fruitlogix.producer.navigation.ProducerNavHost
import com.rebootech.fruitlogix.producer.navigation.ProducerRoute
import com.rebootech.fruitlogix.producer.navigation.producerMoreItems
import com.rebootech.fruitlogix.producer.navigation.producerNavTabs
import com.rebootech.fruitlogix.producer.orderManagement.ProducerOrderManagementRoutes
import com.rebootech.fruitlogix.producer.profilesManagement.ProducerProfilesManagementRoutes
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixTopBar
import com.rebootech.fruitlogix.shared.ui.components.SyncChip
import com.rebootech.fruitlogix.shared.ui.components.SyncState
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

private val primaryTabRoutes = setOf(
    ProducerRoute.Home.route,
    ProducerRoute.Orders.route,
    ProducerRoute.Stock.route,
    ProducerRoute.Reports.route
)

@Composable
fun ProducerShell(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    var selectedLanguage by remember { mutableStateOf(AppLanguage.ES) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isPrimaryTab = currentDestination?.hierarchy?.any { it.route in primaryTabRoutes } == true

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
                    onLanguageSelected = { selectedLanguage = it },
                    onNotificationClick = {
                        navController.navigate(ProducerInfrastructureIotRoutes.Notifications)
                    },
                    onProfileClick = {
                        navController.navigate(ProducerProfilesManagementRoutes.Profile)
                    },
                    showLanguageToggle = false,
                    unreadMessages = 3, // TODO(producer): vincular con conteo reactivo de mensajes no leídos
                    onMessagesClick = {
                        navController.navigate(ProducerOrderManagementRoutes.Inbox)
                    },
                    userInitials = "AJ"
                )

                if (isPrimaryTab) {
                    ProducerStatusRow(
                        syncState = SyncState.SYNCED,
                        onSyncClick = {
                            // TODO(producer): Navegar a sync_center cuando exista
                        }
                    )
                }
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

@Composable
fun ProducerStatusRow(
    syncState: SyncState,
    onSyncClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FruitLogixTheme.colors.surfaceDark)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SyncChip(
            state = syncState,
            onClick = onSyncClick
        )

        Box(
            modifier = Modifier
                .clip(FruitLogixTheme.shapes.Pill)
                .background(FruitLogixTheme.colors.appbar)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ROLE: PRODUCER",
                style = FruitLogixTheme.typography.labelSmall,
                color = FruitLogixTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
