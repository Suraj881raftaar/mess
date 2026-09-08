package com.example.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Menu
import com.example.data.model.MealType
import com.example.data.repository.MenuRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(
  private val menuRepository: MenuRepository
) : ViewModel() {

  companion object {
    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val DISPLAY_DATE_FORMAT = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)
    val DAY_NAME_FORMAT = SimpleDateFormat("EEEE", Locale.US)
    val SHORT_DAY_FORMAT = SimpleDateFormat("dd MMM", Locale.US)
    val WEEK_RANGE_FORMAT = SimpleDateFormat("dd MMM", Locale.US)

    fun getTodayString(): String = DATE_FORMAT.format(Date())

    fun getMondayOfWeek(dateString: String): String {
      val cal = Calendar.getInstance(Locale.US)
      cal.time = DATE_FORMAT.parse(dateString) ?: Date()
      // Enforce Monday as first day of week
      cal.firstDayOfWeek = Calendar.MONDAY
      var dow = cal.get(Calendar.DAY_OF_WEEK)
      var daysBack = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
      cal.add(Calendar.DAY_OF_YEAR, -daysBack)
      return DATE_FORMAT.format(cal.time)
    }

    fun addDays(dateString: String, days: Int): String {
      val cal = Calendar.getInstance(Locale.US)
      cal.time = DATE_FORMAT.parse(dateString) ?: Date()
      cal.add(Calendar.DAY_OF_YEAR, days)
      return DATE_FORMAT.format(cal.time)
    }
  }

  private val _menuMode = MutableStateFlow(MenuMode.DAILY)
  val menuMode: StateFlow<MenuMode> = _menuMode.asStateFlow()

  private val _selectedDate = MutableStateFlow(getTodayString())
  val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

  private val _selectedWeekMonday = MutableStateFlow(getMondayOfWeek(getTodayString()))
  val selectedWeekMonday: StateFlow<String> = _selectedWeekMonday.asStateFlow()

  private val _editMealDialogState = MutableStateFlow<EditMealDialogState?>(null)
  val editMealDialogState: StateFlow<EditMealDialogState?> = _editMealDialogState.asStateFlow()

  private val _copyDayDialogState = MutableStateFlow<CopyDayDialogState?>(null)
  val copyDayDialogState: StateFlow<CopyDayDialogState?> = _copyDayDialogState.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val dailyMenuState: StateFlow<DailyMenuState> = _selectedDate
    .flatMapLatest { date ->
      menuRepository.getMenuForDate(date).map { list ->
        val parsedDate = runCatching { DATE_FORMAT.parse(date) }.getOrNull() ?: Date()
        DailyMenuState(
          date = date,
          formattedDate = DISPLAY_DATE_FORMAT.format(parsedDate),
          isToday = date == getTodayString(),
          breakfast = list.find { it.mealType == MealType.BREAKFAST },
          lunch = list.find { it.mealType == MealType.LUNCH },
          dinner = list.find { it.mealType == MealType.DINNER },
          isLoading = false
        )
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = DailyMenuState(date = getTodayString(), isLoading = true)
    )

  @OptIn(ExperimentalCoroutinesApi::class)
  val weeklyMenuState: StateFlow<WeeklyMenuState> = _selectedWeekMonday
    .flatMapLatest { monday ->
      val sunday = addDays(monday, 6)
      menuRepository.getMenuForDateRange(monday, sunday).map { allMenus ->
        val menuMap = allMenus.groupBy { it.date }
        val today = getTodayString()

        val days = (0..6).map { offset ->
          val curDate = addDays(monday, offset)
          val parsedDate = runCatching { DATE_FORMAT.parse(curDate) }.getOrNull() ?: Date()
          val dayMenus = menuMap[curDate] ?: emptyList()
          DayMenuItem(
            date = curDate,
            dayName = DAY_NAME_FORMAT.format(parsedDate),
            formattedDate = SHORT_DAY_FORMAT.format(parsedDate),
            isToday = curDate == today,
            breakfast = dayMenus.find { it.mealType == MealType.BREAKFAST }?.description ?: "",
            lunch = dayMenus.find { it.mealType == MealType.LUNCH }?.description ?: "",
            dinner = dayMenus.find { it.mealType == MealType.DINNER }?.description ?: ""
          )
        }

        val monDate = runCatching { DATE_FORMAT.parse(monday) }.getOrNull() ?: Date()
        val sunDate = runCatching { DATE_FORMAT.parse(sunday) }.getOrNull() ?: Date()
        val rangeLabel = "${WEEK_RANGE_FORMAT.format(monDate)} – ${WEEK_RANGE_FORMAT.format(sunDate)}"

        WeeklyMenuState(
          startMonday = monday,
          endSunday = sunday,
          formattedWeekRange = rangeLabel,
          days = days,
          isLoading = false
        )
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = WeeklyMenuState(startMonday = getMondayOfWeek(getTodayString()), endSunday = "", isLoading = true)
    )

  fun setMenuMode(mode: MenuMode) {
    _menuMode.value = mode
  }

  fun setDate(date: String) {
    _selectedDate.value = date
  }

  fun selectToday() {
    val today = getTodayString()
    _selectedDate.value = today
    _selectedWeekMonday.value = getMondayOfWeek(today)
  }

  fun selectPreviousDay() {
    _selectedDate.value = addDays(_selectedDate.value, -1)
  }

  fun selectNextDay() {
    _selectedDate.value = addDays(_selectedDate.value, 1)
  }

  fun selectCurrentWeek() {
    _selectedWeekMonday.value = getMondayOfWeek(getTodayString())
  }

  fun selectPreviousWeek() {
    _selectedWeekMonday.value = addDays(_selectedWeekMonday.value, -7)
  }

  fun selectNextWeek() {
    _selectedWeekMonday.value = addDays(_selectedWeekMonday.value, 7)
  }

  fun navigateToDateFromWeekly(date: String) {
    _selectedDate.value = date
    _menuMode.value = MenuMode.DAILY
  }

  fun openEditMealDialog(date: String, mealType: MealType, currentDescription: String) {
    _editMealDialogState.value = EditMealDialogState(
      date = date,
      mealType = mealType,
      currentDescription = currentDescription
    )
  }

  fun closeEditMealDialog() {
    _editMealDialogState.value = null
  }

  suspend fun saveMealMenuSync(date: String, mealType: MealType, description: String): Boolean {
    val trimmed = description.trim()
    return try {
      menuRepository.setMenu(date, mealType, trimmed)
      _userMessage.value = "${mealType.displayName} menu saved successfully."
      closeEditMealDialog()
      true
    } catch (e: Exception) {
      _userMessage.value = "Failed to save menu: ${e.message}"
      false
    }
  }

  fun saveMealMenu(date: String, mealType: MealType, description: String) {
    viewModelScope.launch {
      saveMealMenuSync(date, mealType, description)
    }
  }

  fun openCopyDayDialog(sourceDate: String) {
    val defaultTarget = addDays(sourceDate, 1)
    val state = dailyMenuState.value
    val b = state.breakfast?.description?.takeIf { it.isNotBlank() } ?: "None"
    val l = state.lunch?.description?.takeIf { it.isNotBlank() } ?: "None"
    val d = state.dinner?.description?.takeIf { it.isNotBlank() } ?: "None"
    val summary = "B: $b | L: $l | D: $d"

    _copyDayDialogState.value = CopyDayDialogState(
      sourceDate = sourceDate,
      targetDate = defaultTarget,
      sourceSummary = summary
    )
  }

  fun setCopyTargetDate(targetDate: String) {
    val current = _copyDayDialogState.value ?: return
    _copyDayDialogState.value = current.copy(targetDate = targetDate)
  }

  fun closeCopyDayDialog() {
    _copyDayDialogState.value = null
  }

  suspend fun copyMenuSync(fromDate: String, toDate: String): Result<Int> {
    if (fromDate == toDate) {
      val errorMsg = "Cannot copy menu to the same date."
      _userMessage.value = errorMsg
      return Result.failure(IllegalArgumentException(errorMsg))
    }
    val result = menuRepository.copyMenu(fromDate, toDate)
    if (result.isSuccess) {
      val count = result.getOrDefault(0)
      _userMessage.value = "Copied $count meal items to $toDate."
      closeCopyDayDialog()
    } else {
      _userMessage.value = result.exceptionOrNull()?.message ?: "Failed to copy menu."
    }
    return result
  }

  fun confirmCopyMenu() {
    val current = _copyDayDialogState.value ?: return
    viewModelScope.launch {
      copyMenuSync(current.sourceDate, current.targetDate)
    }
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  class Factory(
    private val menuRepository: MenuRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MenuViewModel(menuRepository) as T
    }
  }
}
