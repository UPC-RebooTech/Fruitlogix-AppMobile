package com.rebootech.fruitlogix.distributor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.app.navigation.AppNavHost
import com.rebootech.fruitlogix.app.navigation.BottomNavBar

@Composable
fun DistributorShell(
    onSwitchRole: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            onSwitchRole = onSwitchRole, // <--- Conectamos la acción hacia abajo
            modifier = Modifier.padding(innerPadding)
        )
    }
}