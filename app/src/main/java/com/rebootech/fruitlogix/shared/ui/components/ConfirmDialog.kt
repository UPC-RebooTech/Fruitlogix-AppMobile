package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Cancelar",
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                style = FruitLogixTheme.typography.titleMedium,
                color = FruitLogixTheme.colors.textOnDark
            )
        },
        text = {
            Text(
                text = message,
                style = FruitLogixTheme.typography.bodyMedium,
                color = FruitLogixTheme.colors.textMuted
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (destructive) FruitLogixTheme.colors.dangerStrong else FruitLogixTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = FruitLogixTheme.colors.textMuted
                )
            }
        },
        containerColor = FruitLogixTheme.colors.surfaceDark,
        shape = FruitLogixTheme.shapes.Card,
        modifier = modifier
    )
}

