package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpensesByMonthUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<List<Expense>> {
        return repository.getExpensesByMonth(year, month)
    }
}