package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.data.local.model.TagWithCount
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense)
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenses(): Flow<Double>
    fun getAllTagsWithCount(): Flow<List<TagWithCount>>
}