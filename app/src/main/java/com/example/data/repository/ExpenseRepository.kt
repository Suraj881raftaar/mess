package com.example.data.repository

import com.example.data.dao.CategoryExpenseSummary
import com.example.data.dao.ExpenseDao
import com.example.data.entity.Expense
import com.example.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

  val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

  fun getExpensesForMonth(monthPrefix: String): Flow<List<Expense>> =
    expenseDao.getExpensesForMonth(monthPrefix)

  fun getExpensesForDate(date: String): Flow<List<Expense>> =
    expenseDao.getExpensesForDate(date)

  fun getExpenseById(id: Long): Flow<Expense?> =
    expenseDao.getExpenseById(id)

  fun getExpensesFiltered(monthPrefix: String, category: ExpenseCategory?): Flow<List<Expense>> =
    expenseDao.getExpensesFiltered(monthPrefix, category)

  fun getTotalExpensesPaiseForMonth(monthPrefix: String): Flow<Long> =
    expenseDao.getTotalExpensesPaiseForMonth(monthPrefix)

  suspend fun getTotalExpensesPaiseForMonthOnce(monthPrefix: String): Long =
    expenseDao.getTotalExpensesPaiseForMonthOnce(monthPrefix)

  fun getCategoryExpenseSummaryForMonth(monthPrefix: String): Flow<List<CategoryExpenseSummary>> =
    expenseDao.getCategoryExpenseSummaryForMonth(monthPrefix)

  suspend fun addExpense(
    date: String,
    description: String,
    category: ExpenseCategory,
    amountPaise: Long,
    quantity: Double? = null,
    unit: String? = null,
    vendor: String? = null,
    notes: String? = null
  ): Result<Long> {
    val trimmedDesc = description.trim()
    val trimmedDate = date.trim()

    if (trimmedDesc.isBlank()) {
      return Result.failure(IllegalArgumentException("Description cannot be blank"))
    }
    if (trimmedDate.isBlank()) {
      return Result.failure(IllegalArgumentException("Date cannot be blank"))
    }
    if (amountPaise <= 0L) {
      return Result.failure(IllegalArgumentException("Expense amount must be greater than 0"))
    }

    val expense = Expense(
      date = trimmedDate,
      description = trimmedDesc,
      category = category,
      amountPaise = amountPaise,
      quantity = quantity,
      unit = unit?.trim()?.ifBlank { null },
      vendor = vendor?.trim()?.ifBlank { null },
      notes = notes?.trim()?.ifBlank { null }
    )
    val id = expenseDao.insert(expense)
    return Result.success(id)
  }

  suspend fun updateExpense(expense: Expense): Result<Unit> {
    if (expense.description.isBlank()) {
      return Result.failure(IllegalArgumentException("Description cannot be blank"))
    }
    if (expense.date.isBlank()) {
      return Result.failure(IllegalArgumentException("Date cannot be blank"))
    }
    if (expense.amountPaise <= 0L) {
      return Result.failure(IllegalArgumentException("Expense amount must be greater than 0"))
    }

    expenseDao.update(
      expense.copy(
        description = expense.description.trim(),
        date = expense.date.trim(),
        unit = expense.unit?.trim()?.ifBlank { null },
        vendor = expense.vendor?.trim()?.ifBlank { null },
        notes = expense.notes?.trim()?.ifBlank { null },
        updatedAt = System.currentTimeMillis()
      )
    )
    return Result.success(Unit)
  }

  suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

  suspend fun deleteExpenseById(id: Long) = expenseDao.deleteById(id)
}
