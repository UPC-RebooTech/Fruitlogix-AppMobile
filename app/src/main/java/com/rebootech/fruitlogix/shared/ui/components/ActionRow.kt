package com.rebootech.fruitlogix.shared.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** White row that opens something: icon, title, one line of detail and a chevron. 56dp+ tall. */
@Composable
fun ActionRow(
    @DrawableRes iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
            .whiteCard(FruitLogixTheme.shapes.Button)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(FruitLogixTheme.sizing.iconBox)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(scheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = iconRes, contentDescription = null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Text(subtitle, style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        AppIcon(id = R.drawable.ic_chevron_right, contentDescription = null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
    }
}
