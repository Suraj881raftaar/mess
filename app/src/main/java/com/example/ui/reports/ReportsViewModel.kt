package com.example.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.dao.EmployeeMealCount
import com.example.data.dao.MonthlyMealBreakdown
import com.example.data.entity.Employee
import com.example.data.entity.Payment
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.PaymentRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(
  private val employeeRepository: EmployeeRepository,
  private val attendanceRepository: AttendanceRepository,
  private val expenseRepository: ExpenseRepository,
  private val paymentRepository: PaymentRepository? = null
) : ViewModel() {

  companion object {
    private val MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)
    private val DISPLAY_MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    fun getCurrentMonthString(): String = YearMonth.now().format(MONTH_FORMAT)
  }

  private val _selectedMonth = MutableStateFlow(getCurrentMonthString())
  val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _showOnlyWithMeals = MutableStateFlow(false)
  val showOnlyWithMeals: StateFlow<Boolean> = _showOnlyWithMeals.asStateFlow()

  private val _selectedEmployeeDetail = MutableStateFlow<EmployeeReportItem?>(null)
  val selectedEmployeeDetail: StateFlow<EmployeeReportItem?> = _selectedEmployeeDetail.asStateFlow()

  fun setMonth(month: String) {
    _selectedMonth.value = month
  }

  fun previousMonth() {
    try {
      val ym = YearMonth.parse(_selectedMonth.value, MONTH_FORMAT)
      _selectedMonth.value = ym.minusMonths(1).format(MONTH_FORMAT)
    } catch (_: Exception) {
      _selectedMonth.value = getCurrentMonthString()
    }
  }

  fun nextMonth() {
    try {
      val ym = YearMonth.parse(_selectedMonth.value, MONTH_FORMAT)
      _selectedMonth.value = ym.plusMonths(1).format(MONTH_FORMAT)
    } catch (_: Exception) {
      _selectedMonth.value = getCurrentMonthString()
    }
  }

  fun resetToCurrentMonth() {
    _selectedMonth.value = getCurrentMonthString()
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setShowOnlyWithMeals(show: Boolean) {
    _showOnlyWithMeals.value = show
  }

  fun selectEmployeeForDetail(item: EmployeeReportItem?) {
    _selectedEmployeeDetail.value = item
  }

  fun recordPayment(
    employeeId: Long,
    month: String,
    amountPaise: Long,
    paymentDate: String,
    paymentMethod: String,
    notes: String?,
    onComplete: (Boolean) -> Unit = {}
  ) {
    viewModelScope.launch {
      val repo = paymentRepository
      if (repo != null) {
        val res = repo.recordPayment(employeeId, month, amountPaise, paymentDate, paymentMethod, notes)
        onComplete(res.isSuccess)
      } else {
        onComplete(false)
      }
    }
  }

  fun deletePayment(payment: Payment) {
    viewModelScope.launch {
      paymentRepository?.deletePayment(payment)
    }
  }

  private data class RawMonthData(
    val month: String,
    val totalExpensesPaise: Long,
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int,
    val totalMeals: Int,
    val activeEmployeeCount: Int,
    val employeeBills: List<EmployeeReportItem>,
    val totalBilledPaise: Long,
    val totalCollectedPaise: Long,
    val totalPendingPaise: Long,
    val collectionPercentage: Float
  )

  // Reactively fetch and calculate monthly data atomically paired with selectedMonth
  private val monthDataFlow = _selectedMonth.flatMapLatest { month ->
    val paymentsFlow = paymentRepository?.getPaymentsForMonth(month) ?: flowOf(emptyList())

    combine(
      expenseRepository.getTotalExpensesPaiseForMonth(month),
      attendanceRepository.getMonthlyMealBreakdown(month),
      attendanceRepository.getEmployeeMealCountsForMonth(month),
      employeeRepository.allEmployees,
      employeeRepository.activeEmployeeCount,
      paymentsFlow
    ) { args: Array<Any?> ->
      @Suppress("UNCHECKED_CAST")
      val totalExpensesPaise = args[0] as Long
      val breakdown = args[1] as MonthlyMealBreakdown
      @Suppress("UNCHECKED_CAST")
      val employeeMealCounts = args[2] as List<EmployeeMealCount>
      @Suppress("UNCHECKED_CAST")
      val allEmployees = args[3] as List<Employee>
      val activeCount = args[4] as Int
      @Suppress("UNCHECKED_CAST")
      val paymentsList = args[5] as List<Payment>

      val costPerMealRupees = CurrencyUtils.calculateCostPerMealRupees(
        totalExpensePaise = totalExpensesPaise,
        totalMeals = breakdown.totalMeals
      )

      val mealCountsMap = employeeMealCounts.associateBy { it.employeeId }
      val employeesMap = allEmployees.associateBy { it.id }.toMutableMap()
      val paymentsByEmployee = paymentsList.groupBy { it.employeeId }

      // Ensure any historical employee IDs present in attendance are accounted for
      employeeMealCounts.forEach { mealCount ->
        if (!employeesMap.containsKey(mealCount.employeeId)) {
          employeesMap[mealCount.employeeId] = Employee(
            id = mealCount.employeeId,
            employeeCode = "EMP-${mealCount.employeeId}",
            name = "Employee #${mealCount.employeeId}",
            department = "General",
            isActive = false
          )
        }
      }

      var totalBilledSum = 0L
      var totalCollectedSum = 0L

      val billItems = employeesMap.values.map { employee ->
        val counts = mealCountsMap[employee.id]
        val breakfast = counts?.breakfastCount ?: 0
        val lunch = counts?.lunchCount ?: 0
        val dinner = counts?.dinnerCount ?: 0
        val total = counts?.totalMeals ?: 0

        val payableRupees = CurrencyUtils.calculateEmployeePayable(
          employeeMeals = total,
          costPerMealRupees = costPerMealRupees
        )
        val payablePaise = CurrencyUtils.rupeesToPaise(payableRupees)

        val empPayments = paymentsByEmployee[employee.id] ?: emptyList()
        val paidPaise = empPayments.sumOf { it.amountPaise }
        val pendingPaise = (payablePaise - paidPaise).coerceAtLeast(0L)

        totalBilledSum += payablePaise
        totalCollectedSum += paidPaise

        val paymentStatus = when {
          payablePaise == 0L && paidPaise == 0L -> PaymentStatus.NO_DUES
          paidPaise >= payablePaise -> PaymentStatus.PAID
          paidPaise > 0L -> PaymentStatus.PARTIAL
          else -> PaymentStatus.PENDING
        }

        EmployeeReportItem(
          employee = employee,
          breakfastCount = breakfast,
          lunchCount = lunch,
          dinnerCount = dinner,
          totalMeals = total,
          payablePaise = payablePaise,
          payableRupees = payableRupees,
          paidPaise = paidPaise,
          pendingPaise = pendingPaise,
          paymentStatus = paymentStatus,
          payments = empPayments
        )
      }.sortedWith(
        compareByDescending<EmployeeReportItem> { it.totalMeals > 0 }
          .thenBy { it.employee.employeeCode }
      )

      val totalPendingSum = (totalBilledSum - totalCollectedSum).coerceAtLeast(0L)
      val colPct = if (totalBilledSum > 0) {
        ((totalCollectedSum.toFloat() / totalBilledSum.toFloat()) * 100f).coerceIn(0f, 100f)
      } else 100f

      RawMonthData(
        month = month,
        totalExpensesPaise = totalExpensesPaise,
        breakfastCount = breakdown.breakfastCount,
        lunchCount = breakdown.lunchCount,
        dinnerCount = breakdown.dinnerCount,
        totalMeals = breakdown.totalMeals,
        activeEmployeeCount = activeCount,
        employeeBills = billItems,
        totalBilledPaise = totalBilledSum,
        totalCollectedPaise = totalCollectedSum,
        totalPendingPaise = totalPendingSum,
        collectionPercentage = colPct
      )
    }
  }

  val uiState: StateFlow<ReportUiState> = combine(
    monthDataFlow,
    _searchQuery,
    _showOnlyWithMeals,
    _selectedEmployeeDetail
  ) { rawData, query, onlyWithMeals, selectedDetail ->
    val formattedMonth = try {
      val ym = YearMonth.parse(rawData.month, MONTH_FORMAT)
      ym.format(DISPLAY_MONTH_FORMAT)
    } catch (_: Exception) {
      rawData.month
    }

    val costPerMealRupees = CurrencyUtils.calculateCostPerMealRupees(
      totalExpensePaise = rawData.totalExpensesPaise,
      totalMeals = rawData.totalMeals
    )

    val filteredList = rawData.employeeBills.filter { item ->
      val matchesQuery = if (query.isBlank()) {
        true
      } else {
        item.employee.name.contains(query, ignoreCase = true) ||
            item.employee.employeeCode.contains(query, ignoreCase = true) ||
            item.employee.department.contains(query, ignoreCase = true)
      }

      val matchesMealFilter = if (onlyWithMeals) {
        item.totalMeals > 0
      } else {
        true
      }

      matchesQuery && matchesMealFilter
    }

    // Keep selected detail in sync if present
    val currentDetail = selectedDetail?.let { detail ->
      filteredList.firstOrNull { it.employee.id == detail.employee.id } ?: detail
    }

    ReportUiState(
      selectedMonth = rawData.month,
      formattedMonth = formattedMonth,
      totalExpensesPaise = rawData.totalExpensesPaise,
      totalMeals = rawData.totalMeals,
      breakfastCount = rawData.breakfastCount,
      lunchCount = rawData.lunchCount,
      dinnerCount = rawData.dinnerCount,
      activeEmployeeCount = rawData.activeEmployeeCount,
      costPerMealRupees = costPerMealRupees,
      totalBilledPaise = rawData.totalBilledPaise,
      totalCollectedPaise = rawData.totalCollectedPaise,
      totalPendingPaise = rawData.totalPendingPaise,
      collectionPercentage = rawData.collectionPercentage,
      employeeBills = rawData.employeeBills,
      filteredEmployeeBills = filteredList,
      searchQuery = query,
      showOnlyWithMeals = onlyWithMeals,
      selectedEmployeeForDetail = currentDetail,
      isLoading = false
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ReportUiState(
      selectedMonth = getCurrentMonthString(),
      isLoading = true
    )
  )

  class Factory(
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val expenseRepository: ExpenseRepository,
    private val paymentRepository: PaymentRepository? = null
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
        return ReportsViewModel(
          employeeRepository,
          attendanceRepository,
          expenseRepository,
          paymentRepository
        ) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
