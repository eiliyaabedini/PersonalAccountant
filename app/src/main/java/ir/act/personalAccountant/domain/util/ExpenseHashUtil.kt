package ir.act.personalAccountant.domain.util

import ir.act.personalAccountant.domain.model.Expense
import java.security.MessageDigest

object ExpenseHashUtil {

    fun generateExpenseHash(expense: Expense): String {
        val dataToHash =
            "${expense.amount}_${expense.tag}_${expense.timestamp}_${expense.imagePath ?: ""}"
        return hashString(dataToHash)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}