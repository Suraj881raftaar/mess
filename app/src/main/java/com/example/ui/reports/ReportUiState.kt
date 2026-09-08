package com.example.ui.reports

import com.example.data.entity.Employee

data class EmployeeReportItem(
  val employee: Employee,
  val breakfastCount: Int = 0,
  val lunchCount: Int = 0,
  val dinnerCount: Int = 0,
  val totalMeals: Int = 0,
  val payablePaise: Long = 0L,
  val payableRupees: Double = 0.0
)

data class ReportUiState(
  val selectedMonth: String = "",
  val formattedMonth: String = "",
  val totalExpensesPaise: Long = 0L,
  val totalMeals: Int = 0,
  val breakfastCount: Int = 0,
  val lunchCount: Int = 0,
  val dinnerCount: Int = 0,
  val activeEmployeeCount: Int = 0,
  val costPerMealRupees: Double = 0.0,
  val employeeBills: List<EmployeeReportItem> = emptyList(),
  val filteredEmployeeBills: List<EmployeeReportItem> = emptyList(),
  val searchQuery: String = "",
  val showOnlyWithMeals: Boolean = false,
  val selectedEmployeeForDetail: EmployeeReportItem? = null,
  val isLoading: Boolean = true
)
