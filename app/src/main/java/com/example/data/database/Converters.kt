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

  @TypeConverter
  fun fromPantryUnit(unit: com.example.data.entity.PantryUnit?): String? {
    return unit?.name
  }

  @TypeConverter
  fun toPantryUnit(name: String?): com.example.data.entity.PantryUnit? {
    return name?.let { runCatching { com.example.data.entity.PantryUnit.valueOf(it) }.getOrNull() } ?: com.example.data.entity.PantryUnit.KG
  }

  @TypeConverter
  fun fromDietaryPreference(pref: com.example.data.model.DietaryPreference?): String? {
    return pref?.name
  }

  @TypeConverter
  fun toDietaryPreference(name: String?): com.example.data.model.DietaryPreference? {
    return com.example.data.model.DietaryPreference.fromString(name)
  }
}
