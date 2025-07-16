package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.SyncProgress
import kotlinx.coroutines.flow.Flow

interface GoogleSheetsRepository {
    suspend fun createPersonalAccountantSpreadsheet(title: String): Result<String>
    suspend fun createMonthlySheet(spreadsheetId: String, monthYear: String): Result<Unit>
    suspend fun syncExpenses(expenses: List<Expense>): Result<Unit>
    suspend fun syncExpensesWithProgress(expenses: List<Expense>): Flow<SyncProgress>
    suspend fun uploadImageToDrive(imageBytes: ByteArray, fileName: String): Result<String>
    suspend fun isConnected(): Boolean
    suspend fun getSpreadsheetId(): String?
    suspend fun saveSpreadsheetId(spreadsheetId: String)
    suspend fun clearSyncState(expenseId: Long)
    suspend fun getLastSyncTimestamp(): Long?
}