package com.rebootech.fruitlogix.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.auth.AuthRoutes
import com.rebootech.fruitlogix.auth.authGraph
import com.rebootech.fruitlogix.auth.data.DataStoreSessionRepository
import com.rebootech.fruitlogix.auth.domain.model.Session
import com.rebootech.fruitlogix.auth.domain.model.UserRole
import com.rebootech.fruitlogix.auth.domain.repository.SessionRepository
import com.rebootech.fruitlogix.distributor.DistributorShell
import com.rebootech.fruitlogix.producer.ProducerShell
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val session: Session) : SessionState
}

object RootRoutes {
    const val Auth = "auth"
    const val Distributor = "distributor"
    const val Producer = "producer"
}

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionRepository: SessionRepository? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repo = sessionRepository ?: DataStoreSessionRepository.getInstance(context)
    val session by repo.session.collectAsState()
    val isInitialized = if (repo is DataStoreSessionRepository) {
        val init by repo.isInitialized.collectAsState()
        init
    } else {
        true
    }

    val sessionState: SessionState = when {
        !isInitialized -> SessionState.Loading
        session != null -> SessionState.SignedIn(session!!)
        else -> SessionState.SignedOut
    }

    when (val state = sessionState) {
        SessionState.Loading -> {
            // Mientras carga, muestra solo el fondo ColorSurfaceDark con un indicador de progreso
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(FruitLogixTheme.colors.surfaceDark),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = FruitLogixTheme.colors.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        SessionState.SignedOut -> {
            NavHost(
                navController = navController,
                startDestination = AuthRoutes.Login,
                modifier = modifier
            ) {
                authGraph(navController)
            }
        }

        is SessionState.SignedIn -> {
            when (state.session.role) {
                UserRole.DISTRIBUTOR -> {
                    DistributorShell(
                        onSignOut = {
                            coroutineScope.launch {
                                repo.signOut()
                            }
                        }
                    )
                }

                UserRole.PRODUCER -> {
                    ProducerShell(
                        onSignOut = {
                            coroutineScope.launch {
                                repo.signOut()
                            }
                        }
                    )
                }
            }
        }
    }
}
