package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expenseId: Long) {
        repository.deleteExpense(expenseId)
    }
}