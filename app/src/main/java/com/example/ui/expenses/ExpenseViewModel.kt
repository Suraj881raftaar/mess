package com.example.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.dao.CategoryExpenseSummary
import com.example.data.entity.Expense
import com.example.data.model.ExpenseCategory
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(
  private val expenseRepository: ExpenseRepository
) : ViewModel() {

  companion object {
    val MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)
    val DISPLAY_MONTH_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun getCurrentMonthString(): String = YearMonth.now().format(MONTH_FORMAT)
    fun getTodayString(): String = LocalDate.now().format(DATE_FORMAT)

    fun parseRupeesToPaise(input: String): Long? {
      val trimmed = input.trim()
      if (trimmed.isEmpty()) return null
      return try {
        val bd = BigDecimal(trimmed)
        if (bd <= BigDecimal.ZERO) return null
        bd.multiply(BigDecimal(100)).setScale(0, RoundingMode.HALF_UP).toLong()
      } catch (_: Exception) {
        null
      }
    }
  }

  private data class ExpenseMonthData(
    val month: String = getCurrentMonthString(),
    val category: ExpenseCategory? = null,
    val expenses: List<Expense> = emptyList(),
    val monthlyTotal: Long = 0L,
    val summaries: List<CategoryExpenseSummary> = emptyList()
  )

  private data class ExpenseDialogState(
    val showAddEditDialog: Boolean = false,
    val editingExpense: Expense? = null,
    val deleteConfirmExpense: Expense? = null,
    val userMessage: String? = null
  )

  private val _selectedMonth = MutableStateFlow(getCurrentMonthString())
  val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

  private val _selectedCategory = MutableStateFlow<ExpenseCategory?>(null)
  val selectedCategory: StateFlow<ExpenseCategory?> = _selectedCategory.asStateFlow()

  private val _dialogState = MutableStateFlow(ExpenseDialogState())
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  val showAddEditDialog: StateFlow<Boolean> = _dialogState
    .combine(MutableStateFlow(Unit)) { state, _ -> state.showAddEditDialog }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val editingExpense: StateFlow<Expense?> = _dialogState
    .combine(MutableStateFlow(Unit)) { state, _ -> state.editingExpense }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val deleteConfirmExpense: StateFlow<Expense?> = _dialogState
    .combine(MutableStateFlow(Unit)) { state, _ -> state.deleteConfirmExpense }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val userMessage: StateFlow<String?> = _dialogState
    .combine(MutableStateFlow(Unit)) { state, _ -> state.userMessage }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Reactively fetch expenses, monthly totals, and summaries atomically paired with month and category
  private val expenseDataFlow = combine(_selectedMonth, _selectedCategory) { month, category ->
    Pair(month, category)
  }.flatMapLatest { (month, category) ->
    combine(
      expenseRepository.getExpensesFiltered(month, category),
      expenseRepository.getTotalExpensesPaiseForMonth(month),
      expenseRepository.getCategoryExpenseSummaryForMonth(month)
    ) { expenses, monthlyTotal, summaries ->
      ExpenseMonthData(
        month = month,
        category = category,
        expenses = expenses,
        monthlyTotal = monthlyTotal,
        summaries = summaries
      )
    }
  }

  val uiState: StateFlow<ExpenseUiState> = combine(
    expenseDataFlow,
    _dialogState,
    _isLoading
  ) { data, dialog, loading ->
    val formattedMonth = try {
      val ym = YearMonth.parse(data.month, MONTH_FORMAT)
      ym.format(DISPLAY_MONTH_FORMAT)
    } catch (_: Exception) {
      data.month
    }

    val filteredTotal = data.expenses.sumOf { it.amountPaise }

    ExpenseUiState(
      selectedMonth = data.month,
      formattedMonth = formattedMonth,
      selectedCategory = data.category,
      expenses = data.expenses,
      monthlyTotalPaise = data.monthlyTotal,
      filteredTotalPaise = filteredTotal,
      categorySummaries = data.summaries,
      isLoading = loading,
      userMessage = dialog.userMessage,
      showAddEditDialog = dialog.showAddEditDialog,
      editingExpense = dialog.editingExpense,
      deleteConfirmExpense = dialog.deleteConfirmExpense
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ExpenseUiState(
      selectedMonth = getCurrentMonthString(),
      formattedMonth = try {
        YearMonth.now().format(DISPLAY_MONTH_FORMAT)
      } catch (_: Exception) {
        getCurrentMonthString()
      },
      isLoading = true
    )
  )

  fun setMonth(monthPrefix: String) {
    _selectedMonth.value = monthPrefix
  }

  fun selectPreviousMonth() {
    try {
      val current = YearMonth.parse(_selectedMonth.value, MONTH_FORMAT)
      val prev = current.minusMonths(1)
      _selectedMonth.value = prev.format(MONTH_FORMAT)
    } catch (_: Exception) {
      // ignore
    }
  }

  fun selectNextMonth() {
    try {
      val current = YearMonth.parse(_selectedMonth.value, MONTH_FORMAT)
      val next = current.plusMonths(1)
      _selectedMonth.value = next.format(MONTH_FORMAT)
    } catch (_: Exception) {
      // ignore
    }
  }

  fun selectCurrentMonth() {
    _selectedMonth.value = getCurrentMonthString()
  }

  fun setCategory(category: ExpenseCategory?) {
    _selectedCategory.value = category
  }

  fun openAddDialog() {
    _dialogState.update { it.copy(showAddEditDialog = true, editingExpense = null) }
  }

  fun openEditDialog(expense: Expense) {
    _dialogState.update { it.copy(showAddEditDialog = true, editingExpense = expense) }
  }

  fun dismissAddEditDialog() {
    _dialogState.update { it.copy(showAddEditDialog = false, editingExpense = null) }
  }

  fun requestDelete(expense: Expense) {
    _dialogState.update { it.copy(deleteConfirmExpense = expense) }
  }

  fun dismissDeleteDialog() {
    _dialogState.update { it.copy(deleteConfirmExpense = null) }
  }

  fun clearUserMessage() {
    _dialogState.update { it.copy(userMessage = null) }
  }

  fun saveExpense(
    id: Long,
    date: String,
    description: String,
    category: ExpenseCategory,
    amountRupeesStr: String,
    quantityStr: String? = null,
    unit: String? = null,
    vendor: String? = null,
    notes: String? = null
  ) {
    viewModelScope.launch {
      val result = saveExpenseInternal(
        id = id,
        date = date,
        description = description,
        category = category,
        amountRupeesStr = amountRupeesStr,
        quantityStr = quantityStr,
        unit = unit,
        vendor = vendor,
        notes = notes
      )
      if (result.isSuccess) {
        _dialogState.update {
          it.copy(
            showAddEditDialog = false,
            editingExpense = null,
            userMessage = if (id == 0L) "Expense added successfully" else "Expense updated successfully"
          )
        }
      } else {
        _dialogState.update {
          it.copy(userMessage = result.exceptionOrNull()?.message ?: "Failed to save expense")
        }
      }
    }
  }

  suspend fun saveExpenseSync(
    id: Long,
    date: String,
    description: String,
    category: ExpenseCategory,
    amountRupeesStr: String,
    quantityStr: String? = null,
    unit: String? = null,
    vendor: String? = null,
    notes: String? = null
  ): Result<Long> {
    return saveExpenseInternal(
      id = id,
      date = date,
      description = description,
      category = category,
      amountRupeesStr = amountRupeesStr,
      quantityStr = quantityStr,
      unit = unit,
      vendor = vendor,
      notes = notes
    )
  }

  private suspend fun saveExpenseInternal(
    id: Long,
    date: String,
    description: String,
    category: ExpenseCategory,
    amountRupeesStr: String,
    quantityStr: String?,
    unit: String?,
    vendor: String?,
    notes: String?
  ): Result<Long> {
    val trimmedDate = date.trim()
    val trimmedDesc = description.trim()

    if (trimmedDate.isBlank()) {
      return Result.failure(IllegalArgumentException("Date cannot be blank"))
    }
    try {
      LocalDate.parse(trimmedDate, DATE_FORMAT)
    } catch (_: Exception) {
      return Result.failure(IllegalArgumentException("Invalid date format, expected YYYY-MM-DD"))
    }

    if (trimmedDesc.isBlank()) {
      return Result.failure(IllegalArgumentException("Description cannot be blank"))
    }

    val paise = parseRupeesToPaise(amountRupeesStr)
      ?: return Result.failure(IllegalArgumentException("Amount must be greater than ₹0"))

    val quantity = if (!quantityStr.isNullOrBlank()) {
      val q = quantityStr.trim().toDoubleOrNull()
      if (q == null || q <= 0.0) {
        return Result.failure(IllegalArgumentException("Quantity must be a positive number"))
      }
      q
    } else {
      null
    }

    return if (id == 0L) {
      // Adding new expense
      expenseRepository.addExpense(
        date = trimmedDate,
        description = trimmedDesc,
        category = category,
        amountPaise = paise,
        quantity = quantity,
        unit = unit?.trim()?.ifBlank { null },
        vendor = vendor?.trim()?.ifBlank { null },
        notes = notes?.trim()?.ifBlank { null }
      )
    } else {
      // Editing existing expense
      val existing = Expense(
        id = id,
        date = trimmedDate,
        description = trimmedDesc,
        category = category,
        amountPaise = paise,
        quantity = quantity,
        unit = unit?.trim()?.ifBlank { null },
        vendor = vendor?.trim()?.ifBlank { null },
        notes = notes?.trim()?.ifBlank { null },
        updatedAt = System.currentTimeMillis()
      )
      val updateResult = expenseRepository.updateExpense(existing)
      if (updateResult.isSuccess) {
        Result.success(id)
      } else {
        Result.failure(updateResult.exceptionOrNull() ?: Exception("Update failed"))
      }
    }
  }

  fun confirmDelete() {
    val expense = _dialogState.value.deleteConfirmExpense ?: return
    viewModelScope.launch {
      expenseRepository.deleteExpense(expense)
      _dialogState.update {
        it.copy(
          deleteConfirmExpense = null,
          userMessage = "Expense deleted"
        )
      }
    }
  }

  suspend fun deleteExpenseSync(expense: Expense) {
    expenseRepository.deleteExpense(expense)
  }

  class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
        return ExpenseViewModel(repository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
