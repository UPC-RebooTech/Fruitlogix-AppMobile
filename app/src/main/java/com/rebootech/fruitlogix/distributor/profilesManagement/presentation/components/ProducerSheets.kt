package com.rebootech.fruitlogix.distributor.profilesManagement.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.Producer
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerDraft
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.model.ProducerEntityType
import com.rebootech.fruitlogix.distributor.profilesManagement.domain.rules.ProducerRules
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

@Composable
fun ProfileSheetError(text: String?) {
    if (text == null) return
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.errorContainer)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.onErrorContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(text, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
    }
}

/** Register (producer = null, US05) or edit (US39) a producer. */
@Composable
fun ProducerFormSheet(
    producer: Producer?,
    errors: Set<ProducerRules.Error>,
    onSave: (ProducerDraft) -> Unit,
    serverError: String? = null,
    saving: Boolean = false
) {
    val key = producer?.id ?: "new"
    var type by rememberSaveable(key) { mutableStateOf(producer?.entityType ?: ProducerEntityType.COMPANY) }
    var name by rememberSaveable(key) { mutableStateOf(producer?.name.orEmpty()) }
    var farm by rememberSaveable(key) { mutableStateOf(producer?.farmName.orEmpty()) }
    var location by rememberSaveable(key) { mutableStateOf(producer?.location.orEmpty()) }
    var crop by rememberSaveable(key) { mutableStateOf(producer?.cropType.orEmpty()) }
    var email by rememberSaveable(key) { mutableStateOf(producer?.email.orEmpty()) }
    var phone by rememberSaveable(key) { mutableStateOf(producer?.phone.orEmpty()) }
    var ruc by rememberSaveable(key) { mutableStateOf(producer?.ruc.orEmpty()) }

    @Composable
    fun err(e: ProducerRules.Error, res: Int) = if (e in errors) stringResource(res) else null

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Column {
            Text(stringResource(R.string.prod_form_type), style = FruitLogixTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                SelectableChip(label = stringResource(R.string.prod_company), selected = type == ProducerEntityType.COMPANY, onClick = { type = ProducerEntityType.COMPANY })
                SelectableChip(label = stringResource(R.string.prod_individual), selected = type == ProducerEntityType.INDIVIDUAL, onClick = { type = ProducerEntityType.INDIVIDUAL })
            }
        }
        LabeledTextField(
            label = stringResource(R.string.prod_form_name), value = name, onValueChange = { name = it },
            placeholder = stringResource(R.string.prod_form_name_placeholder), leadingIconRes = R.drawable.ic_person,
            error = err(ProducerRules.Error.NAME, R.string.prod_error_name), capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_farm), value = farm, onValueChange = { farm = it },
            leadingIconRes = R.drawable.ic_leaf, error = err(ProducerRules.Error.FARM, R.string.prod_error_farm),
            capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_ruc), value = ruc, onValueChange = { ruc = it.filter { c -> c.isDigit() }.take(11) },
            leadingIconRes = R.drawable.ic_id,
            error = err(ProducerRules.Error.RUC_FORMAT, R.string.prod_error_ruc) ?: err(ProducerRules.Error.RUC_TAKEN, R.string.prod_error_ruc_taken),
            keyboardType = KeyboardType.Number
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_location), value = location, onValueChange = { location = it },
            placeholder = stringResource(R.string.prod_form_location_placeholder), leadingIconRes = R.drawable.ic_gps,
            error = err(ProducerRules.Error.LOCATION, R.string.prod_error_location), capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_crop), value = crop, onValueChange = { crop = it },
            placeholder = stringResource(R.string.dist_order_product_placeholder), leadingIconRes = R.drawable.ic_inventory,
            error = err(ProducerRules.Error.CROP, R.string.prod_error_crop), capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_email), value = email, onValueChange = { email = it.trim() },
            leadingIconRes = R.drawable.ic_send,
            error = err(ProducerRules.Error.EMAIL_FORMAT, R.string.prod_error_email) ?: err(ProducerRules.Error.EMAIL_TAKEN, R.string.prod_error_email_taken),
            keyboardType = KeyboardType.Email
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_phone), value = phone, onValueChange = { phone = it },
            leadingIconRes = R.drawable.ic_call, error = err(ProducerRules.Error.PHONE_FORMAT, R.string.prod_error_phone),
            keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done
        )
        ProfileSheetError(serverError)
        PrimaryButton(
            text = stringResource(if (producer == null) R.string.prod_form_save_register else R.string.prod_form_save_edit),
            onClick = { onSave(ProducerDraft(name, farm, location, crop, email, phone, type, ruc)) },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Invite by email or phone (US41). */
@Composable
fun InviteProducerSheet(error: String?, onInvite: (name: String, contact: String, location: String, crop: String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var crop by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.prod_invite_message), style = FruitLogixTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ProfileSheetError(error)
        LabeledTextField(
            label = stringResource(R.string.prod_invite_name), value = name, onValueChange = { name = it },
            leadingIconRes = R.drawable.ic_person, capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.prod_invite_contact), value = contact, onValueChange = { contact = it },
            placeholder = stringResource(R.string.prod_invite_contact_placeholder), leadingIconRes = R.drawable.ic_send,
            keyboardType = KeyboardType.Email
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_location), value = location, onValueChange = { location = it },
            placeholder = stringResource(R.string.prod_form_location_placeholder), leadingIconRes = R.drawable.ic_gps,
            capitalization = KeyboardCapitalization.Words
        )
        LabeledTextField(
            label = stringResource(R.string.prod_form_crop), value = crop, onValueChange = { crop = it },
            placeholder = stringResource(R.string.dist_order_product_placeholder), leadingIconRes = R.drawable.ic_inventory,
            capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done
        )
        PrimaryButton(text = stringResource(R.string.prod_invite_send), onClick = { onInvite(name, contact, location, crop) }, modifier = Modifier.fillMaxWidth())
    }
}
