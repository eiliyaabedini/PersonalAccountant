package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.sync.CloudSyncStrategy
import ir.act.personalAccountant.domain.sync.SyncResult
import javax.inject.Inject
import javax.inject.Singleton

enum class SyncOperation {
    CREATE, UPDATE, DELETE
}

interface SyncExpenseUseCase {
    suspend operator fun invoke(
        expense: Expense,
        imageUris: List<String>,
        operation: SyncOperation
    ): SyncResult<String>
}

@Singleton
class SyncExpenseUseCaseImpl @Inject constructor(
    private val cloudSyncStrategy: CloudSyncStrategy,
    private val notificationPreferences: NotificationPreferences
) : SyncExpenseUseCase {

    override suspend operator fun invoke(
        expense: Expense,
        imageUris: List<String>,
        operation: SyncOperation
    ): SyncResult<String> {
        return try {
            // Check if cloud sync is enabled
            if (!notificationPreferences.isCloudSyncEnabled) {
                // Skip sync if cloud sync is disabled (silent success)
                return SyncResult.Success(null)
            }

            // Check if user is authenticated
            if (!cloudSyncStrategy.isUserAuthenticated()) {
                // Skip sync if user is not authenticated (silent success)
                return SyncResult.Success(null)
            }

            // Handle different sync operations
            when (operation) {
                SyncOperation.CREATE, SyncOperation.UPDATE -> {
                    // For create and update operations, sync the expense to cloud
                    cloudSyncStrategy.syncExpenseToCloud(expense, imageUris)
                }

                SyncOperation.DELETE -> {
                    // For delete operations, remove the expense from cloud
                    val deleteResult =
                        cloudSyncStrategy.deleteExpenseFromCloud(expense.id.toString())
                    when (deleteResult) {
                        is SyncResult.Success -> SyncResult.Success<String>(null)
                        is SyncResult.Error -> SyncResult.Error<String>(
                            deleteResult.message,
                            deleteResult.exception
                        )

                        is SyncResult.Loading -> SyncResult.Loading<String>(deleteResult.progress)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle errors gracefully - don't let sync failures break the expense operations
            val operationName = when (operation) {
                SyncOperation.CREATE -> "create"
                SyncOperation.UPDATE -> "update"
                SyncOperation.DELETE -> "delete"
            }
            SyncResult.Error("Failed to sync expense $operationName to cloud: ${e.message}", e)
        }
    }
}