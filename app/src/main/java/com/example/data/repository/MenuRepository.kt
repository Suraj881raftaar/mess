package com.example.data.repository

import com.example.data.dao.MenuDao
import com.example.data.dao.MenuTemplateDao
import com.example.data.entity.Menu
import com.example.data.entity.MenuTemplate
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MenuRepository(
  private val menuDao: MenuDao,
  private val templateDao: MenuTemplateDao? = null
) {

  val allTemplates: Flow<List<MenuTemplate>> =
    templateDao?.getAllTemplates() ?: kotlinx.coroutines.flow.flowOf(emptyList())

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

  suspend fun copyWeek(fromStartMonday: String, toStartMonday: String): Result<Int> {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val fromStart = LocalDate.parse(fromStartMonday, formatter)
    val toStart = LocalDate.parse(toStartMonday, formatter)

    var totalCopied = 0
    for (i in 0..6) {
      val srcDate = fromStart.plusDays(i.toLong()).format(formatter)
      val destDate = toStart.plusDays(i.toLong()).format(formatter)
      val result = copyMenu(srcDate, destDate)
      if (result.isSuccess) {
        totalCopied += (result.getOrNull() ?: 0)
      }
    }
    return Result.success(totalCopied)
  }

  suspend fun copyWeekMenu(fromStartMonday: String, toStartMonday: String): Result<Int> =
    copyWeek(fromStartMonday, toStartMonday)

  suspend fun saveAsTemplate(
    templateName: String,
    breakfast: String?,
    lunch: String?,
    dinner: String?
  ): Result<Long> {
    if (templateDao == null) return Result.failure(IllegalStateException("Template DAO not available"))
    val name = templateName.trim()
    if (name.isBlank()) return Result.failure(IllegalArgumentException("Template name cannot be blank"))

    val template = MenuTemplate(
      templateName = name,
      breakfast = breakfast?.trim()?.ifBlank { null },
      lunch = lunch?.trim()?.ifBlank { null },
      dinner = dinner?.trim()?.ifBlank { null }
    )
    val id = templateDao.insert(template)
    return Result.success(id)
  }

  suspend fun saveDayAsTemplate(templateName: String, date: String): Result<Long> {
    val dayMenus = menuDao.getMenuForDate(date).first()
    val breakfast = dayMenus.find { it.mealType == MealType.BREAKFAST }?.description
    val lunch = dayMenus.find { it.mealType == MealType.LUNCH }?.description
    val dinner = dayMenus.find { it.mealType == MealType.DINNER }?.description

    return saveAsTemplate(templateName, breakfast, lunch, dinner)
  }

  suspend fun applyTemplateToDate(template: MenuTemplate, date: String): Result<Unit> {
    template.breakfast?.let { setMenu(date, MealType.BREAKFAST, it) }
    template.lunch?.let { setMenu(date, MealType.LUNCH, it) }
    template.dinner?.let { setMenu(date, MealType.DINNER, it) }
    return Result.success(Unit)
  }

  suspend fun applyTemplateToDate(templateId: Long, date: String): Result<Unit> {
    if (templateDao == null) return Result.failure(IllegalStateException("Template DAO not available"))
    val template = templateDao.getTemplateByIdOnce(templateId)
      ?: return Result.failure(IllegalStateException("Template not found"))
    return applyTemplateToDate(template, date)
  }

  suspend fun deleteTemplate(template: MenuTemplate) {
    templateDao?.delete(template)
  }

  suspend fun deleteTemplateById(id: Long) {
    templateDao?.deleteById(id)
  }

  suspend fun deleteById(id: Long) = menuDao.deleteById(id)

  suspend fun deleteForDate(date: String) = menuDao.deleteForDate(date)

  suspend fun clearMeal(date: String, mealType: MealType) {
    val existing = menuDao.getMenuForDateAndMealOnce(date, mealType)
    if (existing != null) {
      menuDao.deleteById(existing.id)
    }
  }
}
