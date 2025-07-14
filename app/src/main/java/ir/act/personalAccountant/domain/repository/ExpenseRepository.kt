package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun addExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: Long)
    suspend fun getExpenseById(id: Long): Expense?
    fun getAllExpenses(): Flow<List<Expense>>
    fun getTotalExpenses(): Flow<Double>
    fun getAllTagsWithCount(): Flow<List<TagWithCount>>
    fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>>
    fun getTotalExpensesByMonth(year: Int, month: Int): Flow<Double>
    fun getExpensesByTagAndMonth(tag: String, year: Int, month: Int): Flow<List<Expense>>
    suspend fun mergeTags(oldTags: List<String>, newTag: String): Int
}