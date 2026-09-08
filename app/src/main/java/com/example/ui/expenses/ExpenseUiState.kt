package com.example.ui.expenses

import com.example.data.dao.CategoryExpenseSummary
import com.example.data.entity.Expense
import com.example.data.model.ExpenseCategory

data class ExpenseUiState(
  val selectedMonth: String = "", // e.g. "2026-09"
  val formattedMonth: String = "", // e.g. "September 2026"
  val selectedCategory: ExpenseCategory? = null, // null means "All"
  val expenses: List<Expense> = emptyList(),
  val monthlyTotalPaise: Long = 0L,
  val filteredTotalPaise: Long = 0L,
  val categorySummaries: List<CategoryExpenseSummary> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val userMessage: String? = null,
  val showAddEditDialog: Boolean = false,
  val editingExpense: Expense? = null,
  val deleteConfirmExpense: Expense? = null
)
