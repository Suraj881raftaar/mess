package com.example.ui.dashboard

import com.example.data.entity.Expense
import com.example.data.entity.Menu
import com.example.data.model.MealType

data class DashboardUiState(
  val selectedDate: String = "",
  val formattedDate: String = "",
  val currentMonth: String = "",
  val formattedMonth: String = "",
  val messName: String = "Office Mess",
  val activeEmployeeCount: Int = 0,
  val todayBreakfastCount: Int = 0,
  val todayLunchCount: Int = 0,
  val todayDinnerCount: Int = 0,
  val totalTodayMeals: Int = 0,
  val currentMonthTotalExpensePaise: Long = 0L,
  val currentMonthTotalMeals: Int = 0,
  val currentMonthCostPerMealRupees: Double = 0.0,
  val currentMonthBilledPaise: Long = 0L,
  val currentMonthCollectedPaise: Long = 0L,
  val currentMonthPendingPaise: Long = 0L,
  val currentMonthCollectionPercentage: Float = 0f,
  val todayBreakfastMenu: Menu? = null,
  val todayLunchMenu: Menu? = null,
  val todayDinnerMenu: Menu? = null,
  val unmarkedMealsToday: List<MealType> = emptyList(),
  val missingMenuMealsToday: List<MealType> = emptyList(),
  val recentExpenses: List<Expense> = emptyList(),
  val isLoading: Boolean = true
)
