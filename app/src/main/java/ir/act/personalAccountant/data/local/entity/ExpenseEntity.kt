package ir.act.personalAccountant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val timestamp: Long,
    val tag: String,
    val imagePath: String? = null,
    val destinationAmount: Double? = null, // Amount in destination currency
    val destinationCurrency: String? = null // Destination currency code
)