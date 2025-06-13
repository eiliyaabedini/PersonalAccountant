package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExpensesByTagAndMonthUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(tag: String, year: Int, month: Int): Flow<List<Expense>> {
        return repository.getExpensesByTagAndMonth(tag, year, month)
    }
}