package ir.act.personalAccountant.core.util

import ir.act.personalAccountant.domain.model.CurrencySettings
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {
    
    fun formatCurrency(amount: Double, currencySettings: CurrencySettings): String {
        return try {
            val locale = parseLocale(currencySettings.locale)
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.currency = Currency.getInstance(currencySettings.currencyCode)
            formatter.format(amount)
        } catch (e: Exception) {
            // Fallback to default USD formatting
            formatCurrencyFallback(amount, currencySettings.currencyCode)
        }
    }
    
    fun getCurrencySymbol(currencySettings: CurrencySettings): String {
        return CurrencySettings.getCurrencySymbol(currencySettings.currencyCode)
    }
    
    private fun parseLocale(localeString: String): Locale {
        return when {
            localeString.contains("_") -> {
                val parts = localeString.split("_")
                if (parts.size >= 2) {
                    Locale(parts[0], parts[1])
                } else {
                    Locale(parts[0])
                }
            }
            else -> Locale(localeString)
        }
    }
    
    private fun formatCurrencyFallback(amount: Double, currencyCode: String): String {
        val symbol = CurrencySettings.getCurrencySymbol(currencyCode)
        return "$symbol%.2f".format(amount)
    }
}