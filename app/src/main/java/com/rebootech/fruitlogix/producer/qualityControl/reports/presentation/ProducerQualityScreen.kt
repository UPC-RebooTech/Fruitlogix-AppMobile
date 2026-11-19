package com.rebootech.fruitlogix.producer.qualityControl.reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.producer.qualityControl.reports.data.QualityReportRepository
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.DEFECT_LIMIT_PCT
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityReportItem
import com.rebootech.fruitlogix.producer.qualityControl.reports.domain.QualityStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.KpiCard
import com.rebootech.fruitlogix.shared.ui.components.StatusBadge
import com.rebootech.fruitlogix.shared.ui.components.StatusBadgeType
import com.rebootech.fruitlogix.shared.ui.components.TextActionButton
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import java.text.NumberFormat

private enum class ReportFilter { ALL, APPROVED, PENDING, REJECTED }

// TODO(producer): take from the producer profile when it exists.
private const val FARM_NAME = "Fundo Santa Rosa"

/**
 * Producer quality reports.
 * Card rule: the two summary numbers are dark KPI tiles; reports are light list cards.
 */
@Composable
fun ProducerQualityScreen(
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable { mutableStateOf(ReportFilter.ALL) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val allReports = QualityReportRepository.reports
    val summary = QualityReportRepository.summary
    val scheme = MaterialTheme.colorScheme
    val spacing = FruitLogixTheme.spacing
    val locale = reportLocale()

    val filteredReports = allReports.filter { report ->
        val matchesFilter = when (selectedFilter) {
            ReportFilter.APPROVED -> report.status == QualityStatus.APPROVED
            ReportFilter.PENDING -> report.status == QualityStatus.PENDING
            ReportFilter.REJECTED -> report.status == QualityStatus.REJECTED
            ReportFilter.ALL -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
            report.orderNumber.contains(searchQuery, ignoreCase = true) ||
            report.productName.contains(searchQuery, ignoreCase = true) ||
            report.clientName.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(scheme.background),
        contentPadding = PaddingValues(start = spacing.sm, end = spacing.sm, top = spacing.sm, bottom = spacing.xl),
        verticalArrangement = Arrangement.spacedBy(spacing.inner)
    ) {
        item {
            Column {
                Text(
                    text = stringResource(R.string.producer_reports_title),
                    style = FruitLogixTheme.typography.headlineMedium,
                    color = scheme.onBackground
                )
                Text(
                    text = stringResource(R.string.producer_reports_subtitle, FARM_NAME),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        // Summary KPIs (dark)
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                KpiCard(
                    caption = stringResource(R.string.producer_reports_kpi_pass_rate),
                    value = "${formatDecimal(summary.passRatePct)}%",
                    icon = {
                        AppIcon(
                            id = R.drawable.ic_shield_check,
                            contentDescription = null,
                            tint = scheme.inversePrimary,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    },
                    footer = {
                        Text(
                            text = stringResource(
                                R.string.producer_reports_kpi_weekly_delta,
                                "+${formatDecimal(summary.weeklyDeltaPct)}%"
                            ),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = scheme.inversePrimary
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    caption = stringResource(R.string.producer_reports_kpi_sampled_today),
                    value = stringResource(
                        R.string.producer_orders_tile_volume_format,
                        NumberFormat.getNumberInstance(locale).format(summary.sampledTodayKg)
                    ),
                    icon = {
                        AppIcon(
                            id = R.drawable.ic_inventory,
                            contentDescription = null,
                            tint = scheme.inversePrimary,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    },
                    footer = {
                        Text(
                            text = pluralStringResource(
                                R.plurals.producer_reports_kpi_crates,
                                summary.cratesInspectedToday,
                                summary.cratesInspectedToday
                            ),
                            style = FruitLogixTheme.typography.bodySmall,
                            color = FruitLogixTheme.colors.onInverseSurfaceVariant
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = stringResource(R.string.producer_reports_search_placeholder),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    AppIcon(
                        id = R.drawable.ic_search,
                        contentDescription = null,
                        modifier = Modifier.size(FruitLogixTheme.sizing.icon)
                    )
                },
                textStyle = FruitLogixTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                shape = FruitLogixTheme.shapes.Input,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = scheme.surfaceContainerLowest,
                    unfocusedContainerColor = scheme.surfaceContainerLow,
                    focusedBorderColor = scheme.primary,
                    unfocusedBorderColor = scheme.outlineVariant,
                    focusedPlaceholderColor = scheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = scheme.onSurfaceVariant,
                    focusedLeadingIconColor = scheme.onSurfaceVariant,
                    unfocusedLeadingIconColor = scheme.onSurfaceVariant
                ),
                singleLine = true
            )
        }

        // Status filters
        item {
            val counts = mapOf(
                ReportFilter.ALL to allReports.size,
                ReportFilter.APPROVED to allReports.count { it.status == QualityStatus.APPROVED },
                ReportFilter.PENDING to allReports.count { it.status == QualityStatus.PENDING },
                ReportFilter.REJECTED to allReports.count { it.status == QualityStatus.REJECTED }
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                items(ReportFilter.entries) { filter ->
                    val labelRes = when (filter) {
                        ReportFilter.ALL -> R.string.producer_reports_filter_all
                        ReportFilter.APPROVED -> R.string.producer_reports_filter_approved
                        ReportFilter.PENDING -> R.string.producer_reports_filter_pending
                        ReportFilter.REJECTED -> R.string.producer_reports_filter_rejected
                    }
                    ReportFilterChip(
                        label = stringResource(labelRes, counts[filter] ?: 0),
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }
        }

        if (filteredReports.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.producer_reports_empty),
                    style = FruitLogixTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = spacing.md)
                )
            }
        } else {
            items(filteredReports, key = { it.id }) { report ->
                QualityReportCard(
                    report = report,
                    onClick = { onNavigateToDetail(report.id) }
                )
            }
        }
    }
}

@Composable
private fun ReportFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = FruitLogixTheme.sizing.minTouch)
            .clip(FruitLogixTheme.shapes.Pill)
            .background(if (selected) scheme.secondary else scheme.surfaceContainerHigh)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = if (selected) scheme.onSecondary else scheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Quality report (light list card). */
@Composable
fun QualityReportCard(report: QualityReportItem, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors

    val (statusRes, badgeType) = when (report.status) {
        QualityStatus.APPROVED -> R.string.producer_reports_status_approved to StatusBadgeType.SUCCESS
        QualityStatus.PENDING -> R.string.producer_reports_status_pending to StatusBadgeType.WARNING
        QualityStatus.REJECTED -> R.string.producer_reports_status_rejected to StatusBadgeType.DANGER
    }
    val actionRes = when (report.status) {
        QualityStatus.APPROVED -> R.string.producer_reports_action_certificate
        QualityStatus.PENDING -> R.string.producer_reports_action_review
        QualityStatus.REJECTED -> R.string.producer_reports_action_audit
    }

    FruitLogixCard(
        variant = CardVariant.White,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            // Order + status, then client and time on their own lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.orderNumber,
                    style = FruitLogixTheme.typography.titleMedium,
                    color = scheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(
                    text = stringResource(statusRes),
                    type = badgeType,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Text(
                text = report.clientName,
                style = FruitLogixTheme.typography.titleMedium,
                color = scheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatInspectedAt(report.inspectedAt)} · ${report.locationInfo}",
                style = FruitLogixTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            // Product + lot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FruitLogixTheme.shapes.Button)
                    .background(scheme.surfaceContainerLow)
                    .padding(FruitLogixTheme.spacing.inner),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_leaf,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = "${report.productName} · ${stringResource(R.string.common_kg_value, report.weightKg)}",
                    style = FruitLogixTheme.typography.titleSmall,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.producer_reports_lot, report.lotCode),
                    style = FruitLogixTheme.extraTypography.dataMonoSmall,
                    color = scheme.onSecondaryContainer,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .clip(FruitLogixTheme.shapes.Tag)
                        .background(scheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Quarantine alert (derived from the metrics)
            if (report.isQuarantined) {
                Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FruitLogixTheme.shapes.Button)
                        .background(scheme.errorContainer)
                        .padding(FruitLogixTheme.spacing.inner),
                    verticalAlignment = Alignment.Top
                ) {
                    AppIcon(
                        id = R.drawable.ic_alert,
                        contentDescription = null,
                        tint = scheme.error,
                        modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                    )
                    Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                    Text(
                        text = stringResource(
                            R.string.producer_reports_quarantine_alert,
                            formatDecimal(report.defectsPercentage),
                            formatDecimal(DEFECT_LIMIT_PCT)
                        ),
                        style = FruitLogixTheme.typography.bodySmall,
                        color = scheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            // Metrics 2x2 (larger than 4 in a row, easier to read in the field)
            val defectsHigh = report.defectsPercentage > DEFECT_LIMIT_PCT
            Column(verticalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                    MetricBox(
                        label = stringResource(R.string.producer_reports_metric_visual),
                        value = "${formatDecimal(report.visualScore)} / ${formatDecimal(5.0)}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = stringResource(R.string.producer_reports_metric_defects),
                        value = "${formatDecimal(report.defectsPercentage)}%",
                        container = if (defectsHigh) scheme.errorContainer else null,
                        content = if (defectsHigh) scheme.onErrorContainer else null,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(FruitLogixTheme.spacing.xs)) {
                    MetricBox(
                        label = stringResource(R.string.producer_reports_metric_core_temp),
                        value = "${formatDecimal(report.coreTemp)} °C",
                        container = colors.coldContainer,
                        content = colors.onColdContainer,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = stringResource(R.string.producer_reports_metric_humidity),
                        value = "${report.humidityPercentage}%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))
            HorizontalDivider(color = scheme.outlineVariant)
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))

            // Inspector + action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    id = R.drawable.ic_person,
                    contentDescription = null,
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xxs))
                Text(
                    text = stringResource(R.string.producer_reports_inspector, report.inspectorName),
                    style = FruitLogixTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextActionButton(
                    text = stringResource(actionRes),
                    onClick = onClick,
                    trailingIcon = {
                        AppIcon(
                            id = R.drawable.ic_chevron_right,
                            contentDescription = null,
                            modifier = Modifier.size(FruitLogixTheme.sizing.iconSm)
                        )
                    }
                )
            }
        }
    }
}

/** Metric tile: label on top, value in the data font. */
@Composable
fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    container: Color? = null,
    content: Color? = null
) {
    val scheme = MaterialTheme.colorScheme
    val fg = content ?: scheme.onSurface
    Column(
        modifier = modifier
            .clip(FruitLogixTheme.shapes.Button)
            .background(container ?: scheme.surfaceContainerLow)
            .padding(horizontal = FruitLogixTheme.spacing.inner, vertical = FruitLogixTheme.spacing.xs)
    ) {
        Text(
            text = label,
            style = FruitLogixTheme.typography.labelMedium,
            color = if (content != null) fg else scheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = FruitLogixTheme.extraTypography.dataMono,
            color = fg
        )
    }
}
