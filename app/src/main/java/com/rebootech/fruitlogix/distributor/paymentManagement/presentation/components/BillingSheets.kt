package com.rebootech.fruitlogix.distributor.paymentManagement.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.CreditReason
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PricePoint
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.ProposedCreditNote
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.PurchaseSummary
import com.rebootech.fruitlogix.distributor.paymentManagement.domain.model.SummaryPeriod
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.LabeledTextField
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SecondaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.whiteCard
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatPen
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat
import java.time.format.TextStyle

/** Error line inside a sheet. */
@Composable
fun BillingSheetError(text: String?) {
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

@Composable
private fun ChipRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)
    ) {
        options.forEach { SelectableChip(label = it, selected = it == selected, onClick = { onSelect(it) }) }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = FruitLogixTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(6.dp))
}

// US66: invoice a reception ---------------------------------------------------

@Composable
fun AutoInvoiceSheet(
    clients: List<String>,
    error: String?,
    onIssue: (client: String, kilos: String, price: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    var client by rememberSaveable { mutableStateOf("") }
    var kilos by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    val k = kilos.replace(',', '.').toDoubleOrNull()
    val p = price.replace(',', '.').toDoubleOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Text(stringResource(R.string.inv_auto_message), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
        BillingSheetError(error)
        Column {
            LabeledTextField(
                label = stringResource(R.string.inv_auto_client),
                value = client,
                onValueChange = { client = it },
                placeholder = stringResource(R.string.inv_auto_client_placeholder),
                leadingIconRes = R.drawable.ic_warehouse,
                capitalization = KeyboardCapitalization.Words
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            ChipRow(clients, client) { client = it }
        }
        LabeledTextField(
            label = stringResource(R.string.inv_auto_kilos),
            value = kilos,
            onValueChange = { kilos = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(8) },
            leadingIconRes = R.drawable.ic_inventory,
            keyboardType = KeyboardType.Decimal
        )
        LabeledTextField(
            label = stringResource(R.string.inv_auto_price),
            value = price,
            onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(7) },
            leadingIconRes = R.drawable.ic_invoices,
            helper = stringResource(R.string.inv_auto_price_help),
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
        if (k != null && p != null && k > 0 && p > 0) {
            Text(
                text = stringResource(R.string.inv_auto_total, formatPen(k * p)),
                style = FruitLogixTheme.typography.titleLarge,
                color = scheme.primary
            )
        }
        PrimaryButton(text = stringResource(R.string.inv_auto_issue), onClick = { onIssue(client, kilos, price) }, modifier = Modifier.fillMaxWidth())
    }
}

// US67: credit notes ------------------------------------------------------------

@Composable
fun creditReasonText(note: ProposedCreditNote): String = when (note.reason) {
    CreditReason.THERMAL_EXCURSION -> stringResource(
        R.string.inv_credit_thermal,
        "${formatDecimal(note.peakCelsius ?: 0.0)} °C",
        note.excursionMinutes ?: 0,
        note.affectedKg,
        note.productName
    )
    CreditReason.REJECTED_FRUIT -> stringResource(R.string.inv_credit_rejected, note.affectedKg, note.productName)
    CreditReason.DAMAGED_IN_RECEPTION -> stringResource(R.string.inv_credit_damaged, note.affectedKg, note.productName)
}

@Composable
fun CreditNotesSheet(
    notes: List<ProposedCreditNote>,
    adjustingNoteId: String?,
    error: String?,
    onApprove: (noteId: String) -> Unit,
    onStartAdjust: (noteId: String) -> Unit,
    onApproveAmount: (noteId: String, amount: String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    if (notes.isEmpty()) {
        Text(stringResource(R.string.inv_credit_none), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.inner)) {
        notes.forEach { note ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .whiteCard()
                    .padding(FruitLogixTheme.spacing.inner)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(note.id, style = FruitLogixTheme.extraTypography.dataMono, color = scheme.onSurface, modifier = Modifier.weight(1f))
                    if (note.isApproved) {
                        StatusBadge(text = stringResource(R.string.inv_credit_badge_approved), type = StatusBadgeType.SUCCESS, maxLines = 1, softWrap = false)
                    }
                }
                Text(stringResource(R.string.inv_credit_for, note.invoiceId, note.clientName), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
                Text(creditReasonText(note), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurface)
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                Text(
                    text = note.approvedAmount?.let { stringResource(R.string.inv_credit_approved, formatPen(it)) }
                        ?: stringResource(R.string.inv_credit_suggested, formatPen(note.suggestedAmount)),
                    style = FruitLogixTheme.typography.titleMedium,
                    color = if (note.isApproved) FruitLogixTheme.colors.successContent else scheme.primary
                )
                if (!note.isApproved) {
                    Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                    if (adjustingNoteId == note.id) {
                        var amount by rememberSaveable(note.id) { mutableStateOf(note.suggestedAmount.toString()) }
                        BillingSheetError(error)
                        LabeledTextField(
                            label = stringResource(R.string.inv_credit_amount),
                            value = amount,
                            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(9) },
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        )
                        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                        PrimaryButton(text = stringResource(R.string.inv_credit_save), onClick = { onApproveAmount(note.id, amount) }, modifier = Modifier.fillMaxWidth())
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                            PrimaryButton(text = stringResource(R.string.inv_credit_approve), onClick = { onApprove(note.id) }, modifier = Modifier.weight(1f))
                            SecondaryButton(text = stringResource(R.string.inv_credit_adjust), onClick = { onStartAdjust(note.id) }, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// US68: price per kilo ------------------------------------------------------------

@Composable
fun PricesSheet(
    products: List<String>,
    clients: List<String>,
    historyFor: (product: String, client: String) -> List<PricePoint>
) {
    val scheme = MaterialTheme.colorScheme
    var product by rememberSaveable { mutableStateOf(products.firstOrNull().orEmpty()) }
    var client by rememberSaveable { mutableStateOf(clients.firstOrNull().orEmpty()) }
    val history = historyFor(product, client)
    val locale = appLocale()

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Column { Label(stringResource(R.string.inv_prices_product)); ChipRow(products, product) { product = it } }
        Column { Label(stringResource(R.string.inv_prices_client)); ChipRow(clients, client) { client = it } }
        if (history.size < 2) {
            Text(stringResource(R.string.inv_prices_none, product, client), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            return@Column
        }
        val first = history.first().pricePerKg
        val last = history.last().pricePerKg
        val change = (last - first) / first * 100
        val money = NumberFormat.getNumberInstance(locale).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
        val firstText = stringResource(R.string.common_amount_pen, money.format(first))
        val lastText = stringResource(R.string.common_amount_pen, money.format(last))
        Text(stringResource(R.string.inv_prices_now, lastText), style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
        Text(
            text = stringResource(R.string.inv_prices_change, (if (change >= 0) "+" else "") + formatDecimal(change) + "%", history.size - 1),
            style = FruitLogixTheme.typography.bodyMedium,
            color = if (change >= 0) FruitLogixTheme.colors.successContent else scheme.error
        )
        val labels = history.map { it.month.month.getDisplayName(TextStyle.SHORT, locale).replaceFirstChar { c -> c.titlecase(locale) } }
        val description = stringResource(R.string.inv_prices_chart_cd, history.size, firstText, lastText)
        PriceChart(
            values = history.map { it.pricePerKg },
            labels = labels,
            valueLabels = history.map { money.format(it.pricePerKg) },
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .whiteCard()
                .padding(FruitLogixTheme.spacing.inner)
                .semantics { contentDescription = description }
        )
    }
}

@Composable
private fun PriceChart(values: List<Double>, labels: List<String>, valueLabels: List<String>, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = FruitLogixTheme.extraTypography.dataMonoSmall.copy(color = scheme.onSurfaceVariant)
    val valueStyle = FruitLogixTheme.extraTypography.dataMonoSmall.copy(color = scheme.onSurface)
    Canvas(modifier = modifier) {
        val top = 18.dp.toPx()
        val bottom = 20.dp.toPx()
        val side = 16.dp.toPx()
        val w = size.width - side * 2
        val h = size.height - top - bottom
        val min = values.min()
        val max = values.max()
        val span = (max - min).takeIf { it > 0 } ?: 1.0
        fun x(i: Int) = side + w * i / (values.size - 1).toFloat()
        fun y(v: Double) = (top + (1 - (v - min) / span) * h).toFloat()
        val path = Path()
        values.forEachIndexed { i, v -> if (i == 0) path.moveTo(x(i), y(v)) else path.lineTo(x(i), y(v)) }
        drawPath(path, scheme.primary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        values.forEachIndexed { i, v ->
            drawCircle(scheme.primary, radius = 4.dp.toPx(), center = Offset(x(i), y(v)))
            val value = measurer.measure(valueLabels[i], valueStyle)
            drawText(value, topLeft = Offset((x(i) - value.size.width / 2f).coerceIn(0f, size.width - value.size.width), y(v) - value.size.height - 4.dp.toPx()))
            val label = measurer.measure(labels[i], labelStyle)
            drawText(label, topLeft = Offset((x(i) - label.size.width / 2f).coerceIn(0f, size.width - label.size.width), size.height - bottom + 4.dp.toPx()))
        }
    }
}

// US69: purchases by client -------------------------------------------------------

@Composable
fun PurchasesSheet(
    clients: List<String>,
    summaryFor: (client: String, period: SummaryPeriod) -> PurchaseSummary?
) {
    val scheme = MaterialTheme.colorScheme
    var client by rememberSaveable { mutableStateOf(clients.firstOrNull().orEmpty()) }
    var period by rememberSaveable { mutableStateOf(SummaryPeriod.MONTH) }
    val summary = summaryFor(client, period)
    val number = NumberFormat.getIntegerInstance(appLocale())

    Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.sm)) {
        Column { Label(stringResource(R.string.inv_purchases_client)); ChipRow(clients, client) { client = it } }
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            SelectableChip(label = stringResource(R.string.inv_purchases_week), selected = period == SummaryPeriod.WEEK, onClick = { period = SummaryPeriod.WEEK })
            SelectableChip(label = stringResource(R.string.inv_purchases_month), selected = period == SummaryPeriod.MONTH, onClick = { period = SummaryPeriod.MONTH })
        }
        if (summary == null) {
            Text(stringResource(R.string.inv_purchases_none, client), style = FruitLogixTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            return@Column
        }
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            SummaryTile(stringResource(R.string.inv_purchases_kilos), stringResource(R.string.inv_purchases_kg, number.format(summary.kilos)), Modifier.weight(1f))
            SummaryTile(stringResource(R.string.inv_purchases_orders), summary.orders.toString(), Modifier.weight(1f))
        }
        SummaryTile(stringResource(R.string.inv_purchases_amount), formatPen(summary.amount), Modifier.fillMaxWidth())
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier.whiteCard().padding(FruitLogixTheme.spacing.inner)) {
        Text(label, style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        Text(value, style = FruitLogixTheme.typography.titleLarge, color = scheme.onSurface)
    }
}

@Composable
fun overdueText(days: Int): String = pluralStringResource(R.plurals.inv_days_overdue, days, days)
