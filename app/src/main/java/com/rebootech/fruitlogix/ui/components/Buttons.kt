package com.rebootech.fruitlogix.ui.components

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
import com.rebootech.fruitlogix.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.ui.theme.PoppinsFontFamily

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
        shape = FruitLogixTheme.shapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = FruitLogixTheme.colors.primary,
            contentColor = FruitLogixTheme.colors.onPrimary,
            disabledContainerColor = FruitLogixTheme.colors.primary.copy(alpha = 0.4f),
            disabledContentColor = FruitLogixTheme.colors.onPrimary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            }
            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                style = FruitLogixTheme.typography.titleMedium
            )
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
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
        shape = FruitLogixTheme.shapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = FruitLogixTheme.colors.surfaceDark,
            contentColor = FruitLogixTheme.colors.textOnDark,
            disabledContainerColor = FruitLogixTheme.colors.surfaceDark.copy(alpha = 0.5f),
            disabledContentColor = FruitLogixTheme.colors.textMuted
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                it()
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            }
            Text(
                text = text,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.SemiBold,
                style = FruitLogixTheme.typography.titleMedium
            )
            trailingIcon?.let {
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                it()
            }
        }
    }
}

@Preview
@Composable
private fun ButtonsPreview() {
    FruitLogixTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(text = "Confirm Order", onClick = {})
            SecondaryButton(text = "Details", onClick = {})
        }
    }
}
