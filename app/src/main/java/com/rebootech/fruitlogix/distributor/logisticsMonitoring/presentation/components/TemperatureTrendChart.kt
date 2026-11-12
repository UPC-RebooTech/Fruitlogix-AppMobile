package com.rebootech.fruitlogix.distributor.logisticsMonitoring.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.rebootech.fruitlogix.distributor.infrastructureIot.domain.model.TemperatureReading
import com.rebootech.fruitlogix.shared.ui.theme.FruitLogixTheme

/**
 * Reefer temperature over time on a dark card: shaded safe band between [min] and [max],
 * the readings line, and points outside the band marked in [alertColor].
 * Labels are passed already formatted (language and decimal separator).
 */
@Composable
fun TemperatureTrendChart(
    readings: List<TemperatureReading>,
    min: Double,
    max: Double,
    minLabel: String,
    maxLabel: String,
    startLabel: String,
    endLabel: String,
    lineColor: Color,
    bandColor: Color,
    alertColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = FruitLogixTheme.extraTypography.dataMonoSmall.copy(color = labelColor)

    Canvas(modifier = modifier) {
        if (readings.size < 2) return@Canvas
        val left = 44.dp.toPx()
        val right = 8.dp.toPx()
        val top = 8.dp.toPx()
        val bottom = 20.dp.toPx()
        val width = size.width - left - right
        val height = size.height - top - bottom

        val span = (max - min).coerceAtLeast(0.5)
        val low = minOf(readings.minOf { it.celsius }, min) - span * 0.25
        val high = maxOf(readings.maxOf { it.celsius }, max) + span * 0.25
        val oldest = readings.maxOf { it.minutesAgo }.coerceAtLeast(1)

        fun x(minutesAgo: Int) = left + (1f - minutesAgo / oldest.toFloat()) * width
        fun y(t: Double) = (top + (1.0 - (t - low) / (high - low)) * height).toFloat()

        // Safe band
        drawRect(bandColor, topLeft = Offset(left, y(max)), size = Size(width, y(min) - y(max)))
        val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        listOf(max to maxLabel, min to minLabel).forEach { (value, label) ->
            drawLine(labelColor, Offset(left, y(value)), Offset(left + width, y(value)), strokeWidth = 1.5f, pathEffect = dash)
            val layout = measurer.measure(label, labelStyle)
            drawText(layout, topLeft = Offset(0f, y(value) - layout.size.height / 2f))
        }

        // Readings
        val sorted = readings.sortedByDescending { it.minutesAgo }
        val path = Path()
        sorted.forEachIndexed { i, r -> if (i == 0) path.moveTo(x(r.minutesAgo), y(r.celsius)) else path.lineTo(x(r.minutesAgo), y(r.celsius)) }
        drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        sorted.filter { it.celsius < min || it.celsius > max }.forEach {
            drawCircle(alertColor, radius = 5.dp.toPx(), center = Offset(x(it.minutesAgo), y(it.celsius)))
        }
        val last = sorted.last()
        drawCircle(lineColor, radius = 6.dp.toPx(), center = Offset(x(last.minutesAgo), y(last.celsius)))

        // Time axis
        val axisY = size.height - bottom + 4.dp.toPx()
        drawText(measurer.measure(startLabel, labelStyle), topLeft = Offset(left, axisY))
        val endLayout = measurer.measure(endLabel, labelStyle)
        drawText(endLayout, topLeft = Offset(left + width - endLayout.size.width, axisY))
    }
}
