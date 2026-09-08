package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Expense
import com.example.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

data class CategoryExpenseSummary(
  val category: ExpenseCategory,
  val totalPaise: Long,
  val expenseCount: Int
)

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
  fun getAllExpenses(): Flow<List<Expense>>

  @Query("SELECT * FROM expenses WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, id DESC")
  fun getExpensesForMonth(monthPrefix: String): Flow<List<Expense>>

  @Query("SELECT * FROM expenses WHERE date = :date ORDER BY id DESC")
  fun getExpensesForDate(date: String): Flow<List<Expense>>

  @Query("SELECT * FROM expenses WHERE id = :id")
  fun getExpenseById(id: Long): Flow<Expense?>

  @Query("SELECT * FROM expenses WHERE id = :id")
  suspend fun getExpenseByIdOnce(id: Long): Expense?

  @Query(
    """
    SELECT * FROM expenses 
    WHERE date LIKE :monthPrefix || '%' 
      AND (:category IS NULL OR category = :category)
    ORDER BY date DESC, id DESC
    """
  )
  fun getExpensesFiltered(monthPrefix: String, category: ExpenseCategory?): Flow<List<Expense>>

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE date LIKE :monthPrefix || '%'")
  fun getTotalExpensesPaiseForMonth(monthPrefix: String): Flow<Long>

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM expenses WHERE date LIKE :monthPrefix || '%'")
  suspend fun getTotalExpensesPaiseForMonthOnce(monthPrefix: String): Long

  @Query(
    """
    SELECT 
      category, 
      COALESCE(SUM(amountPaise), 0) AS totalPaise, 
      COUNT(*) AS expenseCount 
    FROM expenses 
    WHERE date LIKE :monthPrefix || '%' 
    GROUP BY category 
    ORDER BY totalPaise DESC
    """
  )
  fun getCategoryExpenseSummaryForMonth(monthPrefix: String): Flow<List<CategoryExpenseSummary>>

  @Query("SELECT DISTINCT vendor FROM expenses WHERE date LIKE :monthPrefix || '%' AND vendor IS NOT NULL AND vendor != '' ORDER BY vendor ASC")
  fun getVendorsForMonth(monthPrefix: String): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(expense: Expense): Long

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertAll(expenses: List<Expense>)

  @Update
  suspend fun update(expense: Expense)

  @Delete
  suspend fun delete(expense: Expense)

  @Query("DELETE FROM expenses WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM expenses")
  suspend fun deleteAll()
}
