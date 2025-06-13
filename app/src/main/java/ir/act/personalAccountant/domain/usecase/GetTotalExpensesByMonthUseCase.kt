package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTotalExpensesByMonthUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(year: Int, month: Int): Flow<Double> {
        return repository.getTotalExpensesByMonth(year, month)
    }
}