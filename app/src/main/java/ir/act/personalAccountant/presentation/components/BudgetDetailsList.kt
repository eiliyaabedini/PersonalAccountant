package ir.act.personalAccountant.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ir.act.personalAccountant.R
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.BudgetStatus
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.ui.theme.BudgetGreenLight
import ir.act.personalAccountant.ui.theme.BudgetOrangeLight
import ir.act.personalAccountant.ui.theme.BudgetPurpleLight
import ir.act.personalAccountant.ui.theme.BudgetRedLight

@Composable
fun BudgetDetailsList(
    budgetData: BudgetData?,
    currencySettings: CurrencySettings,
    modifier: Modifier = Modifier
) {
    if (budgetData == null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_budget_data_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.configure_budget_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.budget_details_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Budget status indicator
                val (statusColor, statusText) = when (budgetData.budgetStatus) {
                    BudgetStatus.GOOD -> BudgetGreenLight to stringResource(R.string.budget_status_good)
                    BudgetStatus.MIDDLE -> BudgetOrangeLight to stringResource(R.string.budget_status_moderate)
                    BudgetStatus.RED -> BudgetRedLight to stringResource(R.string.budget_status_over)
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Daily Budget Section
                if (budgetData.recommendedDailyExpenseBudget > 0) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        BudgetDetailItem(
                            title = stringResource(R.string.budget_title),
                            isHeader = true
                        )
                    }

                    item {
                        BudgetDetailItem(
                            title = stringResource(R.string.recommended_daily_budget_title),
                            value = CurrencyFormatter.formatCurrency(
                                budgetData.recommendedDailyExpenseBudget,
                                currencySettings
                            ),
                            valueColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Monthly Overview Section
                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.monthly_overview_title),
                        isHeader = true
                    )
                }

                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.total_income_so_far_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.totalIncomeToDate,
                            currencySettings
                        ),
                        valueColor = BudgetGreenLight
                    )
                }

                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.total_expenses_so_far_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.totalExpensesToDate,
                            currencySettings
                        ),
                        valueColor = BudgetRedLight
                    )
                }

                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.total_rent_expenses_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.totalRentToDate + budgetData.totalExpensesToDate,
                            currencySettings
                        ),
                        valueColor = BudgetPurpleLight
                    )
                }

                item {
                    val remainingBudget =
                        budgetData.totalIncomeToDate - budgetData.totalExpensesToDate - budgetData.totalRentToDate
                    val remainingColor = when (budgetData.budgetStatus) {
                        BudgetStatus.GOOD -> BudgetGreenLight
                        BudgetStatus.MIDDLE -> BudgetOrangeLight
                        BudgetStatus.RED -> BudgetRedLight
                    }
                    BudgetDetailItem(
                        title = stringResource(R.string.remaining_title),
                        value = CurrencyFormatter.formatCurrency(remainingBudget, currencySettings),
                        valueColor = remainingColor,
                        isImportant = true
                    )
                }

                // Daily Breakdown Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetDetailItem(
                        title = stringResource(R.string.daily_breakdown_title),
                        isHeader = true
                    )
                }

                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.daily_income_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.dailyIncome,
                            currencySettings
                        ),
                        valueColor = BudgetGreenLight
                    )
                }

                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.daily_rent_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.dailyRent,
                            currencySettings
                        ),
                        valueColor = BudgetPurpleLight
                    )
                }

                item {
                    BudgetDetailItem(
                        title = stringResource(R.string.average_daily_expenses_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.averageDailyExpenses,
                            currencySettings
                        ),
                        valueColor = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Projections Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BudgetDetailItem(
                        title = stringResource(R.string.projections_title),
                        isHeader = true
                    )
                }

                item {
                    val projectionColor = when (budgetData.budgetStatus) {
                        BudgetStatus.GOOD -> BudgetGreenLight
                        BudgetStatus.MIDDLE -> BudgetOrangeLight
                        BudgetStatus.RED -> BudgetRedLight
                    }
                    BudgetDetailItem(
                        title = stringResource(R.string.estimated_end_month_balance_title),
                        value = CurrencyFormatter.formatCurrency(
                            budgetData.estimatedEndOfMonthBalance,
                            currencySettings
                        ),
                        valueColor = projectionColor,
                        isImportant = true
                    )
                }

                // Add padding at bottom for FAB
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
private fun BudgetDetailItem(
    title: String,
    value: String = "",
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isHeader: Boolean = false,
    isImportant: Boolean = false
) {
    if (isHeader) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = if (isImportant) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = if (isImportant) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = if (isImportant) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}