package ir.act.personalAccountant.data.sync

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.AuthRepository
import ir.act.personalAccountant.domain.sync.CloudExpense
import ir.act.personalAccountant.domain.sync.CloudSyncStrategy
import ir.act.personalAccountant.domain.sync.SyncResult
import ir.act.personalAccountant.util.ImageCompressionUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncStrategy @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val authRepository: AuthRepository,
    private val imageCompressionUtil: ImageCompressionUtil,
    @ApplicationContext private val context: Context
) : CloudSyncStrategy {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val EXPENSES_COLLECTION = "expenses"
        private const val RECEIPTS_PATH = "receipts"
    }

    override suspend fun syncExpenseToCloud(
        expense: Expense,
        imageUris: List<String>
    ): SyncResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                val cloudExpenseId = expense.id.toString() // Use local ID directly as document ID

                // For updates (expense.id > 0), handle image cleanup
                if (expense.id > 0) {
                    cleanupOldImagesForUpdate(userId, cloudExpenseId, imageUris)
                }

                // Upload images first and get URLs
                val imageUploadResult = uploadImages(imageUris, cloudExpenseId)
                if (imageUploadResult is SyncResult.Error) {
                    return@withContext SyncResult.Error("Failed to upload images: ${imageUploadResult.message}")
                }

                val imageUrls = (imageUploadResult as SyncResult.Success).data ?: emptyList()

                // Create CloudExpense with image URLs
                val cloudExpense = mapExpenseToCloudExpense(expense, imageUrls, cloudExpenseId)

                // Save to Firestore
                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(EXPENSES_COLLECTION)
                    .document(cloudExpenseId)
                    .set(cloudExpense, SetOptions.merge())
                    .await()

                SyncResult.Success(cloudExpenseId)
            } catch (e: Exception) {
                SyncResult.Error("Failed to sync expense to cloud: ${e.message}", e)
            }
        }
    }

    override suspend fun syncAllPendingExpenses(expenses: List<Expense>): SyncResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                val results = expenses.map { expense ->
                    async {
                        // For batch sync, assume no images for now or handle them separately
                        syncExpenseToCloud(expense, emptyList())
                    }
                }.awaitAll()

                val failedSyncs = results.filterIsInstance<SyncResult.Error<String>>()
                if (failedSyncs.isNotEmpty()) {
                    val errorMessages = failedSyncs.joinToString("; ") { it.message }
                    return@withContext SyncResult.Error("Some expenses failed to sync: $errorMessages")
                }

                SyncResult.Success(Unit)
            } catch (e: Exception) {
                SyncResult.Error("Failed to sync pending expenses: ${e.message}", e)
            }
        }
    }

    override suspend fun downloadUserExpenses(userId: String): SyncResult<List<CloudExpense>> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(EXPENSES_COLLECTION)
                    .get()
                    .await()

                // Map documents to CloudExpense and set the document ID as the expense ID
                val cloudExpenses = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(CloudExpense::class.java)?.copy(
                        id = document.id // Set the document ID as the expense ID (which is the local ID)
                    )
                }

                SyncResult.Success(cloudExpenses)
            } catch (e: Exception) {
                SyncResult.Error("Failed to download user expenses: ${e.message}", e)
            }
        }
    }

    override suspend fun deleteExpenseFromCloud(expenseId: String): SyncResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                // Direct delete using local ID as document ID - no query needed!
                firestore.collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(EXPENSES_COLLECTION)
                    .document(expenseId) // Direct access using local ID
                    .delete()
                    .await()

                // Delete associated images
                deleteExpenseImages(userId, expenseId)

                SyncResult.Success(Unit)
            } catch (e: Exception) {
                SyncResult.Error("Failed to delete expense from cloud: ${e.message}", e)
            }
        }
    }

    override suspend fun isUserAuthenticated(): Boolean {
        return try {
            authRepository.isUserSignedIn()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getCurrentUserId(): String? {
        return try {
            val currentUser = authRepository.currentUser.first()
            currentUser?.uid
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun uploadImages(
        imageUris: List<String>,
        expenseId: String
    ): SyncResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                if (imageUris.isEmpty()) {
                    return@withContext SyncResult.Success(emptyList())
                }

                val uploadResults = imageUris.mapIndexed { index, imageUri ->
                    async {
                        uploadSingleImage(imageUri, expenseId, index)
                    }
                }.awaitAll()

                val failedUploads = uploadResults.filterIsInstance<SyncResult.Error<String>>()
                if (failedUploads.isNotEmpty()) {
                    val errorMessages = failedUploads.joinToString("; ") { it.message }
                    return@withContext SyncResult.Error("Some images failed to upload: $errorMessages")
                }

                val imageUrls = uploadResults.mapNotNull {
                    (it as? SyncResult.Success)?.data
                }

                SyncResult.Success(imageUrls)
            } catch (e: Exception) {
                SyncResult.Error("Failed to upload images: ${e.message}", e)
            }
        }
    }

    private suspend fun uploadSingleImage(
        imageUri: String,
        expenseId: String,
        imageIndex: Int
    ): SyncResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                // Compress image first
                val compressionResult = imageCompressionUtil.compressImage(imageUri)
                if (compressionResult is SyncResult.Error) {
                    return@withContext SyncResult.Error("Failed to compress image: ${compressionResult.message}")
                }

                val compressedFile = (compressionResult as SyncResult.Success).data!!
                val fileName = "image_${imageIndex}.jpg"
                val imagePath = "$RECEIPTS_PATH/$userId/$expenseId/$fileName"

                val storageRef = storage.reference.child(imagePath)
                val uploadTask = storageRef.putFile(Uri.fromFile(compressedFile)).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                // Clean up compressed file
                compressedFile.delete()

                SyncResult.Success(downloadUrl)
            } catch (e: Exception) {
                SyncResult.Error("Failed to upload single image: ${e.message}", e)
            }
        }
    }

    override suspend fun createExpensesInCloud(expenses: List<Expense>): SyncResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                val batch = firestore.batch()

                expenses.forEach { expense ->
                    val cloudExpenseId = expense.id.toString() // Use local ID as document ID
                    val cloudExpense =
                        mapExpenseToCloudExpense(expense, emptyList(), cloudExpenseId)

                    val docRef = firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(EXPENSES_COLLECTION)
                        .document(cloudExpenseId) // Direct access with local ID

                    batch.set(docRef, cloudExpense)
                }

                batch.commit().await()
                SyncResult.Success(Unit)
            } catch (e: Exception) {
                SyncResult.Error("Failed to create expenses in cloud: ${e.message}", e)
            }
        }
    }

    override suspend fun updateExpensesInCloud(expenses: List<Expense>): SyncResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                val batch = firestore.batch()

                for (expense in expenses) {
                    // Direct update using local ID as document ID - no query needed!
                    val docRef = firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(EXPENSES_COLLECTION)
                        .document(expense.id.toString()) // Direct access with local ID

                    val updateData = mapExpenseToFirestoreData(expense)
                    batch.set(docRef, updateData) // Use set for upsert behavior
                }

                batch.commit().await()
                SyncResult.Success(Unit)
            } catch (e: Exception) {
                SyncResult.Error("Failed to update expenses in cloud: ${e.message}", e)
            }
        }
    }

    override suspend fun deleteExpensesFromCloud(expenseIds: List<String>): SyncResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getCurrentUserId()
                    ?: return@withContext SyncResult.Error("User not authenticated")

                val batch = firestore.batch()

                for (expenseId in expenseIds) {
                    // Direct batch delete using local ID as document ID - no query needed!
                    val docRef = firestore.collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(EXPENSES_COLLECTION)
                        .document(expenseId) // Direct access with local ID
                    batch.delete(docRef)

                    // Clean up associated images (fire and forget)
                    try {
                        deleteExpenseImages(userId, expenseId)
                    } catch (e: Exception) {
                        println("Failed to delete images for expense: $expenseId, error: ${e.message}")
                    }
                }

                batch.commit().await()
                SyncResult.Success(Unit)
            } catch (e: Exception) {
                SyncResult.Error("Failed to delete expenses from cloud: ${e.message}", e)
            }
        }
    }

    private fun mapExpenseToCloudExpense(
        expense: Expense,
        imageUrls: List<String>,
        cloudId: String
    ): CloudExpense {
        return CloudExpense(
            id = cloudId,
            amount = expense.amount,
            timestamp = expense.timestamp,
            tag = expense.tag,
            imageUrls = imageUrls,
            destinationAmount = expense.destinationAmount,
            destinationCurrency = expense.destinationCurrency,
            lastModified = System.currentTimeMillis()
            // syncStatus is excluded from Firebase storage via @Exclude annotation
            // It defaults to SyncStatus.PENDING for local tracking
            // localId is computed from id (document ID) since we use local ID as document ID
        )
    }

    private fun mapExpenseToFirestoreData(expense: Expense): Map<String, Any> {
        return mapOf(
            "amount" to expense.amount,
            "timestamp" to expense.timestamp,
            "tag" to expense.tag,
            "imageUrls" to (expense.imagePath?.let { listOf(it) } ?: emptyList()),
            "destinationAmount" to (expense.destinationAmount ?: 0.0),
            "destinationCurrency" to (expense.destinationCurrency ?: ""),
            "lastModified" to System.currentTimeMillis()
            // No localId field needed since we use local ID as document ID
        )
    }

    private suspend fun deleteExpenseImages(userId: String, cloudExpenseId: String) {
        try {
            val imagePath = "$RECEIPTS_PATH/$userId/$cloudExpenseId/"
            val storageRef = storage.reference.child(imagePath)

            // List all items in the expense's image folder
            val listResult = storageRef.listAll().await()

            // Delete all images in parallel
            listResult.items.forEach { imageRef ->
                try {
                    imageRef.delete().await()
                } catch (e: Exception) {
                    println("Failed to delete image: ${imageRef.path}, error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("Failed to delete expense images: $e")
        }
    }

    /**
     * Cleans up old images when updating an expense. For updates, we delete all existing images
     * and upload the new ones to ensure consistency and prevent orphaned files.
     */
    private suspend fun cleanupOldImagesForUpdate(
        userId: String,
        expenseId: String,
        newImageUris: List<String>
    ) {
        try {
            // Get existing expense from Firestore to check if it has images
            val existingExpense = getExpenseFromFirestore(userId, expenseId)

            // If there were existing images, clean them up
            if (existingExpense?.imageUrls?.isNotEmpty() == true) {
                // For simplicity and reliability, delete all existing images
                // This prevents orphaned files and ensures consistency
                // Note: newImageUris will be uploaded in the normal flow after this cleanup
                deleteExpenseImages(userId, expenseId)
            }
        } catch (e: Exception) {
            // Log error but don't fail the sync - image cleanup failure shouldn't prevent expense update
            println("Failed to cleanup old images for expense $expenseId: ${e.message}")
        }
    }

    /**
     * Retrieves an existing expense from Firestore to compare image URLs.
     */
    private suspend fun getExpenseFromFirestore(userId: String, expenseId: String): CloudExpense? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(EXPENSES_COLLECTION)
                .document(expenseId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(CloudExpense::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to get existing expense from Firestore: ${e.message}")
            null
        }
    }
}