package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.act.personalAccountant.R
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.BudgetStatus
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.ui.theme.BudgetGreenLight
import ir.act.personalAccountant.ui.theme.BudgetPurpleLight
import ir.act.personalAccountant.ui.theme.BudgetRedLight

@Composable
fun BudgetOwlDisplay(
    budgetData: BudgetData?,
    currencySettings: CurrencySettings,
    modifier: Modifier = Modifier
) {
    if (budgetData == null) {
        // No budget data available
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.mipmap.owl),
                contentDescription = "Owl",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Budget Not Configured",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Please configure your budget settings to see daily tracking",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    // Determine if today's expenses are over the daily budget
    val isOverBudget = if (budgetData.recommendedDailyExpenseBudget > 0) {
        budgetData.todayExpenses > budgetData.recommendedDailyExpenseBudget
    } else {
        // Fallback to overall budget status if no daily budget is set
        budgetData.budgetStatus == BudgetStatus.RED
    }

    val owlImage = if (isOverBudget) R.mipmap.sad_owl_big else R.mipmap.happy_owl_big
    val owlColor = if (isOverBudget) BudgetRedLight else BudgetGreenLight
    val statusText = if (isOverBudget) "Over Budget Today!" else "On Track Today!"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Content area with padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Owl with today's status
            Image(
                painter = painterResource(owlImage),
                contentDescription = if (isOverBudget) "Sad owl - over budget" else "Happy owl - within budget",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = owlColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Today's key information row with proper alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Today's expenses (left aligned)
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Today's Expenses",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(
                            budgetData.todayExpenses,
                            currencySettings
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Daily budget (center aligned, if available)
                if (budgetData.recommendedDailyExpenseBudget > 0) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Daily Budget",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(
                                budgetData.recommendedDailyExpenseBudget,
                                currencySettings
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Remaining (right aligned)
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        val remaining =
                            budgetData.recommendedDailyExpenseBudget - budgetData.todayExpenses
                        Text(
                            text = if (remaining >= 0) "Remaining" else "Over",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(
                                kotlin.math.abs(remaining),
                                currencySettings
                            ),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (remaining >= 0) BudgetGreenLight else BudgetRedLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Progress bar as divider using existing LayeredProgressBar component with clipping
        val totalSalary = budgetData.dailyIncome * budgetData.totalDaysInMonth

        // Calculate percentages (0-100) for LayeredProgressBar
        val incomePercentage = if (totalSalary > 0)
            ((budgetData.totalIncomeToDate / totalSalary) * 100.0).toFloat()
                .coerceIn(0f, 100f) else 0f
        val totalExpensePercentage = if (totalSalary > 0)
            (((budgetData.totalExpensesToDate + budgetData.totalRentToDate) / totalSalary) * 100.0).toFloat()
                .coerceIn(0f, 100f) else 0f
        val rentPercentage = if (totalSalary > 0)
            ((budgetData.totalRentToDate / totalSalary) * 100.0).toFloat()
                .coerceIn(0f, 100f) else 0f

        // Create progress layers
        val progressLayers = listOfNotNull(
            if (incomePercentage > 0f) ProgressLayer(
                value = incomePercentage,
                color = BudgetGreenLight.copy(alpha = 0.8f),
                label = "Income"
            ) else null,
            if (totalExpensePercentage > 0f) ProgressLayer(
                value = totalExpensePercentage,
                color = BudgetRedLight.copy(alpha = 0.8f),
                label = "Expenses"
            ) else null,
            if (rentPercentage > 0f) ProgressLayer(
                value = rentPercentage,
                color = BudgetPurpleLight,
                label = "Rent"
            ) else null
        )

        // Clipped container for LayeredProgressBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
        ) {
            LayeredProgressBar(
                layers = progressLayers,
                height = 16.dp,
                backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                cornerRadius = 40.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}