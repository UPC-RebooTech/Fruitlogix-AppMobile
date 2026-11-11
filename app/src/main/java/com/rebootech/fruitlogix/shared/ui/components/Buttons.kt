package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.isOnDarkSurface

private val ButtonContentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)

@Composable
private fun ButtonContent(
    text: String,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        leadingIcon?.let {
            it()
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        }
        // Shrinks down to 13sp before wrapping, so labels like "Aceptar pedido" fit in half-width buttons.
        val style = MaterialTheme.typography.labelLarge
        Text(
            text = text,
            style = style,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 13.sp,
                maxFontSize = style.fontSize,
                stepSize = 0.5.sp
            ),
            modifier = Modifier.weight(1f, fill = false)
        )
        trailingIcon?.let {
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            it()
        }
    }
}

/**
 * Main call to action: Lima fill (primaryContainer), dark text, full pill, 56dp high.
 * Use at most one per screen.
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
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FruitLogixTheme.sizing.buttonHeight),
        enabled = enabled,
        shape = FruitLogixTheme.shapes.Pill,
        contentPadding = ButtonContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = scheme.primaryContainer,
            contentColor = scheme.onPrimaryContainer,
            disabledContainerColor = scheme.surfaceContainerHigh,
            disabledContentColor = scheme.onSurfaceVariant
        )
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

/**
 * Secondary action: Bosque fill with white text on light surfaces; on dark surfaces it
 * switches to a light fill so it stays visible. 12dp corners, 56dp high.
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
    val scheme = MaterialTheme.colorScheme
    val onDark = isOnDarkSurface()
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FruitLogixTheme.sizing.buttonHeight),
        enabled = enabled,
        shape = FruitLogixTheme.shapes.Button,
        contentPadding = ButtonContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (onDark) scheme.inverseOnSurface else scheme.secondary,
            contentColor = if (onDark) scheme.onSecondaryContainer else scheme.onSecondary,
            disabledContainerColor = if (onDark) {
                FruitLogixTheme.colors.textOnDark.copy(alpha = 0.12f)
            } else {
                scheme.surfaceContainerHigh
            },
            disabledContentColor = if (onDark) {
                FruitLogixTheme.colors.textMuted
            } else {
                scheme.onSurfaceVariant
            }
        )
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

/**
 * Low-emphasis action (e.g. "Ver detalle", "Cancelar"): text only, 48dp touch target.
 * Musgo on light surfaces, Lima on dark surfaces.
 */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val onDark = isOnDarkSurface()
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch),
        enabled = enabled,
        shape = FruitLogixTheme.shapes.Button,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (onDark) FruitLogixTheme.colors.primary else MaterialTheme.colorScheme.primary
        )
    ) {
        ButtonContent(text, leadingIcon, trailingIcon)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7FAF6)
@Composable
private fun ButtonsPreview() {
    FruitLogixTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrimaryButton(text = "Aceptar pedido", onClick = {}, modifier = Modifier.fillMaxWidth())
            SecondaryButton(text = "Ver detalle", onClick = {}, modifier = Modifier.fillMaxWidth())
            TextActionButton(text = "Cancelar", onClick = {})
            PrimaryButton(text = "Deshabilitado", onClick = {}, enabled = false)
        }
    }
}
