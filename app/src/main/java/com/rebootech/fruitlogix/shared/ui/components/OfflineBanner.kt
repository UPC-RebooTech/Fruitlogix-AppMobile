package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Sin conexión — se sincronizará automáticamente"
) {
    if (!isOffline) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(FruitLogixTheme.colors.warning.copy(alpha = 0.2f))
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            id = R.drawable.ic_warning,
            contentDescription = null,
            tint = FruitLogixTheme.colors.warning,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(
            text = message,
            style = FruitLogixTheme.typography.labelSmall,
            color = FruitLogixTheme.colors.textOnLight,
            fontWeight = FontWeight.Medium
        )
    }
}

