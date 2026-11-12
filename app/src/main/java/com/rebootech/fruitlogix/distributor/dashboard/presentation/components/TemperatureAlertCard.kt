package com.rebootech.fruitlogix.distributor.dashboard.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.R
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlert
import com.rebootech.fruitlogix.distributor.logisticsMonitoring.domain.model.PredictiveAlertStatus
import com.rebootech.fruitlogix.shared.ui.components.AppIcon
import com.rebootech.fruitlogix.shared.ui.components.CardVariant
import com.rebootech.fruitlogix.shared.ui.components.FruitLogixCard
import com.rebootech.fruitlogix.shared.ui.components.PrimaryButton
import com.rebootech.fruitlogix.shared.ui.format.appLocale
import com.rebootech.fruitlogix.shared.ui.format.formatDecimal
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Highlighted card of the Home: the reefer most at risk.
 * - Predicted breach: dark card with the trend chart.
 * - Already above the limit: red card.
 * - Everything in range: a small green line (no big card, nothing to do).
 */
@Composable
fun TemperatureAlertCard(
    alert: PredictiveAlert,
    onReviewUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (alert.status) {
        PredictiveAlertStatus.NOMINAL -> AllInRange(modifier)
        PredictiveAlertStatus.BREACHED -> Breached(alert, onReviewUnit, modifier)
        PredictiveAlertStatus.PREDICTIVE -> Predicted(alert, onReviewUnit, modifier)
    }
}

@Composable
private fun celsius(value: Double) = "${formatDecimal(value)} °C"

@Composable
private fun AllInRange(modifier: Modifier) {
    val colors = FruitLogixTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.Card)
            .background(colors.successContainer)
            .padding(FruitLogixTheme.spacing.inner),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(id = R.drawable.ic_verified, contentDescription = null, tint = colors.successContent, modifier = Modifier.size(FruitLogixTheme.sizing.icon))
        Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
        Text(stringResource(R.string.dist_home_alert_ok), style = FruitLogixTheme.typography.titleSmall, color = colors.onSuccessContainer)
    }
}

@Composable
private fun Breached(alert: PredictiveAlert, onReviewUnit: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FruitLogixTheme.shapes.HeroCard)
            .background(scheme.errorContainer)
            .padding(FruitLogixTheme.spacing.sm)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(id = R.drawable.ic_warning, contentDescription = null, tint = scheme.error, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
            Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
            Text(
                text = stringResource(R.string.dist_home_alert_breached_label).uppercase(),
                style = FruitLogixTheme.typography.labelSmall,
                color = scheme.error
            )
        }
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
        Text(
            text = stringResource(R.string.dist_home_alert_breached_title, alert.unitId, celsius(alert.thresholdCelsius)),
            style = FruitLogixTheme.typography.titleLarge,
            color = scheme.onErrorContainer
        )
        Text(alert.cargoDescription, style = FruitLogixTheme.typography.bodyMedium, color = scheme.onErrorContainer)
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xxs))
        Text(
            text = stringResource(R.string.dist_home_alert_breached_detail, celsius(alert.currentCelsius)),
            style = FruitLogixTheme.typography.bodyMedium,
            color = scheme.onErrorContainer
        )
        Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
        PrimaryButton(
            text = stringResource(R.string.dist_home_alert_review),
            onClick = onReviewUnit,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Predicted(alert: PredictiveAlert, onReviewUnit: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val colors = FruitLogixTheme.colors
    FruitLogixCard(
        variant = CardVariant.Dark,
        shape = FruitLogixTheme.shapes.HeroCard,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(FruitLogixTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(id = R.drawable.ic_thermostat, contentDescription = null, tint = colors.attentionContainer, modifier = Modifier.size(FruitLogixTheme.sizing.iconSm))
                Spacer(modifier = Modifier.width(FruitLogixTheme.spacing.xs))
                Text(
                    text = stringResource(R.string.dist_home_alert_label).uppercase(),
                    style = FruitLogixTheme.typography.labelSmall,
                    color = colors.attentionContainer
                )
            }
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.xs))
            Text(
                text = stringResource(
                    R.string.dist_home_alert_title,
                    alert.unitId,
                    celsius(alert.thresholdCelsius),
                    alert.minutesToBreach
                ),
                style = FruitLogixTheme.typography.titleLarge,
                color = scheme.inverseOnSurface
            )
            Text(alert.cargoDescription, style = FruitLogixTheme.typography.bodyMedium, color = colors.onInverseSurfaceVariant)
            Text(
                text = stringResource(
                    R.string.dist_home_alert_rate,
                    "${String.format(appLocale(), "%.2f", alert.ratePerMinute)} °C",
                    celsius(alert.currentCelsius)
                ),
                style = FruitLogixTheme.typography.bodySmall,
                color = colors.onInverseSurfaceVariant
            )
            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.inner))

            val history = alert.readings.maxOfOrNull { it.minutesAgo } ?: 0
            val chartDescription = stringResource(R.string.dist_home_chart_cd, history, celsius(alert.thresholdCelsius))
            TemperatureTrendChart(
                readings = alert.readings,
                currentCelsius = alert.currentCelsius,
                thresholdCelsius = alert.thresholdCelsius,
                minutesToBreach = alert.minutesToBreach,
                nowLabel = stringResource(R.string.dist_home_chart_now, celsius(alert.currentCelsius)),
                limitLabel = stringResource(R.string.dist_home_chart_limit, celsius(alert.thresholdCelsius)),
                startCaption = stringResource(R.string.dist_home_chart_minus, history),
                nowCaption = stringResource(R.string.dist_home_chart_now_caption),
                breachCaption = stringResource(R.string.dist_home_chart_plus, alert.minutesToBreach),
                lineColor = scheme.inversePrimary,
                limitColor = colors.attentionContainer,
                breachColor = scheme.error,
                captionColor = colors.onInverseSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .semantics { contentDescription = chartDescription }
            )

            Spacer(modifier = Modifier.height(FruitLogixTheme.spacing.sm))
            PrimaryButton(
                text = stringResource(R.string.dist_home_alert_review),
                onClick = onReviewUnit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Past readings (solid line), projection (dashed) and the point where it crosses the limit. */
@Composable
private fun TemperatureTrendChart(
    readings: List<TemperatureReading>,
    currentCelsius: Double,
    thresholdCelsius: Double,
    minutesToBreach: Int,
    nowLabel: String,
    limitLabel: String,
    startCaption: String,
    nowCaption: String,
    breachCaption: String,
    lineColor: Color,
    limitColor: Color,
    breachColor: Color,
    captionColor: Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = FruitLogixTheme.typography.labelSmall.copy(color = limitColor)
    val nowStyle = FruitLogixTheme.extraTypography.dataMonoSmall.copy(color = lineColor)
    val breachStyle = FruitLogixTheme.extraTypography.dataMonoSmall.copy(color = breachColor)
    val captionStyle = FruitLogixTheme.typography.labelSmall.copy(color = captionColor)
    val history = readings.maxOfOrNull { it.minutesAgo } ?: 45

    Canvas(modifier = modifier) {
        if (readings.isEmpty()) return@Canvas
        val totalMinutes = (history + minutesToBreach).toDouble()
        if (totalMinutes <= 0.0) return@Canvas

        val axisHeight = 20.dp.toPx()
        val top = 22.dp.toPx()
        val left = 8.dp.toPx()
        val right = 56.dp.toPx()
        val width = size.width - left - right
        val height = size.height - top - axisHeight

        val minTemp = (readings.minOfOrNull { it.celsius } ?: currentCelsius) - 0.4
        val maxTemp = thresholdCelsius + 0.6
        val range = maxTemp - minTemp
        if (range <= 0.0) return@Canvas

        fun x(minutesAgo: Double) = (left + ((history - minutesAgo) / totalMinutes) * width).toFloat()
        fun y(temp: Double) = (top + (1.0 - (temp - minTemp) / range) * height).toFloat()

        val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)

        // Limit line and its label
        val limitY = y(thresholdCelsius)
        drawLine(limitColor, Offset(left, limitY), Offset(size.width - 8.dp.toPx(), limitY), strokeWidth = 2f, pathEffect = dash)
        val limitLayout = textMeasurer.measure(limitLabel, labelStyle)
        drawText(limitLayout, topLeft = Offset(size.width - 8.dp.toPx() - limitLayout.size.width, limitY - limitLayout.size.height - 3.dp.toPx()))

        // History
        val path = Path()
        readings.sortedByDescending { it.minutesAgo }.forEachIndexed { index, r ->
            val px = x(r.minutesAgo.toDouble())
            val py = y(r.celsius)
            if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Now
        val nowX = x(0.0)
        val nowY = y(currentCelsius)
        drawCircle(lineColor, radius = 5.dp.toPx(), center = Offset(nowX, nowY))
        val nowLayout = textMeasurer.measure(nowLabel, nowStyle)
        drawText(nowLayout, topLeft = Offset((nowX - nowLayout.size.width / 2f).coerceAtLeast(0f), nowY + 6.dp.toPx()))

        // Projection to the breach point
        val breachX = x(-minutesToBreach.toDouble())
        drawLine(lineColor, Offset(nowX, nowY), Offset(breachX, limitY), strokeWidth = 3.dp.toPx(), pathEffect = dash, cap = StrokeCap.Round)
        drawCircle(breachColor, radius = 5.dp.toPx(), center = Offset(breachX, limitY))
        val breachLayout = textMeasurer.measure(breachCaption, breachStyle)
        drawText(breachLayout, topLeft = Offset(breachX + 6.dp.toPx(), limitY - breachLayout.size.height / 2f))

        // Time axis
        val captionY = size.height - axisHeight + 4.dp.toPx()
        drawText(textMeasurer.measure(startCaption, captionStyle), topLeft = Offset(left, captionY))
        val nowCaptionLayout = textMeasurer.measure(nowCaption, captionStyle)
        drawText(nowCaptionLayout, topLeft = Offset(nowX - nowCaptionLayout.size.width / 2f, captionY))
    }
}
