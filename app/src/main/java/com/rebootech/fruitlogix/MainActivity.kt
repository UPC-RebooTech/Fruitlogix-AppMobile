package com.rebootech.fruitlogix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rebootech.fruitlogix.app.navigation.RootNavHost
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FruitLogixTheme {
                RootNavHost()
            }
        }
    }
}
