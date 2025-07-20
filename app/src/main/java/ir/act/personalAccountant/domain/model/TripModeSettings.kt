package ir.act.personalAccountant.domain.model

data class TripModeSettings(
    val isEnabled: Boolean = false,
    val destinationCurrency: CurrencySettings = CurrencySettings.DEFAULT,
    val exchangeRate: Double = 1.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        val DEFAULT = TripModeSettings()

        fun convertToHomeCurrency(
            destinationAmount: Double,
            exchangeRate: Double
        ): Double {
            return destinationAmount / exchangeRate
        }

        fun convertToDestinationCurrency(
            homeAmount: Double,
            exchangeRate: Double
        ): Double {
            return homeAmount * exchangeRate
        }

        fun formatExchangeRate(
            homeCurrency: CurrencySettings,
            destinationCurrency: CurrencySettings,
            exchangeRate: Double
        ): String {
            val homeSymbol = CurrencySettings.getCurrencySymbol(homeCurrency.currencyCode)
            val destSymbol = CurrencySettings.getCurrencySymbol(destinationCurrency.currencyCode)
            return "1 $homeSymbol = %.2f $destSymbol".format(exchangeRate)
        }

        fun formatExchangeRateWithTimestamp(
            homeCurrency: CurrencySettings,
            destinationCurrency: CurrencySettings,
            exchangeRate: Double,
            lastUpdated: Long
        ): String {
            val homeSymbol = CurrencySettings.getCurrencySymbol(homeCurrency.currencyCode)
            val destSymbol = CurrencySettings.getCurrencySymbol(destinationCurrency.currencyCode)
            val exchangeRateText = "1 $homeSymbol = %.2f $destSymbol".format(exchangeRate)

            val timeAgo = getTimeAgo(lastUpdated)
            return "$exchangeRateText • $timeAgo"
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Just now"
                diff < 3_600_000 -> "${diff / 60_000}m ago"
                diff < 86_400_000 -> "${diff / 3_600_000}h ago"
                else -> "${diff / 86_400_000}d ago"
            }
        }
    }
}