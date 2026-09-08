package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Menu
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MenuRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
  private val employeeRepository: EmployeeRepository,
  private val attendanceRepository: AttendanceRepository,
  private val menuRepository: MenuRepository,
  private val expenseRepository: ExpenseRepository
) : ViewModel() {

  companion object {
    private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    private val MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)
    private val DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.US)
    private val DISPLAY_MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    fun getTodayString(): String = LocalDate.now().format(DATE_FORMAT)
    fun getCurrentMonthString(): String = YearMonth.now().format(MONTH_FORMAT)
  }

  private val _selectedDate = MutableStateFlow(getTodayString())
  val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

  fun setDate(date: String) {
    _selectedDate.value = date
  }

  fun resetToToday() {
    _selectedDate.value = getTodayString()
  }

  private data class TodayMealData(
    val date: String,
    val breakfastCount: Int = 0,
    val lunchCount: Int = 0,
    val dinnerCount: Int = 0,
    val menus: List<Menu> = emptyList()
  )

  private data class MonthFinancialData(
    val month: String,
    val totalExpensePaise: Long = 0L,
    val totalMeals: Int = 0,
    val costPerMealRupees: Double = 0.0
  )

  // Reactively track today's meal counts and menus
  private val todayMealDataFlow = _selectedDate.flatMapLatest { date ->
    combine(
      attendanceRepository.getPresentCountForDateAndMeal(date, MealType.BREAKFAST),
      attendanceRepository.getPresentCountForDateAndMeal(date, MealType.LUNCH),
      attendanceRepository.getPresentCountForDateAndMeal(date, MealType.DINNER),
      menuRepository.getMenuForDate(date)
    ) { breakfastCount, lunchCount, dinnerCount, menus ->
      TodayMealData(
        date = date,
        breakfastCount = breakfastCount,
        lunchCount = lunchCount,
        dinnerCount = dinnerCount,
        menus = menus
      )
    }
  }

  // Reactively track month financial stats (expenses, meals, cost per meal)
  private val monthFinancialDataFlow = _selectedDate.flatMapLatest { date ->
    val monthPrefix = try {
      val parsed = LocalDate.parse(date, DATE_FORMAT)
      parsed.format(MONTH_FORMAT)
    } catch (_: Exception) {
      getCurrentMonthString()
    }

    combine(
      expenseRepository.getTotalExpensesPaiseForMonth(monthPrefix),
      attendanceRepository.getTotalMealsForMonth(monthPrefix)
    ) { totalExpensesPaise, totalMeals ->
      val costPerMeal = CurrencyUtils.calculateCostPerMealRupees(totalExpensesPaise, totalMeals)
      MonthFinancialData(
        month = monthPrefix,
        totalExpensePaise = totalExpensesPaise,
        totalMeals = totalMeals,
        costPerMealRupees = costPerMeal
      )
    }
  }

  val uiState: StateFlow<DashboardUiState> = combine(
    employeeRepository.activeEmployeeCount,
    todayMealDataFlow,
    monthFinancialDataFlow
  ) { activeCount, todayData, monthData ->
    val formattedDate = try {
      LocalDate.parse(todayData.date, DATE_FORMAT).format(DISPLAY_DATE_FORMAT)
    } catch (_: Exception) {
      todayData.date
    }

    val formattedMonth = try {
      YearMonth.parse(monthData.month, MONTH_FORMAT).format(DISPLAY_MONTH_FORMAT)
    } catch (_: Exception) {
      monthData.month
    }

    val breakfastMenu = todayData.menus.firstOrNull { it.mealType == MealType.BREAKFAST }
    val lunchMenu = todayData.menus.firstOrNull { it.mealType == MealType.LUNCH }
    val dinnerMenu = todayData.menus.firstOrNull { it.mealType == MealType.DINNER }

    DashboardUiState(
      selectedDate = todayData.date,
      formattedDate = formattedDate,
      currentMonth = monthData.month,
      formattedMonth = formattedMonth,
      activeEmployeeCount = activeCount,
      todayBreakfastCount = todayData.breakfastCount,
      todayLunchCount = todayData.lunchCount,
      todayDinnerCount = todayData.dinnerCount,
      totalTodayMeals = todayData.breakfastCount + todayData.lunchCount + todayData.dinnerCount,
      currentMonthTotalExpensePaise = monthData.totalExpensePaise,
      currentMonthTotalMeals = monthData.totalMeals,
      currentMonthCostPerMealRupees = monthData.costPerMealRupees,
      todayBreakfastMenu = breakfastMenu,
      todayLunchMenu = lunchMenu,
      todayDinnerMenu = dinnerMenu,
      isLoading = false
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DashboardUiState(
      selectedDate = getTodayString(),
      currentMonth = getCurrentMonthString(),
      isLoading = true
    )
  )

  class Factory(
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val menuRepository: MenuRepository,
    private val expenseRepository: ExpenseRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
        return DashboardViewModel(
          employeeRepository,
          attendanceRepository,
          menuRepository,
          expenseRepository
        ) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
