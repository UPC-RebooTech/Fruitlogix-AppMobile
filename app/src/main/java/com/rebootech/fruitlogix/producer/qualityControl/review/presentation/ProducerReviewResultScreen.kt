package com.rebootech.fruitlogix.producer.qualityControl.review.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionDraft
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionGrade
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionLot
import com.rebootech.fruitlogix.producer.qualityControl.inspection.domain.model.InspectionRules
import com.rebootech.fruitlogix.producer.qualityControl.inspection.presentation.InspectionSteps
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.format.formatRelativeDateTime
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Step 3: review the inspection and send it to the distributor.
 * Before sending: recipient, photos notice, highlighted report summary, measurements, notes.
 * After sending: confirmation and next steps (distributor decision, dispatch).
 */
@Composable
fun ProducerReviewResultScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    onEditInspection: () -> Unit,
    onBackToOrder: () -> Unit,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewResultViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val lot = state.lot
    val draft = state.draft

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.inner)
        ) {
            BackHeader(
                title = stringResource(if (state.isSent) R.string.review_sent_title else R.string.review_title),
                subtitle = lot?.let { stringResource(R.string.scan_order_context, it.orderId, it.clientName) },
                onBack = onNavigateBack
            )

            if (lot == null || draft == null) {
                Text(
                    text = stringResource(R.string.review_no_data),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
                return@Column
            }

            if (state.isSent) {
                SentContent(sentAt = state.sentAt!!)
            } else {
                InspectionSteps(activeIndex = 2)
                PhotosNotice()
                RecipientRow(distributor = state.distributorName, client = lot.clientName)
                ReportSummary(lot, draft)
                MeasurementsSection(draft)
                if (draft.notes.isNotBlank()) {
                    DetailSection(title = stringResource(R.string.review_section_notes)) {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(draft.notes, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (lot != null && draft != null) {
            Column(modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)) {
                if (state.isSent) {
                    PrimaryButton(
                        text = stringResource(R.string.review_back_to_order),
                        onClick = onBackToOrder,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextActionButton(
                        text = stringResource(R.string.order_detail_message),
                        onClick = onOpenChat,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    PrimaryButton(
                        text = stringResource(R.string.review_send),
                        onClick = viewModel::send,
                        enabled = !state.isSending,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            AppIcon(id = R.drawable.ic_send, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                        }
                    )
                    TextActionButton(
                        text = stringResource(R.string.review_edit),
                        onClick = onEditInspection,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            AppIcon(id = R.drawable.ic_edit, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotosNotice() {
    val colors = FruitLogixTheme.colors
    val context = LocalContext.current
    val soon = stringResource(R.string.review_photos_soon)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(colors.attentionContainer)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_camera, contentDescription = null, tint = colors.attentionContent, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(stringResource(R.string.review_photos_title), style = FruitLogixTheme.typography.titleSmall, color = colors.onAttentionContainer)
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(stringResource(R.string.review_photos_message), style = FruitLogixTheme.typography.bodyMedium, color = colors.onAttentionContainer)
        TextActionButton(
            text = stringResource(R.string.review_add_photos),
            onClick = { Toast.makeText(context, soon, Toast.LENGTH_SHORT).show() }
        )
    }
}

@Composable
private fun RecipientRow(distributor: String?, client: String) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Button)
            .background(scheme.surfaceContainerLow)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(id = R.drawable.ic_person, contentDescription = null, tint = scheme.primary, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(
            text = if (distributor != null) {
                stringResource(R.string.review_recipient, distributor, client)
            } else {
                stringResource(R.string.review_recipient_client, client)
            },
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurface
        )
    }
}

@Composable
private fun ReportSummary(lot: InspectionLot, draft: InspectionDraft) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val grade = InspectionRules.grade(draft.visualScore)
    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.review_report_label).uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = colors.onInverseSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.inspection_lot, lot.lotCode),
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.inversePrimary
                )
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(lot.productName, style = FruitLogixTheme.typography.titleLarge, color = scheme.inverseOnSurface)
            Text(
                text = "${stringResource(R.string.common_kg_value, lot.netWeightKg)} · ${stringResource(R.string.inspection_crates, lot.crates, lot.crateKg)}",
                style = FruitLogixTheme.typography.bodySmall,
                color = colors.onInverseSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.inspection_score_value, draft.visualScore),
                    style = FruitLogixTheme.extraTypography.readingMono,
                    color = scheme.inverseOnSurface
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
                StatusBadge(
                    text = stringResource(if (grade == InspectionGrade.A) R.string.inspection_grade_a else R.string.inspection_grade_b),
                    type = if (grade == InspectionGrade.A) StatusBadgeType.SUCCESS else StatusBadgeType.WARNING,
                    maxLines = 2
                )
            }
            val sizes = lot.calibers.filter { it.code in draft.calibers }.joinToString(", ") { it.code.toString() }
            if (sizes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.review_sizes, sizes),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = colors.onInverseSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MeasurementsSection(draft: InspectionDraft) {
    DetailSection(title = stringResource(R.string.review_section_measurements)) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            ReviewMetric(
                label = stringResource(R.string.inspection_core_temp),
                value = "${formatDecimal(InspectionRules.parse(draft.coreTemp) ?: 0.0)} °C",
                ok = InspectionRules.coreTempOk(draft.coreTemp),
                modifier = Modifier.weight(1f)
            )
            ReviewMetric(
                label = stringResource(R.string.inspection_moisture),
                value = "${formatDecimal(InspectionRules.parse(draft.moisture) ?: 0.0)}%",
                ok = InspectionRules.moistureOk(draft.moisture),
                modifier = Modifier.weight(1f)
            )
            ReviewMetric(
                label = stringResource(R.string.inspection_defects),
                value = "${formatDecimal(InspectionRules.parse(draft.defects) ?: 0.0)}%",
                ok = InspectionRules.defectsOk(draft.defects),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReviewMetric(label: String, value: String, ok: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Column(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Button)
            .background(if (ok) scheme.surfaceContainerLow else scheme.errorContainer)
            .padding(FruitLogixTheme.spacing.xs)
    ) {
        Text(label, style = FruitLogixTheme.typography.labelMedium, color = scheme.onSurfaceVariant, maxLines = 2)
        Text(value, style = FruitLogixTheme.extraTypography.dataMono, color = if (ok) scheme.onSurface else scheme.onErrorContainer)
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = if (ok) R.drawable.ic_check else R.drawable.ic_warning,
                contentDescription = null,
                tint = if (ok) colors.successContent else scheme.error,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(if (ok) R.string.report_detail_within_range else R.string.report_detail_out_of_range),
                style = FruitLogixTheme.typography.bodySmall,
                color = if (ok) colors.successContent else scheme.error,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun SentContent(sentAt: java.time.LocalDateTime) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.successContainer),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(id = R.drawable.ic_check, contentDescription = null, tint = colors.onSuccessContainer, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Text(
            text = stringResource(R.string.review_sent_message),
            style = FruitLogixTheme.typography.titleMedium,
            color = scheme.onSurface
        )
        Text(
            text = stringResource(R.string.review_sent_at, formatRelativeDateTime(sentAt)),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        StatusBadge(
            text = stringResource(R.string.producer_orders_quality_in_review),
            type = StatusBadgeType.WARNING,
            maxLines = 1,
            softWrap = false
        )
    }

    DetailSection(title = stringResource(R.string.review_section_next)) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        NextStep(
            title = stringResource(R.string.review_step_sent),
            detail = formatRelativeDateTime(sentAt),
            done = true,
            active = false
        )
        NextStep(
            title = stringResource(R.string.review_step_decision),
            detail = stringResource(R.string.review_step_decision_detail),
            done = false,
            active = true
        )
        NextStep(
            title = stringResource(R.string.review_step_dispatch),
            detail = stringResource(R.string.review_step_dispatch_detail),
            done = false,
            active = false
        )
    }
}

@Composable
private fun NextStep(title: String, detail: String, done: Boolean, active: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    Row(
        modifier = Modifier.padding(bottom = FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    when {
                        done -> colors.successContent
                        active -> colors.attentionContent
                        else -> scheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (done || active) {
                AppIcon(
                    id = if (done) R.drawable.ic_check else R.drawable.ic_schedule,
                    contentDescription = null,
                    tint = scheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column {
            Text(title, style = FruitLogixTheme.typography.titleSmall, color = if (done || active) scheme.onSurface else scheme.onSurfaceVariant)
            Text(detail, style = FruitLogixTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
}
