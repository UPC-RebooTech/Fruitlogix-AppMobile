package com.rebootech.fruitlogix.producer.profilesManagement.presentation.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.profilesManagement.domain.model.ProducerType
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileError
import com.rebootech.fruitlogix.producer.profilesManagement.domain.validation.ProfileField
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Shown to a producer account without a producer profile on the server (GET /producers/me → 404).
 * Products, harvest lots and orders need it, so it comes before the rest of the app.
 */
@Composable
fun CreateProducerProfileScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateProducerProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val form = state.form

    @Composable
    fun err(field: ProfileField): String? = state.errors[field]?.let { error ->
        stringResource(
            when (error) {
                ProfileError.REQUIRED -> R.string.profile_error_required
                ProfileError.INVALID_EMAIL -> R.string.profile_error_email
                ProfileError.INVALID_PHONE -> R.string.profile_error_phone
                ProfileError.INVALID_RUC -> R.string.profile_error_ruc
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        Text(stringResource(R.string.prof_create_title), style = FruitLogixTheme.typography.headlineMedium, color = scheme.onBackground)
        Text(stringResource(R.string.prof_create_message), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)

        Column {
            Text(stringResource(R.string.prof_create_type), style = FruitLogixTheme.typography.titleSmall, color = scheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                SelectableChip(
                    label = stringResource(R.string.prof_type_individual),
                    selected = form.type == ProducerType.INDIVIDUAL,
                    onClick = { viewModel.onTypeChange(ProducerType.INDIVIDUAL) }
                )
                SelectableChip(
                    label = stringResource(R.string.prof_type_company),
                    selected = form.type == ProducerType.COMPANY,
                    onClick = { viewModel.onTypeChange(ProducerType.COMPANY) }
                )
            }
        }

        LabeledTextField(
            label = stringResource(R.string.profile_field_full_name), value = form.fullName,
            onValueChange = { v -> viewModel.onChange(ProfileField.FULL_NAME) { it.copy(fullName = v) } },
            leadingIconRes = R.drawable.ic_person, error = err(ProfileField.FULL_NAME), capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(if (form.type == ProducerType.COMPANY) R.string.profile_field_legal_name else R.string.prof_create_legal_name_person),
            value = form.legalName,
            onValueChange = { v -> viewModel.onChange(null) { it.copy(legalName = v) } },
            leadingIconRes = R.drawable.ic_id, capitalization = KeyboardCapitalization.Words,
            helper = if (form.type == ProducerType.INDIVIDUAL) stringResource(R.string.prof_create_legal_name_helper) else null
        )
        LabeledTextField(
            label = stringResource(R.string.profile_field_ruc), value = form.ruc,
            onValueChange = { v -> viewModel.onChange(ProfileField.RUC) { it.copy(ruc = v.filter(Char::isDigit).take(11)) } },
            leadingIconRes = R.drawable.ic_receipt, error = err(ProfileField.RUC), keyboardType = KeyboardType.Number
        )
        LabeledTextField(
            label = stringResource(R.string.profile_field_email), value = form.email,
            onValueChange = { v -> viewModel.onChange(ProfileField.EMAIL) { it.copy(email = v.trim()) } },
            leadingIconRes = R.drawable.ic_send, error = err(ProfileField.EMAIL), keyboardType = KeyboardType.Email
        )
        LabeledTextField(
            label = stringResource(R.string.profile_field_phone), value = form.phone,
            onValueChange = { v -> viewModel.onChange(ProfileField.PHONE) { it.copy(phone = v) } },
            leadingIconRes = R.drawable.ic_call, error = err(ProfileField.PHONE), keyboardType = KeyboardType.Phone
        )
        LabeledTextField(
            label = stringResource(R.string.profile_field_crop), value = form.crop,
            onValueChange = { v -> viewModel.onChange(ProfileField.CROP) { it.copy(crop = v) } },
            placeholder = stringResource(R.string.dist_order_product_placeholder),
            leadingIconRes = R.drawable.ic_leaf, error = err(ProfileField.CROP), capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.profile_field_farm_name), value = form.farmName,
            onValueChange = { v -> viewModel.onChange(ProfileField.FARM_NAME) { it.copy(farmName = v) } },
            leadingIconRes = R.drawable.ic_warehouse, error = err(ProfileField.FARM_NAME), capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.profile_field_location), value = form.location,
            onValueChange = { v -> viewModel.onChange(ProfileField.LOCATION) { it.copy(location = v) } },
            placeholder = stringResource(R.string.prod_form_location_placeholder),
            leadingIconRes = R.drawable.ic_gps, error = err(ProfileField.LOCATION),
            capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done
        )

        state.serverError?.let { message ->
            Text(
                text = message.ifBlank { stringResource(R.string.profile_save_failed) },
                style = FruitLogixTheme.typography.bodyMedium,
                color = scheme.error
            )
        }

        PrimaryButton(
            text = stringResource(if (state.isSaving) R.string.route_sending else R.string.prof_create_submit),
            onClick = viewModel::submit,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        )
        TextActionButton(
            text = stringResource(R.string.auth_logout_button),
            onClick = onSignOut,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
