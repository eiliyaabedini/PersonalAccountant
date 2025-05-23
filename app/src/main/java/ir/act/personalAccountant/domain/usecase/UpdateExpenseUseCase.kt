package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import javax.inject.Inject

class UpdateExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense) {
        if (expense.amount <= 0) {
            throw IllegalArgumentException("Amount must be greater than zero")
        }
        if (expense.tag.isBlank()) {
            throw IllegalArgumentException("Tag cannot be empty")
        }
        repository.updateExpense(expense)
    }
}