package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.SyncProgress
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.repository.GoogleSheetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GoogleSheetsUseCase @Inject constructor(
    private val googleSheetsRepository: GoogleSheetsRepository,
    private val expenseRepository: ExpenseRepository
) {

    suspend fun setupUserSpreadsheet(userName: String): Result<String> {
        return googleSheetsRepository.createPersonalAccountantSpreadsheet(
            title = "Personal Accountant - $userName"
        )
    }

    suspend fun syncAllExpenses(): Result<Unit> {
        return try {
            val expenses = expenseRepository.getAllExpenses().first()
            // Use the intelligent sync method instead of the old one
            var lastProgress: SyncProgress? = null
            googleSheetsRepository.syncExpensesWithProgress(expenses).collect { progress ->
                lastProgress = progress
            }

            // Check if sync completed successfully
            if (lastProgress?.currentStep == ir.act.personalAccountant.domain.model.SyncStep.COMPLETED) {
                Result.success(Unit)
            } else if (lastProgress?.currentStep == ir.act.personalAccountant.domain.model.SyncStep.ERROR) {
                Result.failure(Exception(lastProgress?.error ?: "Sync failed"))
            } else {
                Result.failure(Exception("Sync incomplete"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllExpensesWithProgress(): Flow<SyncProgress> {
        val expenses = expenseRepository.getAllExpenses().first()
        return googleSheetsRepository.syncExpensesWithProgress(expenses)
    }

    suspend fun syncExpenseWithImage(
        expense: Expense,
        imageBytes: ByteArray?
    ): Result<Unit> {
        return try {
            val updatedExpense = if (imageBytes != null) {
                val imageUploadResult = googleSheetsRepository.uploadImageToDrive(
                    imageBytes = imageBytes,
                    fileName = "expense_${expense.id}_${System.currentTimeMillis()}.jpg"
                )

                if (imageUploadResult.isSuccess) {
                    expense.copy(imagePath = imageUploadResult.getOrNull())
                } else {
                    // Sync without image if upload fails
                    expense
                }
            } else {
                expense
            }

            googleSheetsRepository.syncExpenses(listOf(updatedExpense))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isConnectedToGoogleSheets(): Boolean {
        return googleSheetsRepository.isConnected()
    }

    suspend fun getSpreadsheetId(): String? {
        return googleSheetsRepository.getSpreadsheetId()
    }

    suspend fun getLastSyncTimestamp(): Long? {
        return googleSheetsRepository.getLastSyncTimestamp()
    }
}