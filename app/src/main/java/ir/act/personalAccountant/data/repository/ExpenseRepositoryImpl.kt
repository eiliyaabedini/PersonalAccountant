package ir.act.personalAccountant.data.repository

import ir.act.personalAccountant.data.local.dao.ExpenseDao
import ir.act.personalAccountant.data.local.entity.ExpenseEntity
import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override suspend fun addExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
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

    private fun Expense.toEntity(): ExpenseEntity {
        return ExpenseEntity(
            id = id,
            amount = amount,
            timestamp = timestamp,
            tag = tag
        )
    }

    private fun ExpenseEntity.toDomain(): Expense {
        return Expense(
            id = id,
            amount = amount,
            timestamp = timestamp,
            tag = tag
        )
    }
}