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
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Offline notice shown at the top of a screen. Mango (attention) container, icon + text.
 */
@Composable
fun OfflineBanner(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    message: String = stringResource(R.string.sync_offline)
) {
    if (!isOffline) return

    val colors = FruitLogixTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.attentionContainer)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(
            id = R.drawable.ic_warning,
            contentDescription = null,
            tint = colors.attentionContent,
            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(
            text = message,
            style = FruitLogixTheme.typography.labelMedium,
            color = colors.onAttentionContainer
        )
    }
}
