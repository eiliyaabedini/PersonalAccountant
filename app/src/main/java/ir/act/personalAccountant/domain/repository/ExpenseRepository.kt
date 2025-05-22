package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenses(): Flow<Double>
}