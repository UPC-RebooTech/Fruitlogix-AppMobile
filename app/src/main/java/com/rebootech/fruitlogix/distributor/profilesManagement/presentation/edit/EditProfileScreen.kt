package com.rebootech.fruitlogix.distributor.profilesManagement.presentation.edit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.DistributorProfileRules.Error
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SettingsGroup
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Edit profile (distributor): personal data plus company data (role, hub, RUC).
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
    val savedMessage = stringResource(R.string.dprof_saved)

    LaunchedEffect(state.saved) {
        if (state.saved) {
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            onNavigateBack()
        }
    }

    @Composable
    fun errorFor(error: Error, res: Int): String? = if (error in state.errors) stringResource(res) else null

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
            BackHeader(title = stringResource(R.string.profile_edit_title), onBack = onNavigateBack)

            SettingsGroup(title = stringResource(R.string.profile_section_personal)) {
                FormFields {
                    LabeledTextField(
                        label = stringResource(R.string.dprof_full_name),
                        value = form.fullName,
                        onValueChange = { v -> viewModel.onChange(Error.NAME) { it.copy(fullName = v) } },
                        leadingIconRes = R.drawable.ic_person,
                        error = errorFor(Error.NAME, R.string.dprof_error_name),
                        capitalization = KeyboardCapitalization.Words
                    )
                    LabeledTextField(
                        label = stringResource(R.string.dprof_email),
                        value = form.email,
                        onValueChange = { v -> viewModel.onChange(Error.EMAIL) { it.copy(email = v.trim()) } },
                        leadingIconRes = R.drawable.ic_send,
                        error = errorFor(Error.EMAIL, R.string.dprof_error_email),
                        keyboardType = KeyboardType.Email
                    )
                    LabeledTextField(
                        label = stringResource(R.string.dprof_phone),
                        value = form.phone,
                        onValueChange = { v -> viewModel.onChange(Error.PHONE) { it.copy(phone = v) } },
                        leadingIconRes = R.drawable.ic_call,
                        error = errorFor(Error.PHONE, R.string.dprof_error_phone),
                        keyboardType = KeyboardType.Phone
                    )
                }
            }

            SettingsGroup(title = stringResource(R.string.dprof_section_business)) {
                FormFields {
                    LabeledTextField(
                        label = stringResource(R.string.dprof_role_title),
                        value = form.roleTitle,
                        onValueChange = { v -> viewModel.onChange(null) { it.copy(roleTitle = v) } },
                        leadingIconRes = R.drawable.ic_id,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                    LabeledTextField(
                        label = stringResource(R.string.dprof_hub),
                        value = form.companyHub,
                        onValueChange = { v -> viewModel.onChange(null) { it.copy(companyHub = v) } },
                        leadingIconRes = R.drawable.ic_warehouse,
                        capitalization = KeyboardCapitalization.Words
                    )
                    LabeledTextField(
                        label = stringResource(R.string.dprof_ruc),
                        value = form.ruc,
                        onValueChange = { v -> viewModel.onChange(Error.RUC) { it.copy(ruc = v.filter(Char::isDigit).take(11)) } },
                        leadingIconRes = R.drawable.ic_receipt,
                        error = errorFor(Error.RUC, R.string.dprof_error_ruc),
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                }
            }
        }

        PrimaryButton(
            text = stringResource(R.string.dprof_save),
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
