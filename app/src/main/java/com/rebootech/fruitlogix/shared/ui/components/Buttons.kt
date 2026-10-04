package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

/**
 * Primary Button component with Lime background, dark text, pill shape, Poppins 600, min 48dp height.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary,
            disabledContainerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary.copy(alpha = 0.4f),
            disabledContentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.onPrimary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
            }
            Text(
                text = text,
                fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium
            )
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
                it()
            }
        }
    }
}

/**
 * Secondary Button component with Dark Surface background, white text, pill shape, min 48dp height.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
            disabledContainerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.5f),
            disabledContentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
            }
            Text(
                text = text,
                fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium
            )
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.spacing.xs))
                it()
            }
        }
    }
}

@Preview
@Composable
private fun ButtonsPreview() {
    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.PrimaryButton(
                text = "Confirm Order",
                onClick = {})
            _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.SecondaryButton(
                text = "Details",
                onClick = {})
        }
    }
}
