package ir.act.personalAccountant.ai.domain.model

data class CurrencyExchangeResponse(
    val success: Boolean,
    val exchangeRate: Double? = null,
    val fromCurrency: String? = null,
    val toCurrency: String? = null,
    val timestamp: Long? = null,
    val source: String? = null, // Source of the rate (e.g., "Google", "XE.com", etc.)
    val errorMessage: String? = null
)

data class ExchangeRateResult(
    val rate: Double,
    val from_currency: String,
    val to_currency: String,
    val timestamp_utc: String? = null,
    val source: String? = null,
    val confidence: Double = 1.0
)