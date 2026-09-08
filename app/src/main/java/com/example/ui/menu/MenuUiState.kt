package com.example.ui.menu

import com.example.data.entity.Menu
import com.example.data.model.MealType

enum class MenuMode {
  DAILY,
  WEEKLY
}

data class DailyMenuState(
  val date: String,
  val formattedDate: String = "",
  val isToday: Boolean = false,
  val breakfast: Menu? = null,
  val lunch: Menu? = null,
  val dinner: Menu? = null,
  val isLoading: Boolean = false
) {
  val hasAnyMeal: Boolean
    get() = breakfast != null || lunch != null || dinner != null
}

data class DayMenuItem(
  val date: String,
  val dayName: String,
  val formattedDate: String,
  val isToday: Boolean,
  val breakfast: String = "",
  val lunch: String = "",
  val dinner: String = ""
) {
  val hasContent: Boolean
    get() = breakfast.isNotBlank() || lunch.isNotBlank() || dinner.isNotBlank()
}

data class WeeklyMenuState(
  val startMonday: String,
  val endSunday: String,
  val formattedWeekRange: String = "",
  val days: List<DayMenuItem> = emptyList(),
  val isLoading: Boolean = false
)

data class EditMealDialogState(
  val date: String,
  val mealType: MealType,
  val currentDescription: String
)

data class CopyDayDialogState(
  val sourceDate: String,
  val targetDate: String,
  val sourceSummary: String
)
