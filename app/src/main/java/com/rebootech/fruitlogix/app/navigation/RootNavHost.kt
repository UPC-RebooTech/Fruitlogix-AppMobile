package com.rebootech.fruitlogix.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.auth.data.FakeSessionRepository
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.auth.presentation.SelectRoleScreen
import com.rebootech.fruitlogix.distributor.DistributorShell
import com.rebootech.fruitlogix.producer.ProducerShell
import kotlinx.coroutines.launch

object RootRoutes {
    const val SelectRole = "select_role"
    const val Distributor = "distributor"
    const val Producer = "producer"
}

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionRepository: SessionRepository = FakeSessionRepository.instance
) {
    val coroutineScope = rememberCoroutineScope()
    val currentRole by sessionRepository.currentRole.collectAsState(initial = null)

    val startDestination = when (currentRole) {
        UserRole.DISTRIBUTOR -> RootRoutes.Distributor
        UserRole.PRODUCER -> RootRoutes.Producer
        null -> RootRoutes.SelectRole
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(RootRoutes.SelectRole) {
            SelectRoleScreen(
                onRoleSelected = { role ->
                    val destination = when (role) {
                        UserRole.DISTRIBUTOR -> RootRoutes.Distributor
                        UserRole.PRODUCER -> RootRoutes.Producer
                    }
                    navController.navigate(destination) {
                        popUpTo(RootRoutes.SelectRole) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(RootRoutes.Distributor) {
            DistributorShell(
                onSwitchRole = {
                    coroutineScope.launch {
                        sessionRepository.clearSession()
                        navController.navigate(RootRoutes.SelectRole) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(RootRoutes.Producer) {
            ProducerShell(
                onSwitchRole = {
                    coroutineScope.launch {
                        sessionRepository.clearSession()
                        navController.navigate(RootRoutes.SelectRole) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

