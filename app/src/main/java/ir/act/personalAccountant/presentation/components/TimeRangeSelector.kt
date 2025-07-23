package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T : Enum<T>> TimeRangeSelector(
    selectedTimeRange: T,
    timeRanges: Array<T>,
    getDisplayName: (T) -> String,
    onTimeRangeSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    showTitle: Boolean = false,
    title: String = "Time Range"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showTitle) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Time range buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeRanges.forEach { range ->
                    FilterChip(
                        onClick = { onTimeRangeSelected(range) },
                        label = {
                            Text(
                                text = getDisplayName(range),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = selectedTimeRange == range,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun <T : Enum<T>> SimpleTimeRangeSelector(
    selectedTimeRange: T,
    timeRanges: Array<T>,
    getDisplayName: (T) -> String,
    onTimeRangeSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        timeRanges.forEach { range ->
            FilterChip(
                onClick = { onTimeRangeSelected(range) },
                label = {
                    Text(
                        text = getDisplayName(range),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                selected = selectedTimeRange == range,
                modifier = Modifier.weight(1f)
            )
        }
    }
}