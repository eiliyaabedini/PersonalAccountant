package ir.act.personalAccountant.domain.model

data class CurrencySettings(
    val currencyCode: String = "USD",
    val locale: String = "en_US"
) {
    companion object {
        val DEFAULT = CurrencySettings()
        
        val SUPPORTED_CURRENCIES = listOf(
            CurrencySettings("USD", "en_US"),
            CurrencySettings("EUR", "en_DE"),
            CurrencySettings("GBP", "en_GB"),
            CurrencySettings("JPY", "ja_JP"),
            CurrencySettings("CAD", "en_CA"),
            CurrencySettings("AUD", "en_AU")
        )
        
        fun getCurrencyDisplayName(currencyCode: String): String {
            return when (currencyCode) {
                "USD" -> "US Dollar ($)"
                "EUR" -> "Euro (€)"
                "GBP" -> "British Pound (£)"
                "JPY" -> "Japanese Yen (¥)"
                "CAD" -> "Canadian Dollar (C$)"
                "AUD" -> "Australian Dollar (A$)"
                else -> currencyCode
            }
        }
        
        fun getCurrencySymbol(currencyCode: String): String {
            return when (currencyCode) {
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                "JPY" -> "¥"
                "CAD" -> "C$"
                "AUD" -> "A$"
                else -> "$"
            }
        }
    }
}