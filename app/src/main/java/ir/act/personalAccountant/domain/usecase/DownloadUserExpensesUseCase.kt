package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.sync.CloudExpense
import ir.act.personalAccountant.domain.sync.CloudSyncStrategy
import ir.act.personalAccountant.domain.sync.SyncResult
import javax.inject.Inject
import javax.inject.Singleton

interface DownloadUserExpensesUseCase {
    suspend operator fun invoke(): SyncResult<List<CloudExpense>>
}

@Singleton
class DownloadUserExpensesUseCaseImpl @Inject constructor(
    private val cloudSyncStrategy: CloudSyncStrategy,
    private val expenseRepository: ExpenseRepository
) : DownloadUserExpensesUseCase {

    override suspend operator fun invoke(): SyncResult<List<CloudExpense>> {
        return try {
            // Check authentication and get user ID
            if (!cloudSyncStrategy.isUserAuthenticated()) {
                return SyncResult.Error<List<CloudExpense>>("User not authenticated. Please sign in to download expenses.")
            }

            val userId = cloudSyncStrategy.getCurrentUserId()
                ?: return SyncResult.Error<List<CloudExpense>>("Could not get current user ID")

            // Download expenses from cloud
            val downloadResult = cloudSyncStrategy.downloadUserExpenses(userId)

            when (downloadResult) {
                is SyncResult.Success -> {
                    val cloudExpenses = downloadResult.data ?: emptyList()

                    // Merge with local expenses using last-write-wins strategy
                    val mergeResult = mergeWithLocalExpenses(cloudExpenses)

                    when (mergeResult) {
                        is SyncResult.Success -> SyncResult.Success(cloudExpenses)
                        is SyncResult.Error -> SyncResult.Error<List<CloudExpense>>(
                            mergeResult.message,
                            mergeResult.exception
                        )

                        else -> SyncResult.Error<List<CloudExpense>>("Unexpected merge result")
                    }
                }

                is SyncResult.Error -> SyncResult.Error<List<CloudExpense>>(
                    downloadResult.message,
                    downloadResult.exception
                )

                else -> SyncResult.Error<List<CloudExpense>>("Unexpected download result")
            }
        } catch (e: Exception) {
            SyncResult.Error<List<CloudExpense>>(
                "Failed to download user expenses: ${e.message}",
                e
            )
        }
    }

    private suspend fun mergeWithLocalExpenses(cloudExpenses: List<CloudExpense>): SyncResult<Unit> {
        return try {
            // For each cloud expense, compare with local data and update if cloud is newer
            cloudExpenses.forEach { cloudExpense ->
                val localExpense = expenseRepository.getExpenseById(cloudExpense.localId)

                // If local expense doesn't exist or cloud is newer, update/create local expense
                if (localExpense == null || cloudExpense.lastModified > cloudExpense.timestamp) {
                    val expenseToSave = mapCloudExpenseToExpense(cloudExpense)

                    if (localExpense == null) {
                        // Create new expense with the original ID from cloud
                        val newExpense = expenseToSave.copy(id = cloudExpense.localId)
                        expenseRepository.addExpense(newExpense)
                    } else {
                        // Update existing expense
                        val updatedExpense = expenseToSave.copy(id = cloudExpense.localId)
                        expenseRepository.updateExpense(updatedExpense)
                    }
                }
            }

            SyncResult.Success(Unit)
        } catch (e: Exception) {
            SyncResult.Error<Unit>(
                "Failed to merge cloud expenses with local data: ${e.message}",
                e
            )
        }
    }

    private fun mapCloudExpenseToExpense(cloudExpense: CloudExpense): Expense {
        return Expense(
            id = cloudExpense.localId,
            amount = cloudExpense.amount,
            timestamp = cloudExpense.timestamp,
            tag = cloudExpense.tag,
            imagePath = null, // Cloud images are handled separately via URLs
            destinationAmount = cloudExpense.destinationAmount,
            destinationCurrency = cloudExpense.destinationCurrency
        )
    }
}