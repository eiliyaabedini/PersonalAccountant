package ir.act.personalAccountant.domain.model

data class NetWorthSnapshot(
    val id: Long = 0,
    val totalAssets: Double,
    val netWorth: Double,
    val currency: String,
    val calculatedAt: Long = System.currentTimeMillis()
)