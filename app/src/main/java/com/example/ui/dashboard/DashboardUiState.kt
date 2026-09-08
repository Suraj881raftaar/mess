package com.example.ui.dashboard

import com.example.data.entity.Menu

data class DashboardUiState(
  val selectedDate: String = "",
  val formattedDate: String = "",
  val currentMonth: String = "",
  val formattedMonth: String = "",
  val activeEmployeeCount: Int = 0,
  val todayBreakfastCount: Int = 0,
  val todayLunchCount: Int = 0,
  val todayDinnerCount: Int = 0,
  val totalTodayMeals: Int = 0,
  val currentMonthTotalExpensePaise: Long = 0L,
  val currentMonthTotalMeals: Int = 0,
  val currentMonthCostPerMealRupees: Double = 0.0,
  val todayBreakfastMenu: Menu? = null,
  val todayLunchMenu: Menu? = null,
  val todayDinnerMenu: Menu? = null,
  val isLoading: Boolean = true
)
