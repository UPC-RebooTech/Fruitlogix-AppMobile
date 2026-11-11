package com.rebootech.fruitlogix.producer.profilesManagement.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Section label + light card that groups settings rows (separated by hairlines). */
@Composable
fun SettingsGroup(
    title: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        title?.let {
            Text(
                text = it.uppercase(),
                style = FruitLogixTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = FruitLogixTheme.spacing.xxs,
                    bottom = FruitLogixTheme.spacing.xs
                )
            )
        }
        FruitLogixCard(variant = CardVariant.White, modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 68.dp)
    )
}

/** Navigation row: icon box, title, summary and chevron. 64dp min height. */
@Composable
fun SettingsNavRow(
    @DrawableRes iconRes: Int,
    title: String,
    summary: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(iconRes = iconRes, tint = tint)
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.titleSmall,
                color = tint ?: scheme.onSurface
            )
            summary?.let {
                Text(
                    text = it,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        if (tint == null) {
            AppIcon(
                id = R.drawable.ic_chevron_right,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
            )
        }
    }
}

/** Switch row: the whole row toggles (large touch target). */
@Composable
fun SettingsSwitchRow(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    enabled: Boolean = true
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconRes?.let {
            SettingsIcon(iconRes = it)
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.titleSmall,
                color = if (enabled) scheme.onSurface else scheme.onSurfaceVariant
            )
            description?.let {
                Text(
                    text = it,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        // The row handles the toggle; the switch is visual only.
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/** Radio row for single-choice settings (e.g. language). */
@Composable
fun SettingsRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = FruitLogixTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = FruitLogixTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        RadioButton(selected = selected, onClick = null)
    }
}

@Composable
private fun SettingsIcon(@DrawableRes iconRes: Int, tint: Color? = null) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(FruitLogixTheme.sizing.iconBox)
            .clip(FruitLogixTheme.shapes.IconBox)
            .background(if (tint != null) scheme.errorContainer else scheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(
            id = iconRes,
            contentDescription = null,
            tint = tint ?: scheme.onSecondaryContainer,
            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
        )
    }
}

