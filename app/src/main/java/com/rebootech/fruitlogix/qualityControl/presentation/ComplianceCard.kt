package com.rebootech.fruitlogix.qualityControl.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.qualityControl.domain.model.ComplianceSummary
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme
import com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily
import com.rebootech.fruitlogix.shared.ui.theme.Spacing

@Composable
fun ComplianceCard(
    summary: ComplianceSummary,
    modifier: Modifier = Modifier.Companion
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm, vertical = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs),
        shape = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.surfaceDark,
            contentColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
        )
    ) {
        Column(modifier = Modifier.padding(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.compliance_title),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.titleMedium,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                        fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.compliance_subtitle),
                        style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                        color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar),
                    contentAlignment = Alignment.Center
                ) {
                    _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                        id = R.drawable.ic_thermostat,
                        contentDescription = null,
                        tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm))

            // Main Donut + Metrics layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart drawn with Canvas
                ComplianceDonutCanvas(
                    percent = summary.compliancePercent,
                    label = stringResource(R.string.compliance_label_compliant),
                    modifier = Modifier.size(105.dp)
                )

                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm))

                // Two Metrics Lines
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Transit Integrity
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                            id = R.drawable.ic_verified,
                            contentDescription = null,
                            tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.compliance_line_integrity),
                                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(
                                    R.string.compliance_line_integrity_sub,
                                    summary.transitIntegrityPercent
                                ),
                                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Metric 2: Tolerance Violations
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                            id = R.drawable.ic_shield_check,
                            contentDescription = null,
                            tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.compliance_line_breaches),
                                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(
                                    R.string.compliance_line_breaches_sub,
                                    summary.coldBreachesToday
                                ),
                                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                                color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.sm))

            // Bottom Amber Risk Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.shapes.Input)
                    .background(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning.copy(alpha = 0.18f))
                    .padding(horizontal = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                _root_ide_package_.com.rebootech.fruitlogix.shared.ui.components.AppIcon(
                    id = R.drawable.ic_warning,
                    contentDescription = null,
                    tint = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(_root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.Spacing.xs))
                Text(
                    text = stringResource(R.string.compliance_risk_label),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.warning,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(
                        R.string.compliance_risk_format,
                        summary.predictiveRiskThermalCount
                    ),
                    style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.bodySmall,
                    color = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// =============================================================================
// Donut Chart drawn with Compose Canvas
// =============================================================================
@Composable
private fun ComplianceDonutCanvas(
    percent: Int,
    label: String,
    modifier: Modifier = Modifier.Companion
) {
    val trackColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.appbar
    val progressColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.primary
    val textColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textOnDark
    val subtextColor = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.colors.textMuted

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 10.dp.toPx()
            val sweepAngle = 360f * (percent / 100f)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Center text layout inside donut
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percent%",
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixExtraTypography.kpiNumber,
                color = textColor,
                fontFamily = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.PoppinsFontFamily,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = _root_ide_package_.com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme.typography.labelSmall,
                color = subtextColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}