package com.rebootech.fruitlogix.producer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rebootech.fruitlogix.app.navigation.BottomNavBar
import com.rebootech.fruitlogix.producer.navigation.ProducerNavHost
import com.rebootech.fruitlogix.producer.navigation.producerMoreItems
import com.rebootech.fruitlogix.producer.navigation.producerNavTabs

@Composable
fun ProducerShell(
    onSwitchRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            onSwitchRole = onSwitchRole,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

