package com.rebootech.fruitlogix.producer.profilesManagement.presentation.edit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileError
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileField
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.format.DateTimeFormatter

/**
 * Edit profile: personal data plus farm and legal data (RUC, location, certification).
 * These details only appear here, not on the account screen.
 */
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val context = LocalContext.current
    val form = state.form
    val savedMessage = stringResource(R.string.profile_saved)

    LaunchedEffect(state.saved) {
        if (state.saved) {
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            BackHeader(
                title = stringResource(R.string.profile_edit_title),
                onBack = onNavigateBack
            )

            SettingsGroup(title = stringResource(R.string.profile_section_personal)) {
                FormFields {
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_full_name),
                        value = form.fullName,
                        error = state.errors[ProfileField.FULL_NAME],
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = { v -> viewModel.onChange(ProfileField.FULL_NAME) { it.copy(fullName = v) } }
                    )
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_email),
                        value = form.email,
                        error = state.errors[ProfileField.EMAIL],
                        keyboardType = KeyboardType.Email,
                        onValueChange = { v -> viewModel.onChange(ProfileField.EMAIL) { it.copy(email = v) } }
                    )
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_phone),
                        value = form.phone,
                        error = state.errors[ProfileField.PHONE],
                        keyboardType = KeyboardType.Phone,
                        onValueChange = { v -> viewModel.onChange(ProfileField.PHONE) { it.copy(phone = v) } }
                    )
                }
            }

            SettingsGroup(title = stringResource(R.string.profile_section_farm)) {
                FormFields {
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_farm_name),
                        value = form.farmName,
                        error = state.errors[ProfileField.FARM_NAME],
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = { v -> viewModel.onChange(ProfileField.FARM_NAME) { it.copy(farmName = v) } }
                    )
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_legal_name),
                        value = form.legalName,
                        error = null,
                        capitalization = KeyboardCapitalization.Words,
                        onValueChange = { v -> viewModel.onChange(null) { it.copy(legalName = v) } }
                    )
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_ruc),
                        value = form.ruc,
                        error = state.errors[ProfileField.RUC],
                        keyboardType = KeyboardType.Number,
                        mono = true,
                        onValueChange = { v ->
                            viewModel.onChange(ProfileField.RUC) { it.copy(ruc = v.filter(Char::isDigit).take(11)) }
                        }
                    )
                    ProfileTextField(
                        label = stringResource(R.string.profile_field_location),
                        value = form.location,
                        error = state.errors[ProfileField.LOCATION],
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                        onValueChange = { v -> viewModel.onChange(ProfileField.LOCATION) { it.copy(location = v) } }
                    )

                    // Certification (read-only, managed by the certifier)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            id = R.drawable.ic_verified,
                            contentDescription = null,
                            tint = scheme.primary,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                        Spacer(modifier = Modifier.width(spacing.xs))
                        Text(
                            text = stringResource(R.string.profile_certification),
                            style = FruitLogixTheme.typography.titleSmall,
                            color = scheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        val validUntil = form.globalGapValidUntil
                        val locale = appLocale()
                        StatusBadge(
                            text = if (validUntil != null) {
                                stringResource(
                                    R.string.profile_certification_valid,
                                    validUntil.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))
                                )
                            } else {
                                stringResource(R.string.profile_certification_missing)
                            },
                            type = if (validUntil != null) StatusBadgeType.SUCCESS else StatusBadgeType.NEUTRAL,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.profile_save),
            onClick = viewModel::onSave,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.sm, vertical = spacing.xs)
        )
    }
}

@Composable
private fun FormFields(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(FruitLogixTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.inner)
    ) {
        content()
    }
}

@Composable
private fun ProfileTextField(
    label: String,
    value: String,
    error: ProfileError?,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    imeAction: ImeAction = ImeAction.Next,
    mono: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(errorMessage(it)) } },
        textStyle = if (mono) FruitLogixTheme.extraTypography.dataMono else FruitLogixTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = capitalization,
            imeAction = imeAction
        ),
        shape = FruitLogixTheme.shapes.Input,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = scheme.surfaceContainerLowest,
            unfocusedContainerColor = scheme.surfaceContainerLowest,
            unfocusedBorderColor = scheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun errorMessage(error: ProfileError): String = stringResource(
    when (error) {
        ProfileError.REQUIRED -> R.string.profile_error_required
        ProfileError.INVALID_EMAIL -> R.string.profile_error_email
        ProfileError.INVALID_PHONE -> R.string.profile_error_phone
        ProfileError.INVALID_RUC -> R.string.profile_error_ruc
    }
)
