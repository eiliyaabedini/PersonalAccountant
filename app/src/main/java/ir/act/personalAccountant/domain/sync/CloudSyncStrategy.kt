package ir.act.personalAccountant.domain.sync

import ir.act.personalAccountant.domain.model.Expense

interface CloudSyncStrategy {
    suspend fun syncExpenseToCloud(expense: Expense, imageUris: List<String>): SyncResult<String>
    suspend fun syncAllPendingExpenses(expenses: List<Expense>): SyncResult<Unit>
    suspend fun downloadUserExpenses(userId: String): SyncResult<List<CloudExpense>>
    suspend fun deleteExpenseFromCloud(expenseId: String): SyncResult<Unit>
    suspend fun isUserAuthenticated(): Boolean
    suspend fun getCurrentUserId(): String?

    // New methods for differential sync
    suspend fun createExpensesInCloud(expenses: List<Expense>): SyncResult<Unit>
    suspend fun updateExpensesInCloud(expenses: List<Expense>): SyncResult<Unit>
    suspend fun deleteExpensesFromCloud(expenseIds: List<String>): SyncResult<Unit>
}