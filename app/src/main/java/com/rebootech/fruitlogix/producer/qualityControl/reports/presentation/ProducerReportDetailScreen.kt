package com.rebootech.fruitlogix.producer.qualityControl.reports.presentation

import android.content.Intent
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.reports.data.QualityReportRepository
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.CORE_TEMP_MAX_C
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.DEFECT_LIMIT_PCT
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.HUMIDITY_RANGE_PCT
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.VISUAL_SCORE_MIN
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.BackHeader
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.DetailSection
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.HeaderAction
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.time.format.DateTimeFormatter

private enum class StepState { DONE, ACTIVE, FAILED, PENDING }

/**
 * Quality report detail.
 * - Highlighted (dark) summary: status, order, product, lot, inspector.
 * - Light sections: metrics with their accepted range, inspector notes, lot trajectory.
 * - Share sends a text summary through Android's share sheet.
 */
@Composable
fun ProducerReportDetailScreen(
    reportId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing

    val cleanId = reportId.removePrefix("#").removePrefix("ORD-").trim()
    val report = remember(reportId) {
        QualityReportRepository.reports.find {
            it.id.removePrefix("#").trim() == cleanId || it.orderNumber.removePrefix("#").trim() == cleanId
        } ?: QualityReportRepository.reports.first()
    }

    val (statusRes, badgeType) = statusBadge(report.status)
    val shareText = stringResource(
        R.string.report_detail_share_text,
        report.orderNumber,
        report.productName,
        report.weightKg,
        report.lotCode,
        stringResource(statusRes),
        formatDecimal(report.visualScore),
        formatDecimal(report.defectsPercentage),
        formatDecimal(report.coreTemp),
        report.humidityPercentage
    )
    val shareTitle = stringResource(R.string.report_detail_cd_share)
    val share: () -> Unit = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, shareTitle))
    }
    val exportSoon = stringResource(R.string.report_detail_export_soon)

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
                title = stringResource(R.string.report_detail_title, report.orderNumber),
                subtitle = report.clientName,
                onBack = onNavigateBack,
                actions = {
                    HeaderAction(
                        iconRes = R.drawable.ic_share,
                        contentDescription = shareTitle,
                        onClick = share
                    )
                }
            )

            SummaryCard(report = report, statusRes = statusRes, badgeType = badgeType)
            MetricsSection(report)

            DetailSection(title = stringResource(R.string.report_detail_section_notes)) {
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = report.inspectorNotes ?: stringResource(R.string.report_detail_no_notes),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }

            TrajectorySection(report)
        }

        Column(modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs)) {
            PrimaryButton(
                text = stringResource(R.string.report_detail_share),
                onClick = share,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    AppIcon(id = R.drawable.ic_share, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                }
            )
            TextActionButton(
                text = stringResource(R.string.report_detail_export_pdf),
                onClick = { Toast.makeText(context, exportSoon, Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    AppIcon(id = R.drawable.ic_download, contentDescription = null, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                }
            )
        }
    }
}

@Composable
private fun SummaryCard(report: QualityReportItem, statusRes: Int, badgeType: StatusBadgeType) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = stringResource(statusRes), type = badgeType, maxLines = 1, softWrap = false)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.producer_reports_lot, report.lotCode),
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.inversePrimary
                )
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            Text(
                text = report.productName,
                style = FruitLogixTheme.typography.titleLarge,
                color = scheme.inverseOnSurface
            )
            Text(
                text = stringResource(
                    R.string.report_detail_weight,
                    report.weightKg,
                    pluralStringResource(R.plurals.report_detail_crates, report.cratesCount, report.cratesCount)
                ),
                style = FruitLogixTheme.extraTypography.dataMono,
                color = colors.onInverseSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(
                text = stringResource(R.string.report_detail_inspected_by, formatInspectedAt(report.inspectedAt), report.inspectorName),
                style = FruitLogixTheme.typography.bodySmall,
                color = colors.onInverseSurfaceVariant
            )
            Text(
                text = report.locationInfo,
                style = FruitLogixTheme.typography.bodySmall,
                color = colors.onInverseSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricsSection(report: QualityReportItem) {
    DetailSection(title = stringResource(R.string.report_detail_section_metrics)) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
            Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                MetricResult(
                    label = stringResource(R.string.producer_reports_metric_visual),
                    value = "${formatDecimal(report.visualScore)} / ${formatDecimal(5.0)}",
                    limit = stringResource(R.string.report_detail_limit_min, formatDecimal(VISUAL_SCORE_MIN)),
                    ok = report.visualOk,
                    modifier = Modifier.weight(1f)
                )
                MetricResult(
                    label = stringResource(R.string.producer_reports_metric_defects),
                    value = "${formatDecimal(report.defectsPercentage)}%",
                    limit = stringResource(R.string.report_detail_limit_max, "${formatDecimal(DEFECT_LIMIT_PCT)}%"),
                    ok = report.defectsOk,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                MetricResult(
                    label = stringResource(R.string.producer_reports_metric_core_temp),
                    value = "${formatDecimal(report.coreTemp)} °C",
                    limit = stringResource(R.string.report_detail_limit_max, "${formatDecimal(CORE_TEMP_MAX_C)} °C"),
                    ok = report.coreTempOk,
                    modifier = Modifier.weight(1f)
                )
                MetricResult(
                    label = stringResource(R.string.producer_reports_metric_humidity),
                    value = "${report.humidityPercentage}%",
                    limit = stringResource(
                        R.string.report_detail_limit_range,
                        HUMIDITY_RANGE_PCT.first.toString(),
                        "${HUMIDITY_RANGE_PCT.last}%"
                    ),
                    ok = report.humidityOk,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Metric tile: value + accepted limit + within/out of range (icon and text, not only color). */
@Composable
private fun MetricResult(
    label: String,
    value: String,
    limit: String,
    ok: Boolean,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val container = if (ok) scheme.surfaceContainerLow else scheme.errorContainer
    val content = if (ok) scheme.onSurface else scheme.onErrorContainer
    Column(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Button)
            .background(container)
            .padding(FruitLogixTheme.spacing.inner)
    ) {
        Text(text = label, style = FruitLogixTheme.typography.labelMedium, color = if (ok) scheme.onSurfaceVariant else content)
        Text(text = value, style = FruitLogixTheme.extraTypography.dataMono, color = content)
        Text(text = limit, style = FruitLogixTheme.typography.bodySmall, color = if (ok) scheme.onSurfaceVariant else content)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(
                id = if (ok) R.drawable.ic_check else R.drawable.ic_warning,
                contentDescription = null,
                tint = if (ok) colors.successContent else scheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(if (ok) R.string.report_detail_within_range else R.string.report_detail_out_of_range),
                style = FruitLogixTheme.typography.labelMedium,
                color = if (ok) colors.successContent else scheme.error
            )
        }
    }
}

@Composable
private fun TrajectorySection(report: QualityReportItem) {
    val locale = appLocale()
    val dateFormat = DateTimeFormatter.ofPattern("d MMM", locale)
    val decisionState = when (report.status) {
        QualityStatus.PENDING -> StepState.ACTIVE
        QualityStatus.APPROVED -> StepState.DONE
        QualityStatus.REJECTED -> StepState.FAILED
    }
    DetailSection(title = stringResource(R.string.report_detail_section_trajectory)) {
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
        TrajectoryStep(
            title = stringResource(R.string.report_detail_step_harvested),
            detail = report.harvestedOn?.let { stringResource(R.string.report_detail_step_done, it.format(dateFormat)) },
            state = StepState.DONE,
            isLast = false
        )
        TrajectoryStep(
            title = stringResource(R.string.report_detail_step_inspected),
            detail = stringResource(R.string.report_detail_step_done, formatInspectedAt(report.inspectedAt)),
            state = StepState.DONE,
            isLast = false
        )
        TrajectoryStep(
            title = stringResource(R.string.report_detail_step_decision),
            detail = stringResource(
                when (decisionState) {
                    StepState.ACTIVE -> R.string.report_detail_step_waiting
                    StepState.DONE -> R.string.report_detail_step_approved
                    else -> R.string.report_detail_step_rejected
                }
            ),
            state = decisionState,
            isLast = false
        )
        TrajectoryStep(
            title = stringResource(R.string.report_detail_step_dispatch),
            detail = stringResource(R.string.report_detail_step_pending),
            state = StepState.PENDING,
            isLast = true
        )
    }
}

@Composable
private fun TrajectoryStep(title: String, detail: String?, state: StepState, isLast: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    val (dotColor, iconRes) = when (state) {
        StepState.DONE -> colors.successContent to R.drawable.ic_check
        StepState.ACTIVE -> colors.attentionContent to R.drawable.ic_schedule
        StepState.FAILED -> scheme.error to R.drawable.ic_close
        StepState.PENDING -> scheme.outlineVariant to null
    }
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                iconRes?.let {
                    AppIcon(id = it, contentDescription = null, tint = scheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(scheme.outlineVariant)
                )
            }
        }
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.inner))
        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = title,
                style = FruitLogixTheme.typography.titleSmall,
                color = if (state == StepState.PENDING) scheme.onSurfaceVariant else scheme.onSurface
            )
            detail?.let {
                Text(
                    text = it,
                    style = FruitLogixTheme.typography.bodySmall,
                    color = if (state == StepState.FAILED) scheme.error else scheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun statusBadge(status: QualityStatus): Pair<Int, StatusBadgeType> = when (status) {
    QualityStatus.APPROVED -> R.string.producer_reports_status_approved to StatusBadgeType.SUCCESS
    QualityStatus.PENDING -> R.string.producer_reports_status_pending to StatusBadgeType.WARNING
    QualityStatus.REJECTED -> R.string.producer_reports_status_rejected to StatusBadgeType.DANGER
}
