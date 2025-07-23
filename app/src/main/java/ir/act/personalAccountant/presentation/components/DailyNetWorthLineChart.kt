package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.usecase.DailyNetWorth
import ir.act.personalAccountant.ui.theme.TextSecondary

@Composable
fun DailyNetWorthLineChart(
    dailyNetWorth: List<DailyNetWorth>,
    currencySettings: CurrencySettings,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showMinMax: Boolean = false,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    if (dailyNetWorth.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
        return
    }

    val values = dailyNetWorth.map { it.totalValue }
    val minValue = values.minOrNull() ?: 0.0
    val maxValue = values.maxOrNull() ?: 1.0
    val valueRange = maxValue - minValue

    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (isClickable && onClick != null) it.clickable { onClick() } else it },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showMinMax && values.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Min",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(minValue, currencySettings),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Max",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(maxValue, currencySettings),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                if (dailyNetWorth.size >= 1) { // Changed from >= 2 to >= 1 to show single data points
                    drawNetWorthLineChart(
                        dailyNetWorth = dailyNetWorth,
                        minValue = minValue,
                        maxValue = maxValue,
                        valueRange = valueRange
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawNetWorthLineChart(
    dailyNetWorth: List<DailyNetWorth>,
    minValue: Double,
    maxValue: Double,
    valueRange: Double
) {
    val width = size.width
    val height = size.height
    val padding = 20f

    // Calculate points
    val points = dailyNetWorth.mapIndexed { index, daily ->
        val x = if (dailyNetWorth.size == 1) {
            // Center single point
            width / 2f
        } else {
            padding + (index.toFloat() / (dailyNetWorth.size - 1)) * (width - 2 * padding)
        }
        val normalizedValue = if (valueRange > 0) {
            ((daily.totalValue - minValue) / valueRange).toFloat()
        } else {
            0.5f
        }
        val y = height - padding - normalizedValue * (height - 2 * padding)
        Offset(x, y)
    }

    // Draw grid lines
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val gridLineCount = 4

    for (i in 0..gridLineCount) {
        val y = padding + (i.toFloat() / gridLineCount) * (height - 2 * padding)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 1f
        )
    }

    // Draw zero line if applicable
    if (minValue < 0 && maxValue > 0) {
        val zeroY = height - padding - ((-minValue) / valueRange).toFloat() * (height - 2 * padding)
        drawLine(
            color = Color.Red.copy(alpha = 0.5f),
            start = Offset(padding, zeroY),
            end = Offset(width - padding, zeroY),
            strokeWidth = 2f
        )
    }

    // Draw line (only if there are multiple points)
    if (points.size > 1) {
        val path = Path()
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }

        drawPath(
            path = path,
            color = Color(0xFF2196F3),
            style = Stroke(width = 3f)
        )
    }

    // Draw data points
    points.forEach { point ->
        val radius = if (points.size == 1) 6f else 4f // Larger radius for single points
        drawCircle(
            color = Color(0xFF2196F3),
            radius = radius,
            center = point
        )
        drawCircle(
            color = Color.White,
            radius = radius - 2f,
            center = point
        )
    }
}