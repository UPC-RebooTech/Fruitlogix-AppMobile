package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Text field with the label above (easier to read than a floating label) and the error
 * or help text below. 56dp tall.
 */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIconRes: Int? = null,
    error: String? = null,
    helper: String? = null,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: (() -> Unit)? = null,
    autofill: ContentType? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisible: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.titleSmall,
            color = if (error != null) scheme.error else scheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
                .then(if (autofill != null) Modifier.semantics { contentType = autofill } else Modifier),
            enabled = enabled,
            singleLine = true,
            textStyle = FruitLogixTheme.typography.bodyLarge,
            placeholder = placeholder?.let {
                { Text(it, style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant.copy(alpha = 0.6f)) }
            },
            leadingIcon = leadingIconRes?.let {
                {
                    AppIcon(
                        id = it,
                        contentDescription = null,
                        tint = if (error != null) scheme.error else scheme.onSurfaceVariant,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                }
            },
            trailingIcon = if (isPassword && onTogglePasswordVisible != null) {
                {
                    IconButton(onClick = onTogglePasswordVisible, modifier = Modifier.size(FruitLogixTheme.sizing.minTouch)) {
                        AppIcon(
                            id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility,
                            contentDescription = stringResource(
                                if (passwordVisible) R.string.iam_hide_password else R.string.iam_show_password
                            ),
                            tint = scheme.onSurfaceVariant,
                            modifier = Modifier.size(FruitLogixTheme.sizing.icon)
                        )
                    }
                }
            } else null,
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization,
                autoCorrectEnabled = false,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(onAny = { onImeAction?.invoke() ?: defaultKeyboardAction(imeAction) }),
            shape = FruitLogixTheme.shapes.Input,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = scheme.surface,
                unfocusedContainerColor = scheme.surface,
                disabledContainerColor = scheme.surfaceContainerLow,
                focusedBorderColor = scheme.primary,
                unfocusedBorderColor = scheme.outline,
                errorBorderColor = scheme.error,
                cursorColor = scheme.primary
            )
        )
        when {
            error != null -> Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(error, style = FruitLogixTheme.typography.bodySmall, color = scheme.error)
            }
            helper != null -> Text(
                text = helper,
                style = FruitLogixTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

