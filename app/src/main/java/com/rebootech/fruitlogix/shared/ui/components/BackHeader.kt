package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Title row for sub-screens: 48dp back button + title (+ optional subtitle).
 * Sits inside the screen content, under the app top bar.
 */
@Composable
fun BackHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.minTouch)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = R.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.common_back),
                tint = scheme.onSurface,
                modifier = Modifier.size(FruitLogixTheme.sizing.icon)
            )
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.headlineSmall,
                color = scheme.onBackground
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        actions()
    }
}

/** 48dp icon action for [BackHeader]. */
@Composable
fun HeaderAction(
    @androidx.annotation.DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(FruitLogixTheme.sizing.minTouch)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            id = iconRes,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(FruitLogixTheme.sizing.icon)
        )
    }
}
