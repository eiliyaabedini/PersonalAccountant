package ir.act.personalAccountant.presentation.components

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.zIndex
import ir.act.personalAccountant.domain.model.TagExpenseData
import java.text.NumberFormat
import java.util.*
import kotlin.math.*

@Composable
fun DonutChart(
    data: List<TagExpenseData>,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 16.dp,
    animationDuration: Int = 1000,
    enableTooltip: Boolean = true
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animateSize by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = LinearEasing
        ), label = "donut_animation"
    )
    
    var selectedSegment by remember { mutableStateOf<TagExpenseData?>(null) }
    var tooltipPosition by remember { mutableStateOf(Offset.Zero) }
    
    val density = LocalDensity.current

    LaunchedEffect(data) {
        animationPlayed = true
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val sizePx = with(density) { size.toPx() }
        
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(data, enableTooltip) {
                    if (enableTooltip) {
                        val canvasSize = this.size // IntSize in pixels
                        detectTapGestures(
                            onPress = { offset ->
                                Log.d("DonutChart", "Touch detected at: $offset")
                                Log.d("DonutChart", "Canvas size in px: ${canvasSize.width}x${canvasSize.height}")
                                Log.d("DonutChart", "Data count: ${data.size}")
                                
                                val segment = findSegmentAtPosition(
                                    offset = offset,
                                    data = data,
                                    canvasWidth = canvasSize.width.toFloat(),
                                    canvasHeight = canvasSize.height.toFloat(),
                                    strokeWidth = strokeWidth.toPx()
                                )
                                
                                Log.d("DonutChart", "Found segment: ${segment?.tag}")
                                selectedSegment = segment
                                tooltipPosition = offset
                                tryAwaitRelease()
                                Log.d("DonutChart", "Touch released")
                                selectedSegment = null
                            }
                        )
                    }
                }
        ) {
            if (data.isNotEmpty()) {
                drawDonutChart(
                    data = data,
                    strokeWidth = strokeWidth.toPx(),
                    animationProgress = animateSize,
                    selectedSegment = selectedSegment
                )
            }
        }
        
        // Tooltip
        selectedSegment?.let { segment ->
            DonutTooltip(
                segment = segment,
                tooltipOffset = tooltipPosition,
                modifier = Modifier.zIndex(1f)
            )
        }
    }
}

private fun DrawScope.drawDonutChart(
    data: List<TagExpenseData>,
    strokeWidth: Float,
    animationProgress: Float,
    selectedSegment: TagExpenseData? = null
) {
    val radius = (size.minDimension - strokeWidth) / 2
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    var startAngle = -90f // Start from top
    
    data.forEach { segment ->
        val sweepAngle = (segment.percentage / 100f * 360f) * animationProgress
        val isSelected = selectedSegment == segment
        
        drawArc(
            color = if (isSelected) {
                segment.color.copy(alpha = 1f)
            } else {
                segment.color.copy(alpha = if (selectedSegment != null) 0.6f else 1f)
            },
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = if (isSelected) strokeWidth * 1.2f else strokeWidth,
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

@Composable
private fun DonutTooltip(
    segment: TagExpenseData,
    tooltipOffset: Offset,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    // Convert touch position to dp
    val touchX = with(density) { tooltipOffset.x.toDp() }
    val touchY = with(density) { tooltipOffset.y.toDp() }
    
    // Tooltip dimensions (approximate)
    val tooltipWidth = 120.dp
    val tooltipHeight = 80.dp
    
    // Position tooltip to the right of the finger
    var tooltipX = touchX + 20.dp
    var tooltipY = touchY - 30.dp
    
    // Keep tooltip within screen bounds
    // If tooltip would go off right edge, show it to the left of finger
    if (tooltipX + tooltipWidth > screenWidth) {
        tooltipX = touchX - tooltipWidth - 20.dp
    }
    
    // Ensure tooltip doesn't go off left edge
    if (tooltipX < 0.dp) {
        tooltipX = 10.dp
    }
    
    // Ensure tooltip doesn't go off top edge
    if (tooltipY < 0.dp) {
        tooltipY = 10.dp
    }
    
    Card(
        modifier = modifier
            .offset(x = tooltipX, y = tooltipY)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = segment.tag,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${segment.percentage.toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatCurrency(segment.totalAmount),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun findSegmentAtPosition(
    offset: Offset,
    data: List<TagExpenseData>,
    canvasWidth: Float,
    canvasHeight: Float,
    strokeWidth: Float
): TagExpenseData? {
    val centerX = canvasWidth / 2
    val centerY = canvasHeight / 2
    val radius = (minOf(canvasWidth, canvasHeight) - strokeWidth) / 2
    
    Log.d("DonutChart", "Canvas size in px: ${canvasWidth}x${canvasHeight}")
    Log.d("DonutChart", "Center: ($centerX, $centerY), Radius: $radius")
    Log.d("DonutChart", "Touch offset: (${offset.x}, ${offset.y})")
    
    // Calculate distance from center
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val distance = sqrt(dx * dx + dy * dy)
    
    Log.d("DonutChart", "Distance from center: $distance")
    
    // Check if touch is within donut ring
    val innerRadius = radius - strokeWidth / 2
    val outerRadius = radius + strokeWidth / 2
    
    Log.d("DonutChart", "Inner radius: $innerRadius, Outer radius: $outerRadius")
    
    if (distance < innerRadius || distance > outerRadius) {
        Log.d("DonutChart", "Touch outside donut ring")
        return null
    }
    
    // Calculate angle
    var angle = atan2(dy, dx) * 180 / PI
    // Convert to 0-360 range starting from top (-90 degrees)
    angle = (angle + 90 + 360) % 360
    
    Log.d("DonutChart", "Calculated angle: $angle")
    
    // Find which segment this angle falls into
    var currentAngle = 0.0
    for ((index, segment) in data.withIndex()) {
        val segmentAngle = segment.percentage / 100.0 * 360.0
        Log.d("DonutChart", "Segment $index (${segment.tag}): ${currentAngle}° to ${currentAngle + segmentAngle}°")
        
        if (angle >= currentAngle && angle < currentAngle + segmentAngle) {
            Log.d("DonutChart", "Found matching segment: ${segment.tag}")
            return segment
        }
        currentAngle += segmentAngle
    }
    
    Log.d("DonutChart", "No segment found for angle $angle")
    return null
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
