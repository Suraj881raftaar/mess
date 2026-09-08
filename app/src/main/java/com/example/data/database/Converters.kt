package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType

class Converters {
  @TypeConverter
  fun fromMealType(mealType: MealType?): String? {
    return mealType?.name
  }

  @TypeConverter
  fun toMealType(name: String?): MealType? {
    return name?.let { MealType.valueOf(it) }
  }

  @TypeConverter
  fun fromExpenseCategory(category: ExpenseCategory?): String? {
    return category?.name
  }

  @TypeConverter
  fun toExpenseCategory(name: String?): ExpenseCategory? {
    return name?.let { ExpenseCategory.valueOf(it) }
  }
}
