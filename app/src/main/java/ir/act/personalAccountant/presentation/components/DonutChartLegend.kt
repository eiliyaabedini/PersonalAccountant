package ir.act.personalAccountant.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.TagExpenseData

@Composable
fun DonutChartLegend(
    data: List<TagExpenseData>,
    modifier: Modifier = Modifier,
    showTopCountOnly: Int = 3,
    currencySettings: CurrencySettings = CurrencySettings.DEFAULT,
    onTagClick: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    val topItems = data.take(showTopCountOnly)
    val remainingItems = if (data.size > showTopCountOnly) {
        data.drop(showTopCountOnly)
    } else {
        emptyList()
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Always show top items
        topItems.forEach { item ->
            LegendItem(
                color = item.color,
                label = item.tag,
                amount = item.totalAmount,
                percentage = item.percentage,
                currencySettings = currencySettings,
                onClick = { onTagClick?.invoke(item.tag) }
            )
        }
        
        // Animated visibility for remaining items
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 300)
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                remainingItems.forEach { item ->
                    LegendItem(
                        color = item.color,
                        label = item.tag,
                        amount = item.totalAmount,
                        percentage = item.percentage,
                        currencySettings = currencySettings,
                        onClick = { onTagClick?.invoke(item.tag) }
                    )
                }
            }
        }
        
        if (data.size > showTopCountOnly) {
            ExpandCollapseButton(
                isExpanded = isExpanded,
                remainingCount = data.size - showTopCountOnly,
                onClick = { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    amount: Double,
    percentage: Float,
    currencySettings: CurrencySettings,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = androidx.compose.ui.graphics.Color.Black,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = androidx.compose.ui.graphics.Color.Gray
        )
        
        Text(
            text = CurrencyFormatter.formatCurrency(amount, currencySettings),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.ui.graphics.Color.Black
        )
    }
}

@Composable
private fun ExpandCollapseButton(
    isExpanded: Boolean,
    remainingCount: Int,
    onClick: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "arrow_rotation"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isExpanded) {
                "Show Less"
            } else {
                "Show $remainingCount More"
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(16.dp)
                .rotate(arrowRotation)
        )
    }
}

