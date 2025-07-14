package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.TagExpenseData
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetExpensesByTagForMonthUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<List<TagExpenseData>> {
        return repository.getExpensesByMonth(year, month).map { expenses ->
            if (expenses.isEmpty()) return@map emptyList()
            
            // Get current day of month for calculating daily averages
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            
            val groupedExpenses = expenses.groupBy { it.tag }
            val totalAmount = expenses.sumOf { it.amount }
            
            groupedExpenses.map { (tag, expenseList) ->
                val tagTotal = expenseList.sumOf { it.amount }
                val percentage = if (totalAmount > 0) (tagTotal / totalAmount * 100).toFloat() else 0f
                val averageDaily = if (currentDay > 0) tagTotal / currentDay else 0.0
                
                TagExpenseData(
                    tag = tag,
                    totalAmount = tagTotal,
                    percentage = percentage,
                    averageDailyAmount = averageDaily,
                    color = androidx.compose.ui.graphics.Color.Transparent // Color will be assigned in UI
                )
            }.sortedByDescending { it.totalAmount }
        }
    }
}