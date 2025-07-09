package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.core.util.ImageFileManager
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val imageFileManager: ImageFileManager
) {
    suspend operator fun invoke(expenseId: Long) {
        // Get the expense to check if it has an image
        val expense = repository.getExpenseById(expenseId)
        
        // Delete the image file if it exists
        expense?.imagePath?.let { imagePath ->
            imageFileManager.deleteImage(imagePath)
        }
        
        // Delete the expense from database
        repository.deleteExpense(expenseId)
    }
}