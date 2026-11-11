package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Confirmation dialog. Dark surface (Bosque), consistent with the other dialogs in the app.
 * Confirm is the Lima pill, or a red pill when [destructive]. Cancel is a text action.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = stringResource(R.string.common_cancel),
    destructive: Boolean = false
) {
    val colors = FruitLogixTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = FruitLogixTheme.typography.titleLarge
            )
        },
        text = {
            Text(
                text = message,
                style = FruitLogixTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            if (destructive) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch),
                    shape = FruitLogixTheme.shapes.Pill,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.dangerStrong,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(text = confirmText, style = FruitLogixTheme.typography.labelLarge)
                }
            } else {
                PrimaryButton(
                    text = confirmText,
                    onClick = onConfirm
                )
            }
        },
        dismissButton = {
            // AlertDialog tints buttons with the primary color; mark this area as a dark surface
            // so the text action picks its light variant.
            CompositionLocalProvider(LocalContentColor provides colors.textOnDark) {
                TextActionButton(
                    text = dismissText,
                    onClick = onDismiss
                )
            }
        },
        containerColor = colors.surfaceDark,
        titleContentColor = colors.textOnDark,
        textContentColor = colors.textMuted,
        shape = FruitLogixTheme.shapes.Dialog,
        modifier = modifier
    )
}
