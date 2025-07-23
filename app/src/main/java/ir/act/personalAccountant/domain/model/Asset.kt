package ir.act.personalAccountant.domain.model

data class Asset(
    val id: Long = 0,
    val name: String,
    val type: String,
    val amount: Double,
    val currency: String,
    val quantity: Double = 1.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalValue: Double
        get() = amount * quantity
}