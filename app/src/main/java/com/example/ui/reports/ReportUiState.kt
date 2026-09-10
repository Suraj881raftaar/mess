package com.example.ui.reports

import com.example.data.entity.Employee
import com.example.data.entity.Payment

enum class PaymentStatus {
  PAID,
  PARTIAL,
  PENDING,
  NO_DUES
}

enum class PaymentFilterTab(val label: String) {
  ALL("All Staff"),
  HAS_DUES("With Dues"),
  SETTLED("Settled")
}

data class EmployeeReportItem(
  val employee: Employee,
  val breakfastCount: Int = 0,
  val lunchCount: Int = 0,
  val dinnerCount: Int = 0,
  val totalMeals: Int = 0,
  val payablePaise: Long = 0L,
  val payableRupees: Double = 0.0,
  val paidPaise: Long = 0L,
  val pendingPaise: Long = 0L,
  val paymentStatus: PaymentStatus = PaymentStatus.NO_DUES,
  val payments: List<Payment> = emptyList()
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
  val totalBilledPaise: Long = 0L,
  val totalCollectedPaise: Long = 0L,
  val totalPendingPaise: Long = 0L,
  val collectionPercentage: Float = 0f,
  val employeeBills: List<EmployeeReportItem> = emptyList(),
  val filteredEmployeeBills: List<EmployeeReportItem> = emptyList(),
  val searchQuery: String = "",
  val showOnlyWithMeals: Boolean = false,
  val paymentFilter: PaymentFilterTab = PaymentFilterTab.ALL,
  val selectedEmployeeForDetail: EmployeeReportItem? = null,
  val isLoading: Boolean = true
)
