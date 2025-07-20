package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.repository.TripModeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val tripModeRepository: TripModeRepository
) {
    suspend operator fun invoke(
        amount: Double,
        tag: String,
        timestamp: Long = System.currentTimeMillis(),
        imagePath: String? = null,
        originalDestinationAmount: Double? = null, // Original amount in destination currency before conversion
        destinationCurrency: String? = null // Explicit destination currency (overrides trip mode settings)
    ): Long {
        if (amount <= 0) {
            throw IllegalArgumentException("Amount must be greater than 0")
        }

        // Get current trip mode settings
        val tripModeSettings = tripModeRepository.getTripModeSettings().first()

        val expense = if (originalDestinationAmount != null && destinationCurrency != null) {
            // Explicit dual currency (from AI analysis or manual entry)
            Expense(
                amount = amount, // Home currency amount (already converted)
                timestamp = timestamp,
                tag = tag,
                imagePath = imagePath,
                destinationAmount = originalDestinationAmount, // Original destination currency amount
                destinationCurrency = destinationCurrency // Explicit destination currency
            )
        } else if (tripModeSettings.isEnabled && originalDestinationAmount != null) {
            // Travel mode: amount is already converted to home currency
            Expense(
                amount = amount, // Home currency amount (already converted)
                timestamp = timestamp,
                tag = tag,
                imagePath = imagePath,
                destinationAmount = originalDestinationAmount, // Original destination currency amount
                destinationCurrency = tripModeSettings.destinationCurrency.currencyCode
            )
        } else {
            // Normal mode: no dual currency
            Expense(
                amount = amount,
                timestamp = timestamp,
                tag = tag,
                imagePath = imagePath,
                destinationAmount = null,
                destinationCurrency = null
            )
        }
        
        return repository.addExpense(expense)
    }
}