package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Expandable frequent question, for use inside a [SettingsGroup]. */
@Composable
fun FaqRow(question: String, answer: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    var expanded by rememberSaveable(question) { mutableStateOf(false) }
    val stateText = stringResource(if (expanded) R.string.help_cd_collapse else R.string.help_cd_expand)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { expanded = !expanded }
            .semantics { stateDescription = stateText }
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
            .padding(horizontal = FruitLogixTheme.spacing.sm, vertical = FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(question, style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            AppIcon(
                id = R.drawable.ic_chevron_right,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier
                    .size(FruitLogixTheme.sizing.iconSm)
                    .rotate(if (expanded) 90f else 0f)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Text(answer, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = FruitLogixTheme.spacing.xs))
        }
    }
}
