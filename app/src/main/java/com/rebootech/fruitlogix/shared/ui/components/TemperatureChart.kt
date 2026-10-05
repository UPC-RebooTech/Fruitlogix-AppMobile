package com.rebootech.fruitlogix.shared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TemperaturePoint(
    val timeLabel: String,
    val celsius: Float,
    val isProjected: Boolean = false
)

@Composable
fun TemperatureChart(
    points: List<TemperaturePoint>,
    minThreshold: Float,
    maxThreshold: Float,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxTemp = (points.maxOf { it.celsius }.coerceAtLeast(maxThreshold) + 1.0f)
    val minTemp = (points.minOf { it.celsius }.coerceAtMost(minThreshold) - 0.5f)
    val tempRange = (maxTemp - minTemp).coerceAtLeast(1.0f)

    val lineColor = MaterialTheme.colorScheme.error
    val projectedColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
    val gridLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val minThresholdColor = MaterialTheme.colorScheme.primary
    val maxThresholdColor = MaterialTheme.colorScheme.error

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Legend Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Min: ${minThreshold}°C",
                style = MaterialTheme.typography.labelSmall,
                color = minThresholdColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Max Threshold: ${maxThreshold}°C",
                style = MaterialTheme.typography.labelSmall,
                color = maxThresholdColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "LIVE TELEMETRY",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chart Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val width = size.width
            val height = size.height

            val paddingLeft = 32.dp.toPx()
            val paddingRight = 16.dp.toPx()
            val paddingTop = 16.dp.toPx()
            val paddingBottom = 24.dp.toPx()

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            fun getY(temp: Float): Float {
                val normalized = (temp - minTemp) / tempRange
                return paddingTop + chartHeight * (1f - normalized)
            }

            fun getX(index: Int): Float {
                val step = chartWidth / (points.size - 1).coerceAtLeast(1)
                return paddingLeft + index * step
            }

            // Draw Max Threshold Line
            val maxThresholdY = getY(maxThreshold)
            drawLine(
                color = maxThresholdColor,
                start = Offset(paddingLeft, maxThresholdY),
                end = Offset(width - paddingRight, maxThresholdY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Draw Min Threshold Line
            val minThresholdY = getY(minThreshold)
            drawLine(
                color = minThresholdColor,
                start = Offset(paddingLeft, minThresholdY),
                end = Offset(width - paddingRight, minThresholdY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Separate actual points and projected points
            val actualPoints = points.filter { !it.isProjected }
            val projectedPoints = points.filter { it.isProjected }

            // Draw Actual Line Path
            if (actualPoints.isNotEmpty()) {
                val actualPath = Path().apply {
                    val startX = getX(0)
                    val startY = getY(actualPoints[0].celsius)
                    moveTo(startX, startY)
                    for (i in 1 until actualPoints.size) {
                        val x = getX(i)
                        val y = getY(actualPoints[i].celsius)
                        lineTo(x, y)
                    }
                }
                drawPath(
                    path = actualPath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw dots for actual points
                for (i in actualPoints.indices) {
                    val x = getX(i)
                    val y = getY(actualPoints[i].celsius)
                    drawCircle(
                        color = lineColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            // Draw Projected Dotted Path
            if (actualPoints.isNotEmpty() && projectedPoints.isNotEmpty()) {
                val lastActualIndex = actualPoints.size - 1
                val projectedPath = Path().apply {
                    moveTo(getX(lastActualIndex), getY(actualPoints.last().celsius))
                    for (i in projectedPoints.indices) {
                        val actualIndex = lastActualIndex + 1 + i
                        moveTo(getX(actualIndex - 1), getY(if (i == 0) actualPoints.last().celsius else projectedPoints[i - 1].celsius))
                        lineTo(getX(actualIndex), getY(projectedPoints[i].celsius))
                    }
                }
                drawPath(
                    path = projectedPath,
                    color = projectedColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )

                // Draw dots for projected points
                for (i in projectedPoints.indices) {
                    val index = lastActualIndex + 1 + i
                    val x = getX(index)
                    val y = getY(projectedPoints[i].celsius)
                    drawCircle(
                        color = projectedColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.timeLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (pt.isProjected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (pt.isProjected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
