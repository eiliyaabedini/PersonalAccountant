package ir.act.personalAccountant.data.local.model

data class TagStatistics(
    val tag: String,
    val usageCount: Int,
    val lastUsed: Long
)