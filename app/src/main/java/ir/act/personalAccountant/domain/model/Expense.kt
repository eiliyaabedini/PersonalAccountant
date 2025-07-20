package ir.act.personalAccountant.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double, // Home currency amount
    val timestamp: Long,
    val tag: String,
    val imagePath: String? = null,
    val destinationAmount: Double? = null, // Amount in destination currency
    val destinationCurrency: String? = null // Destination currency code
) {
    val isTravelExpense: Boolean get() = destinationAmount != null && destinationCurrency != null
    val exchangeRate: Double? get() = if (isTravelExpense) amount / destinationAmount!! else null
}