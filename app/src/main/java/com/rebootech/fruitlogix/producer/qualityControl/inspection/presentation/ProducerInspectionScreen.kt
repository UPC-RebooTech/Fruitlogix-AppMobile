package com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionGrade
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionRules
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.SelectableChip
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Quality inspection form.
 * - Steps (Prepare ✓ · Inspect · Report) and a highlighted (dark) lot summary.
 * - Light sections: visual score + sizes, measurements with accepted ranges, notes.
 * - "Review report" is enabled only when every measurement is in range; "Save on phone" works offline.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProducerInspectionScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onReviewReport: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspectionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val savedMessage = stringResource(R.string.inspection_saved_offline)

    LaunchedEffect(state.savedOffline) {
        if (state.savedOffline) Toast.makeText(context, savedMessage, Toast.LENGTH_LONG).show()
    }

    val lot = state.lot
    val draft = state.draft

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
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            BackHeader(
                title = stringResource(R.string.inspection_title),
                subtitle = lot?.let { stringResource(R.string.scan_order_context, it.orderId, it.clientName) },
                onBack = onNavigateBack
            )

            if (lot == null || draft == null) {
                Text(
                    text = stringResource(R.string.order_detail_not_found),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                return@Column
            }

            InspectionSteps(activeIndex = 1)

            if (lot.isReinspection) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FruitLogixTheme.shapes.Button)
                        .background(scheme.errorContainer)
                        .padding(spacing.inner),
                    verticalAlignment = Alignment.Top
                ) {
                    AppIcon(id = R.drawable.ic_alert, contentDescription = null, tint = scheme.error, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
                    Spacer(modifier = Modifier.width(spacing.xs))
                    Column {
                        Text(stringResource(R.string.inspection_reinspection), style = FruitLogixTheme.typography.titleSmall, color = scheme.onErrorContainer)
                        Text(lot.previousRejectionReason.orEmpty(), style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
                    }
                }
            }

            LotCard(lot)

            // Visual quality and sizes
            DetailSection(title = stringResource(R.string.inspection_section_visual)) {
                Spacer(modifier = Modifier.height(spacing.inner))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.inspection_visual_score),
                        style = FruitLogixTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = stringResource(R.string.inspection_score_value, draft.visualScore),
                        style = FruitLogixTheme.extraTypography.dataMono,
                        color = scheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    (1..5).forEach { score ->
                        ScoreButton(
                            score = score,
                            selected = score <= draft.visualScore,
                            onClick = { viewModel.update { it.copy(visualScore = score) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(spacing.xs))
                val grade = InspectionRules.grade(draft.visualScore)
                StatusBadge(
                    text = stringResource(if (grade == InspectionGrade.A) R.string.inspection_grade_a else R.string.inspection_grade_b),
                    type = if (grade == InspectionGrade.A) StatusBadgeType.SUCCESS else StatusBadgeType.WARNING,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(spacing.md))
                Text(
                    text = stringResource(R.string.inspection_calibers),
                    style = FruitLogixTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(spacing.xs)
                ) {
                    lot.calibers.forEach { caliber ->
                        SelectableChip(
                            label = stringResource(R.string.inspection_caliber, caliber.code, caliber.approxGrams),
                            selected = caliber.code in draft.calibers,
                            onClick = { viewModel.toggleCaliber(caliber.code) }
                        )
                    }
                }
                if (!state.calibersOk) {
                    Spacer(modifier = Modifier.height(spacing.xxs))
                    Text(
                        text = stringResource(R.string.inspection_calibers_required),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.error
                    )
                }
            }

            // Measurements
            DetailSection(title = stringResource(R.string.inspection_section_measurements)) {
                if (state.issues > 0) {
                    Spacer(modifier = Modifier.height(spacing.xxs))
                    StatusBadge(
                        text = pluralStringResource(R.plurals.inspection_issues, state.issues, state.issues),
                        type = StatusBadgeType.DANGER,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Spacer(modifier = Modifier.height(spacing.inner))
                MeasurementField(
                    label = stringResource(R.string.inspection_core_temp),
                    hint = stringResource(
                        R.string.inspection_range,
                        formatDecimal(InspectionRules.CORE_TEMP_RANGE_C.start),
                        formatDecimal(InspectionRules.CORE_TEMP_RANGE_C.endInclusive),
                        "°C"
                    ),
                    unit = "°C",
                    value = draft.coreTemp,
                    ok = state.coreTempOk,
                    onValueChange = { v -> viewModel.update { it.copy(coreTemp = v) } }
                )
                Spacer(modifier = Modifier.height(spacing.inner))
                MeasurementField(
                    label = stringResource(R.string.inspection_moisture),
                    hint = stringResource(
                        R.string.inspection_range,
                        formatDecimal(InspectionRules.MOISTURE_RANGE_PCT.start),
                        formatDecimal(InspectionRules.MOISTURE_RANGE_PCT.endInclusive),
                        "%"
                    ),
                    unit = "%",
                    value = draft.moisture,
                    ok = state.moistureOk,
                    onValueChange = { v -> viewModel.update { it.copy(moisture = v) } }
                )
                Spacer(modifier = Modifier.height(spacing.inner))
                MeasurementField(
                    label = stringResource(R.string.inspection_defects),
                    hint = stringResource(R.string.inspection_max, formatDecimal(InspectionRules.DEFECTS_MAX_PCT), "%"),
                    unit = "%",
                    value = draft.defects,
                    ok = state.defectsOk,
                    onValueChange = { v -> viewModel.update { it.copy(defects = v) } }
                )
            }

            // Notes
            DetailSection(title = stringResource(R.string.inspection_section_notes)) {
                Spacer(modifier = Modifier.height(spacing.inner))
                OutlinedTextField(
                    value = draft.notes,
                    onValueChange = { v -> viewModel.update { it.copy(notes = v.take(InspectionRules.NOTES_MAX)) } },
                    placeholder = { Text(stringResource(R.string.inspection_notes_hint)) },
                    minLines = 3,
                    textStyle = FruitLogixTheme.typography.bodyLarge,
                    supportingText = {
                        Text(stringResource(R.string.inspection_notes_count, draft.notes.length, InspectionRules.NOTES_MAX))
                    },
                    shape = FruitLogixTheme.shapes.Input,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = scheme.outline),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (lot != null && draft != null) {
            Column(modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)) {
                if (!state.canSubmit) {
                    Text(
                        text = stringResource(R.string.inspection_fix_before_submit),
                        style = FruitLogixTheme.typography.labelMedium,
                        color = scheme.error,
                        modifier = Modifier.padding(bottom = spacing.xxs)
                    )
                }
                PrimaryButton(
                    text = stringResource(R.string.inspection_review),
                    onClick = { if (viewModel.submit()) onReviewReport() },
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth()
                )
                TextActionButton(
                    text = stringResource(R.string.inspection_save_offline),
                    onClick = viewModel::saveOffline,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        AppIcon(id = R.drawable.ic_download, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                    }
                )
            }
        }
    }
}

@Composable
/** Prepare · Inspect · Report steps. [activeIndex] is 0-based; earlier steps show as done. */
fun InspectionSteps(activeIndex: Int) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val steps = listOf(
        stringResource(R.string.inspection_step_prepare),
        stringResource(R.string.inspection_step_inspect),
        stringResource(R.string.inspection_step_report)
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { index, title ->
            val done = index < activeIndex
            val active = index == activeIndex
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            done -> colors.successContent
                            active -> scheme.primaryContainer
                            else -> scheme.surfaceContainerHigh
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (done) {
                    AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        text = "${index + 1}",
                        style = FruitLogixTheme.typography.labelMedium,
                        color = if (active) scheme.onPrimaryContainer else scheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = FruitLogixTheme.typography.labelMedium,
                color = if (active) scheme.onSurface else scheme.onSurfaceVariant
            )
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                        .height(2.dp)
                        .background(if (done) colors.successContent else scheme.outlineVariant)
                )
            }
        }
    }
}

@Composable
private fun LotCard(lot: InspectionLot) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Text(
                text = stringResource(R.string.inspection_lot, lot.lotCode),
                style = FruitLogixTheme.extraTypography.dataMonoSmall,
                color = scheme.inversePrimary
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(lot.productName, style = FruitLogixTheme.typography.titleLarge, color = scheme.inverseOnSurface)
                    Text(
                        stringResource(R.string.inspection_crates, lot.crates, lot.crateKg),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = colors.onInverseSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.common_kg_value, lot.netWeightKg),
                    style = FruitLogixTheme.extraTypography.readingMono,
                    color = scheme.inverseOnSurface
                )
            }
        }
    }
}

/** 56dp score button; filled up to the selected score. */
@Composable
private fun ScoreButton(score: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val description = stringResource(R.string.inspection_cd_score, score)
    Box(
        modifier = modifier
            .height(FruitLogixTheme.sizing.fieldTouch)
            .clip(FruitLogixTheme.shapes.Button)
            .background(if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = selected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score",
            style = FruitLogixTheme.typography.titleMedium,
            color = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
        )
    }
}

/** Numeric measurement with its accepted range; turns red with a message when out of range. */
@Composable
private fun MeasurementField(
    label: String,
    hint: String,
    unit: String,
    value: String,
    ok: Boolean,
    onValueChange: (String) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onValueChange(v.filter { it.isDigit() || it == '.' || it == ',' }) },
        label = { Text(label) },
        suffix = { Text(unit, style = FruitLogixTheme.typography.titleSmall) },
        isError = !ok,
        supportingText = {
            Text(if (ok) hint else "${stringResource(R.string.inspection_out_of_range)} · $hint")
        },
        singleLine = true,
        textStyle = FruitLogixTheme.extraTypography.dataMono,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = FruitLogixTheme.shapes.Input,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = scheme.outline,
            focusedContainerColor = scheme.surfaceContainerLowest,
            unfocusedContainerColor = scheme.surfaceContainerLowest
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.fieldTouch)
    )
}
