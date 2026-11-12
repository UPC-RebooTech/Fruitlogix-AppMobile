package com.rebootech.fruitlogix.distributor.orderManagement.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.orderManagement.domain.model.OrderDraft
import com.rebootech.fruitlogix.distributor.orderManagement.domain.validation.OrderValidator
import com.rebootech.fruitlogix.distributor.orderManagement.presentation.formatLongDate
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Products the distributor orders most, offered as one-tap chips. */
val CommonProducts = listOf("Palta Hass", "Mango Kent", "Fresa", "Arándano", "Uva Red Globe", "Lima Tahití")

/** Form values shared by create and edit. Errors are string resources. */
data class OrderFormState(
    val clientName: String = "",
    val productName: String = "",
    val quantity: String = "",
    val deliveryDate: LocalDate? = null,
    @field:StringRes val clientError: Int? = null,
    @field:StringRes val productError: Int? = null,
    @field:StringRes val quantityError: Int? = null,
    @field:StringRes val dateError: Int? = null
) {
    val estimatedPallets: Int?
        get() = OrderValidator.parseTons(quantity)
            ?.takeIf { OrderValidator.isValidQuantity(it) }
            ?.let { OrderValidator.estimatePallets(it) }

    /** Returns the draft when everything is valid, otherwise this form with its errors. */
    fun validate(): Pair<OrderFormState, OrderDraft?> {
        val tons = OrderValidator.parseTons(quantity)
        val checked = copy(
            clientError = if (OrderValidator.isValidClient(clientName)) null else R.string.dist_order_error_client,
            productError = if (OrderValidator.isValidProduct(productName)) null else R.string.dist_order_error_product,
            quantityError = if (OrderValidator.isValidQuantity(tons)) null else R.string.dist_order_error_quantity,
            dateError = if (OrderValidator.isValidDeliveryDate(deliveryDate)) null else R.string.dist_order_error_date
        )
        val ok = checked.clientError == null && checked.productError == null &&
            checked.quantityError == null && checked.dateError == null
        return checked to if (ok) OrderDraft(clientName.trim(), productName.trim(), tons!!, deliveryDate!!) else null
    }
}

@Composable
fun OrderFormFields(
    form: OrderFormState,
    onClientChange: (String) -> Unit,
    onProductChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    enabled: Boolean = true
) {
    val spacing = FruitLogixTheme.spacing
    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
        LabeledTextField(
            label = stringResource(R.string.dist_order_client),
            value = form.clientName,
            onValueChange = onClientChange,
            placeholder = stringResource(R.string.dist_order_client_placeholder),
            leadingIconRes = R.drawable.ic_warehouse,
            error = form.clientError?.let { stringResource(it) },
            enabled = enabled,
            capitalization = KeyboardCapitalization.Words
        )

        Column {
            LabeledTextField(
                label = stringResource(R.string.dist_order_product),
                value = form.productName,
                onValueChange = onProductChange,
                placeholder = stringResource(R.string.dist_order_product_placeholder),
                leadingIconRes = R.drawable.ic_leaf,
                error = form.productError?.let { stringResource(it) },
                enabled = enabled,
                capitalization = KeyboardCapitalization.Words
            )
            if (enabled) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = stringResource(R.string.dist_order_product_suggestions),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.xxs))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    CommonProducts.forEach { product ->
                        SelectableChip(
                            label = product,
                            selected = form.productName.equals(product, ignoreCase = true),
                            onClick = { onProductChange(product) }
                        )
                    }
                }
            }
        }

        LabeledTextField(
            label = stringResource(R.string.dist_order_quantity),
            value = form.quantity,
            onValueChange = onQuantityChange,
            placeholder = stringResource(R.string.dist_order_quantity_placeholder),
            leadingIconRes = R.drawable.ic_inventory,
            error = form.quantityError?.let { stringResource(it) },
            helper = form.estimatedPallets?.let { stringResource(R.string.dist_order_quantity_pallets, it) },
            enabled = enabled,
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )

        DateField(
            date = form.deliveryDate,
            onDateChange = onDateChange,
            error = form.dateError?.let { stringResource(it) },
            enabled = enabled
        )
    }
}

/** Looks like a text field; tapping it opens the calendar (no typing dates by hand). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    date: LocalDate?,
    onDateChange: (LocalDate) -> Unit,
    error: String?,
    enabled: Boolean
) {
    val scheme = MaterialTheme.colorScheme
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val openDescription = stringResource(R.string.dist_order_date_cd)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.dist_order_date),
            style = FruitLogixTheme.typography.titleSmall,
            color = if (error != null) scheme.error else scheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FruitLogixTheme.sizing.fieldTouch)
                .clip(FruitLogixTheme.shapes.Input)
                .background(if (enabled) scheme.surface else scheme.surfaceContainerLow)
                .border(1.dp, if (error != null) scheme.error else scheme.outline, FruitLogixTheme.shapes.Input)
                .clickable(enabled = enabled, role = Role.Button) { showPicker = true }
                .semantics { contentDescription = openDescription }
                .padding(horizontal = FruitLogixTheme.spacing.inner),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(id = R.drawable.ic_schedule, contentDescription = null, tint = if (error != null) scheme.error else scheme.onSurfaceVariant, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
            Text(
                text = date?.let { formatLongDate(it) } ?: stringResource(R.string.dist_order_date_placeholder),
                style = FruitLogixTheme.typography.bodyLarge,
                color = if (date != null) scheme.onSurface else scheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )
        }
        error?.let {
            Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.Top) {
                AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(it, style = FruitLogixTheme.typography.bodySmall, color = scheme.error)
            }
        }
    }

    if (showPicker) {
        val todayMillis = remember { LocalDate.now().toUtcMillis() }
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (date ?: LocalDate.now()).toUtcMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= todayMillis
                override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onDateChange(it.toLocalDate()) }
                    showPicker = false
                }) { Text(stringResource(R.string.dist_order_date_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
