package ir.act.personalAccountant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.act.personalAccountant.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)
    
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>
}