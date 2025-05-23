package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.TagExpenseData
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetExpensesByTagUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<TagExpenseData>> {
        return repository.getAllExpenses().map { expenses ->
            if (expenses.isEmpty()) return@map emptyList()
            
            val groupedExpenses = expenses.groupBy { it.tag }
            val totalAmount = expenses.sumOf { it.amount }
            
            groupedExpenses.map { (tag, expenseList) ->
                val tagTotal = expenseList.sumOf { it.amount }
                val percentage = if (totalAmount > 0) (tagTotal / totalAmount * 100).toFloat() else 0f
                
                TagExpenseData(
                    tag = tag,
                    totalAmount = tagTotal,
                    percentage = percentage,
                    color = androidx.compose.ui.graphics.Color.Transparent // Color will be assigned in UI
                )
            }.sortedByDescending { it.totalAmount }
        }
    }
}