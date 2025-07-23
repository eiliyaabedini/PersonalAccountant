package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.NetWorthSnapshot

@Composable
fun NetWorthLineChart(
    snapshots: List<NetWorthSnapshot>,
    currencySettings: CurrencySettings,
    modifier: Modifier = Modifier,
    title: String = "Net Worth Trend",
    showTitle: Boolean = true,
    height: androidx.compose.ui.unit.Dp = 200.dp,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null,
    showMinMax: Boolean = false
) {
    if (snapshots.isEmpty()) return

    val cardModifier = if (isClickable && onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Card(
        modifier = cardModifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showTitle) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isClickable) Arrangement.SpaceBetween else Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isClickable) {
                        Text(
                            text = "Tap to view details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Line Graph
            val sortedSnapshots = snapshots.sortedBy { it.calculatedAt }
            if (sortedSnapshots.size >= 2) {
                LineChart(
                    snapshots = sortedSnapshots,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (sortedSnapshots.size == 1) {
                            Text(
                                text = "Single Data Point",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = CurrencyFormatter.formatCurrency(
                                    sortedSnapshots[0].netWorth,
                                    currencySettings
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Need at least 2 data points to show trend",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Show value range
            if (showMinMax && sortedSnapshots.size >= 2) {
                Spacer(modifier = Modifier.height(8.dp))

                val minValue = sortedSnapshots.minOf { it.netWorth }
                val maxValue = sortedSnapshots.maxOf { it.netWorth }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Min: ${
                            CurrencyFormatter.formatCurrency(
                                minValue,
                                currencySettings
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Max: ${
                            CurrencyFormatter.formatCurrency(
                                maxValue,
                                currencySettings
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LineChart(
    snapshots: List<NetWorthSnapshot>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val pointColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        if (snapshots.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()

        // Calculate graph bounds
        val graphWidth = width - (padding * 2)
        val graphHeight = height - (padding * 2)

        // Calculate data ranges
        val minValue = snapshots.minOf { it.netWorth }
        val maxValue = snapshots.maxOf { it.netWorth }
        val valueRange = maxValue - minValue
        val adjustedRange = if (valueRange == 0.0) 1.0 else valueRange

        val minTime = snapshots.minOf { it.calculatedAt }
        val maxTime = snapshots.maxOf { it.calculatedAt }
        val timeRange = maxTime - minTime

        // Draw grid lines
        drawGridLines(gridColor, padding, graphWidth, graphHeight)

        // Calculate points
        val points = snapshots.map { snapshot ->
            val x =
                padding + (graphWidth * ((snapshot.calculatedAt - minTime).toDouble() / timeRange.toDouble())).toFloat()
            val y =
                padding + (graphHeight * (1.0 - ((snapshot.netWorth - minValue) / adjustedRange))).toFloat()
            Offset(x, y)
        }

        // Draw line
        drawLineGraph(points, lineColor)

        // Draw points
        points.forEach { point ->
            drawCircle(
                color = pointColor,
                radius = 4.dp.toPx(),
                center = point
            )
        }

        // Draw zero line if applicable
        if (minValue < 0 && maxValue > 0) {
            val zeroY =
                padding + (graphHeight * (1.0 - ((0.0 - minValue) / adjustedRange))).toFloat()
            drawLine(
                color = Color.Red.copy(alpha = 0.5f),
                start = Offset(padding, zeroY),
                end = Offset(padding + graphWidth, zeroY),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

private fun DrawScope.drawGridLines(
    gridColor: Color,
    padding: Float,
    graphWidth: Float,
    graphHeight: Float
) {
    // Vertical grid lines
    val verticalLines = 4
    for (i in 0..verticalLines) {
        val x = padding + (graphWidth / verticalLines) * i
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + graphHeight),
            strokeWidth = 1.dp.toPx()
        )
    }

    // Horizontal grid lines
    val horizontalLines = 4
    for (i in 0..horizontalLines) {
        val y = padding + (graphHeight / horizontalLines) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + graphWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawLineGraph(points: List<Offset>, lineColor: Color) {
    if (points.size < 2) return

    val path = Path()
    path.moveTo(points[0].x, points[0].y)

    for (i in 1 until points.size) {
        path.lineTo(points[i].x, points[i].y)
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(width = 3.dp.toPx())
    )
}