package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.sync.CloudExpense
import ir.act.personalAccountant.domain.sync.CloudSyncStrategy
import ir.act.personalAccountant.domain.sync.SyncResult
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

interface SyncAllExpensesUseCase {
    suspend operator fun invoke(): SyncResult<Unit>
}

@Singleton
class SyncAllExpensesUseCaseImpl @Inject constructor(
    private val cloudSyncStrategy: CloudSyncStrategy,
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val notificationPreferences: NotificationPreferences
) : SyncAllExpensesUseCase {

    override suspend operator fun invoke(): SyncResult<Unit> {
        return try {
            // Check if cloud sync is enabled
            if (!notificationPreferences.isCloudSyncEnabled) {
                return SyncResult.Error("Cloud sync is disabled. Enable it in settings to sync expenses.")
            }

            // Check authentication
            if (!cloudSyncStrategy.isUserAuthenticated()) {
                return SyncResult.Error("User not authenticated. Please sign in to sync expenses.")
            }

            val userId = cloudSyncStrategy.getCurrentUserId()
                ?: return SyncResult.Error("No user ID available")

            // Step 1: Get local expenses
            val localExpenses = getAllExpensesUseCase().firstOrNull() ?: emptyList()

            // Step 2: Download cloud expenses
            val cloudResult = cloudSyncStrategy.downloadUserExpenses(userId)
            val cloudExpenses = when (cloudResult) {
                is SyncResult.Success -> cloudResult.data ?: emptyList()
                is SyncResult.Error -> {
                    // If download fails, upload all local expenses as backup
                    return uploadAllLocalExpenses(localExpenses)
                }

                else -> emptyList()
            }

            // Step 3: Calculate differences
            val (toCreate, toUpdate, toDelete) = calculateSyncDifferences(
                localExpenses,
                cloudExpenses
            )

            // Step 4: Sync differences
            performDifferentialSync(toCreate, toUpdate, toDelete)

        } catch (e: Exception) {
            SyncResult.Error("Failed to sync all expenses: ${e.message}", e)
        }
    }

    private suspend fun uploadAllLocalExpenses(localExpenses: List<Expense>): SyncResult<Unit> {
        return if (localExpenses.isEmpty()) {
            SyncResult.Success(Unit)
        } else {
            cloudSyncStrategy.createExpensesInCloud(localExpenses)
        }
    }

    private fun calculateSyncDifferences(
        localExpenses: List<Expense>,
        cloudExpenses: List<CloudExpense>
    ): Triple<List<Expense>, List<Expense>, List<String>> {

        // Create maps for efficient lookup
        val localByIdMap = localExpenses.associateBy { it.id }
        val cloudByLocalIdMap = cloudExpenses.associateBy { it.localId }
        val cloudByIdMap = cloudExpenses.associateBy { it.id }

        // Expenses to create (exist locally but not in cloud)
        val toCreate = localExpenses.filter { local ->
            !cloudByLocalIdMap.containsKey(local.id)
        }

        // Expenses to update (exist in both but local is newer)
        val toUpdate = localExpenses.filter { local ->
            val cloudExpense = cloudByLocalIdMap[local.id]
            cloudExpense != null && local.timestamp > cloudExpense.lastModified
        }

        // Expenses to delete (exist in cloud but not locally - were deleted locally)
        val toDelete = cloudExpenses.filter { cloud ->
            !localByIdMap.containsKey(cloud.localId)
        }.map { it.id }

        return Triple(toCreate, toUpdate, toDelete)
    }

    private suspend fun performDifferentialSync(
        toCreate: List<Expense>,
        toUpdate: List<Expense>,
        toDelete: List<String>
    ): SyncResult<Unit> {

        val results = mutableListOf<SyncResult<Unit>>()

        // Create new expenses
        if (toCreate.isNotEmpty()) {
            results.add(cloudSyncStrategy.createExpensesInCloud(toCreate))
        }

        // Update existing expenses
        if (toUpdate.isNotEmpty()) {
            results.add(cloudSyncStrategy.updateExpensesInCloud(toUpdate))
        }

        // Delete expenses
        if (toDelete.isNotEmpty()) {
            results.add(cloudSyncStrategy.deleteExpensesFromCloud(toDelete))
        }

        // Check for any failures
        val failedResults = results.filterIsInstance<SyncResult.Error<Unit>>()
        if (failedResults.isNotEmpty()) {
            val errorMessages = failedResults.joinToString("; ") { it.message }
            return SyncResult.Error("Some sync operations failed: $errorMessages")
        }

        return SyncResult.Success(Unit)
    }
}