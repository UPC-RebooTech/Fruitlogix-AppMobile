package com.rebootech.fruitlogix

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rebootech.fruitlogix.app.navigation.RootNavHost
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // Applies the in-app language on Android 12 and below (Android 13+ handles it natively).
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // English by default; Spanish only when the user picks it.
        AppLocale.ensureApplied(this)
        enableEdgeToEdge()
        setContent {
            FruitLogixTheme {
                RootNavHost()
            }
        }
    }
}
