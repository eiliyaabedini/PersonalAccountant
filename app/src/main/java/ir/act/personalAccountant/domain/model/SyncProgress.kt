package ir.act.personalAccountant.domain.model

data class SyncProgress(
    val totalItems: Int = 0,
    val completedItems: Int = 0,
    val currentItem: String = "",
    val currentStep: SyncStep = SyncStep.IDLE,
    val error: String? = null
) {
    val progress: Float
        get() = if (totalItems > 0) completedItems.toFloat() / totalItems.toFloat() else 0f

    val progressPercentage: Int
        get() = (progress * 100).toInt()

    val isCompleted: Boolean
        get() = completedItems >= totalItems && totalItems > 0

    val hasError: Boolean
        get() = error != null
}

enum class SyncStep {
    IDLE,
    CONNECTING,
    FETCHING_EXPENSES,
    CREATING_SPREADSHEET,
    CREATING_MONTHLY_SHEETS,
    UPLOADING_IMAGES,
    SYNCING_EXPENSES,
    COMPLETED,
    ERROR
}

data class SyncItem(
    val id: String,
    val description: String,
    val status: SyncItemStatus,
    val error: String? = null
)

enum class SyncItemStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ERROR
}