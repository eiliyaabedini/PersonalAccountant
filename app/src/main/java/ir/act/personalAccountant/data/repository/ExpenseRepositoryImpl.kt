package ir.act.personalAccountant.data.repository

import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.data.local.dao.ExpenseDao
import ir.act.personalAccountant.data.local.entity.ExpenseEntity
import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.usecase.SyncExpenseUseCase
import ir.act.personalAccountant.domain.usecase.SyncOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val syncExpenseUseCase: SyncExpenseUseCase
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense): Long {
        val id = expenseDao.insertExpense(expense.toEntity())

        // Trigger automatic sync after add (background, silent)
        val expenseWithId = expense.copy(id = id)
        val imageUris = if (expense.imagePath != null) listOf(expense.imagePath!!) else emptyList()

        // Launch sync in background - don't block the UI
        CoroutineScope(Dispatchers.IO).launch {
            syncExpenseUseCase(expenseWithId, imageUris, SyncOperation.CREATE)
        }

        return id
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())

        // Trigger automatic sync after update
        val imageUris = if (expense.imagePath != null) listOf(expense.imagePath!!) else emptyList()

        CoroutineScope(Dispatchers.IO).launch {
            syncExpenseUseCase(expense, imageUris, SyncOperation.UPDATE)
        }
    }

    override suspend fun deleteExpense(expenseId: Long) {
        expenseDao.getExpenseById(expenseId)?.let { entity ->
            val expense = entity.toDomain()
            expenseDao.deleteExpense(entity)

            // Trigger sync for deletion (empty image list)
            CoroutineScope(Dispatchers.IO).launch {
                syncExpenseUseCase(expense, emptyList(), SyncOperation.DELETE)
            }
        }
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return expenseDao.getExpenseById(id)?.toDomain()
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalExpenses(): Flow<Double> {
        return expenseDao.getTotalExpenses().map { total ->
            total ?: 0.0
        }
    }

    override fun getAllTagsWithCount(): Flow<List<TagWithCount>> {
        return expenseDao.getAllTagsWithCount()
    }

    override fun getExpensesByMonth(year: Int, month: Int): Flow<List<Expense>> {
        val startOfMonth = DateUtils.getStartOfMonth(year, month)
        val endOfMonth = DateUtils.getEndOfMonth(year, month)
        return expenseDao.getExpensesByMonth(startOfMonth, endOfMonth).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalExpensesByMonth(year: Int, month: Int): Flow<Double> {
        val startOfMonth = DateUtils.getStartOfMonth(year, month)
        val endOfMonth = DateUtils.getEndOfMonth(year, month)
        return expenseDao.getTotalExpensesByMonth(startOfMonth, endOfMonth).map { total ->
            total ?: 0.0
        }
    }

    override fun getExpensesByTagAndMonth(tag: String, year: Int, month: Int): Flow<List<Expense>> {
        val startOfMonth = DateUtils.getStartOfMonth(year, month)
        val endOfMonth = DateUtils.getEndOfMonth(year, month)
        return expenseDao.getExpensesByTagAndMonth(tag, startOfMonth, endOfMonth).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun mergeTags(oldTags: List<String>, newTag: String): Int {
        return expenseDao.updateExpenseTagsBatch(oldTags, newTag)
    }

    override suspend fun getExpensesByDateRange(startTime: Long, endTime: Long): List<Expense> {
        return expenseDao.getExpensesByDateRange(startTime, endTime).map { it.toDomain() }
    }

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            amount = amount,
            timestamp = timestamp,
            tag = tag,
            imagePath = imagePath,
            destinationAmount = destinationAmount,
            destinationCurrency = destinationCurrency
        )
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            timestamp = timestamp,
            tag = tag,
            imagePath = imagePath,
            destinationAmount = destinationAmount,
            destinationCurrency = destinationCurrency
        )
    }
}