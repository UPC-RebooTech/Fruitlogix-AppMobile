package com.rebootech.fruitlogix.shared.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Round quick action with a label below. The whole column is the touch target.
 * Labels reserve two lines so the four actions stay aligned in both languages.
 */
@Composable
fun QuickActionButton(
    @DrawableRes iconRes: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Card)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = FruitLogixTheme.spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.fieldTouch)
                .clip(CircleShape)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = iconRes,
                contentDescription = null,
                tint = scheme.onSecondaryContainer,
                modifier = Modifier.size(FruitLogixTheme.sizing.icon)
            )
        }

        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))

        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
            minLines = 2,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
