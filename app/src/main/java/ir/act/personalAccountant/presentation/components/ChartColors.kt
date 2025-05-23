package ir.act.personalAccountant.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ir.act.personalAccountant.domain.model.TagExpenseData

@Composable
fun getChartColors(): List<Color> {
    return listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
}

@Composable
fun assignColorsToTagData(data: List<TagExpenseData>): List<TagExpenseData> {
    val colors = getChartColors()
    return data.mapIndexed { index, item ->
        item.copy(color = colors[index % colors.size])
    }
}