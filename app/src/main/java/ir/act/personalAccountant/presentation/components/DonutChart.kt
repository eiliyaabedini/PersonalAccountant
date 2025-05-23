package ir.act.personalAccountant.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.act.personalAccountant.domain.model.TagExpenseData
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DonutChart(
    data: List<TagExpenseData>,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 16.dp,
    animationDuration: Int = 1000
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearEasing
        ), label = "donut_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    Canvas(
        modifier = modifier.size(size)
    ) {
        if (data.isNotEmpty()) {
            drawDonutChart(
                data = data,
                strokeWidth = strokeWidth.toPx(),
                animationProgress = animateSize
            )
        }
    }
}

private fun DrawScope.drawDonutChart(
    data: List<TagExpenseData>,
    strokeWidth: Float,
    animationProgress: Float
) {
    val radius = (size.minDimension - strokeWidth) / 2
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    var startAngle = -90f // Start from top
    
    data.forEach { segment ->
        val sweepAngle = (segment.percentage / 100f * 360f) * animationProgress
        
        drawArc(
            color = segment.color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            topLeft = androidx.compose.ui.geometry.Offset(
                centerX - radius,
                centerY - radius
            )
        )
        
        startAngle += sweepAngle
    }
}

