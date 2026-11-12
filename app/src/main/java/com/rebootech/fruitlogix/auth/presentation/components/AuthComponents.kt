package com.rebootech.fruitlogix.auth.presentation.components

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.core.locale.AppLocale
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.AppLanguage
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/** Logo + "FruitLogix" wordmark, used at the top of sign in. */
@Composable
fun AuthBrand(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(FruitLogixTheme.shapes.IconBox)
                .background(scheme.inverseSurface),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                id = R.drawable.ic_fruitlogix_brand,
                contentDescription = stringResource(R.string.iam_brand_cd),
                tint = Color.Unspecified,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = scheme.onSurface)) { append("Fruit") }
                withStyle(SpanStyle(color = scheme.primary)) { append("Logix") }
            },
            style = FruitLogixTheme.typography.headlineSmall
        )
    }
}

/**
 * ES | EN switch for screens without a top bar. Changing it recreates the activity,
 * so the whole screen is shown in the new language.
 */
@Composable
fun AuthLanguageSwitch(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val selected = remember { AppLocale.current(context) }
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Pill)
            .background(scheme.surfaceContainerHigh)
            .padding(2.dp)
            .selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            AppLanguage.ES to stringResource(R.string.iam_language_es),
            AppLanguage.EN to stringResource(R.string.iam_language_en)
        ).forEach { (language, name) ->
            val isSelected = language == selected
            Box(
                modifier = Modifier
                    .heightIn(min = FruitLogixTheme.sizing.minTouch - 4.dp)
                    .clip(FruitLogixTheme.shapes.Pill)
                    .background(if (isSelected) scheme.primary else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { if (!isSelected) activity?.let { AppLocale.apply(it, language) } }
                    )
                    .semantics { contentDescription = name }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = language.name,
                    style = FruitLogixTheme.typography.labelLarge,
                    color = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Error shown above the main button (wrong credentials, no connection). */
@Composable
fun AuthErrorBanner(message: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.errorContainer)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.onErrorContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(message, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
    }
}

/** Checkbox with a 48dp+ tappable row, label included. */
@Composable
fun AuthCheckRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = scheme.primary,
                checkmarkColor = scheme.onPrimary,
                uncheckedColor = if (isError) scheme.error else scheme.outline
            )
        )
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Text(label, style = FruitLogixTheme.typography.bodyLarge, color = if (isError) scheme.error else scheme.onSurface)
    }
}

/** Live password checklist: each rule turns green when it is met. */
@Composable
fun PasswordRules(rules: List<Pair<String, Boolean>>, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Column(modifier = modifier.padding(top = FruitLogixTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rules.forEach { (text, met) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(FruitLogixTheme.shapes.Pill)
                        .then(
                            if (met) Modifier.background(colors.successContent)
                            else Modifier.border(1.5.dp, scheme.outline, FruitLogixTheme.shapes.Pill)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (met) AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(text, style = FruitLogixTheme.typography.bodyMedium, color = if (met) colors.successContent else scheme.onSurfaceVariant)
            }
        }
    }
}
