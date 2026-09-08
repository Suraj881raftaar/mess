package com.example.data.repository

import com.example.data.dao.MenuDao
import com.example.data.entity.Menu
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MenuRepository(private val menuDao: MenuDao) {

  fun getMenuForDate(date: String): Flow<List<Menu>> = menuDao.getMenuForDate(date)

  fun getMenuForDateAndMeal(date: String, mealType: MealType): Flow<Menu?> =
    menuDao.getMenuForDateAndMeal(date, mealType)

  fun getMenuForDateRange(startDate: String, endDate: String): Flow<List<Menu>> =
    menuDao.getMenuForDateRange(startDate, endDate)

  suspend fun setMenu(date: String, mealType: MealType, description: String): Result<Long> {
    val trimmed = description.trim()
    val existing = menuDao.getMenuForDateAndMealOnce(date, mealType)
    val menu = existing?.copy(description = trimmed) ?: Menu(
      date = date,
      mealType = mealType,
      description = trimmed
    )
    val id = menuDao.upsert(menu)
    return Result.success(id)
  }

  suspend fun copyMenu(fromDate: String, toDate: String): Result<Int> {
    val sourceMenus = menuDao.getMenuForDate(fromDate).first()
    if (sourceMenus.isEmpty()) {
      return Result.failure(IllegalStateException("No menu found to copy from date $fromDate"))
    }
    val copied = sourceMenus.map { src ->
      val existing = menuDao.getMenuForDateAndMealOnce(toDate, src.mealType)
      existing?.copy(description = src.description) ?: Menu(
        date = toDate,
        mealType = src.mealType,
        description = src.description
      )
    }
    menuDao.upsertAll(copied)
    return Result.success(copied.size)
  }

  suspend fun deleteById(id: Long) = menuDao.deleteById(id)

  suspend fun deleteForDate(date: String) = menuDao.deleteForDate(date)
}
