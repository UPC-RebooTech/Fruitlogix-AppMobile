package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Ha ocurrido un error"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FruitLogixTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppIcon(
            id = R.drawable.ic_warning,
            contentDescription = null,
            tint = FruitLogixTheme.colors.dangerStrong,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))

        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            style = FruitLogixTheme.typography.titleMedium,
            color = FruitLogixTheme.colors.textOnLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

        Text(
            text = message,
            style = FruitLogixTheme.typography.bodyMedium,
            color = FruitLogixTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))

        SecondaryButton(
            text = "Reintentar",
            onClick = onRetry
        )
    }
}

