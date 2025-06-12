package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(amount: Double, tag: String, timestamp: Long = System.currentTimeMillis()): Long {
        if (amount <= 0) {
            throw IllegalArgumentException("Amount must be greater than 0")
        }
        
        val expense = Expense(
            amount = amount,
            timestamp = timestamp,
            tag = tag
        )
        
        return repository.addExpense(expense)
    }
}