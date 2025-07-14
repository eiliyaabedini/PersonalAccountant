package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ProgressLayer(
    val value: Float, // 0f to 100f
    val color: Color,
    val label: String = ""
)

@Composable
fun LayeredProgressBar(
    layers: List<ProgressLayer>,
    modifier: Modifier = Modifier,
    height: Dp = 24.dp,
    backgroundColor: Color = Color.Gray.copy(alpha = 0.3f),
    cornerRadius: Dp = 12.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = cornerRadius.toPx()
        
        // Draw background
        drawRoundRect(
            color = backgroundColor,
            topLeft = Offset.Zero,
            size = Size(canvasWidth, canvasHeight),
            cornerRadius = CornerRadius(radius)
        )
        
        // Sort layers by value in descending order to draw largest first
        val sortedLayers = layers.sortedByDescending { it.value }
        
        // Draw each layer
        sortedLayers.forEach { layer ->
            val layerWidth = (canvasWidth * (layer.value / 100f)).coerceIn(0f, canvasWidth)
            
            if (layerWidth > 0f) {
                drawProgressLayer(
                    color = layer.color,
                    width = layerWidth,
                    height = canvasHeight,
                    cornerRadius = radius
                )
            }
        }
    }
}

private fun DrawScope.drawProgressLayer(
    color: Color,
    width: Float,
    height: Float,
    cornerRadius: Float
) {
    // Always draw with full corner radius for all layers
    drawRoundRect(
        color = color,
        topLeft = Offset.Zero,
        size = Size(width, height),
        cornerRadius = CornerRadius(cornerRadius)
    )
}