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
    if (trimmed.isBlank()) {
      val existing = menuDao.getMenuForDateAndMealOnce(date, mealType)
      if (existing != null) {
        menuDao.deleteById(existing.id)
      }
      return Result.success(0L)
    }
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
    val sourceMenus = menuDao.getMenuForDate(fromDate).first().filter { it.description.isNotBlank() }
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

    val b = breakfast?.trim()?.ifBlank { null }
    val l = lunch?.trim()?.ifBlank { null }
    val d = dinner?.trim()?.ifBlank { null }

    if (b == null && l == null && d == null) {
      return Result.failure(IllegalArgumentException("Template must contain at least one meal item (Breakfast, Lunch, or Dinner)."))
    }

    val template = MenuTemplate(
      templateName = name,
      breakfast = b,
      lunch = l,
      dinner = d
    )
    val id = templateDao.insert(template)
    return Result.success(id)
  }

  suspend fun updateTemplate(
    templateId: Long,
    templateName: String,
    breakfast: String?,
    lunch: String?,
    dinner: String?
  ): Result<Unit> {
    if (templateDao == null) return Result.failure(IllegalStateException("Template DAO not available"))
    val name = templateName.trim()
    if (name.isBlank()) return Result.failure(IllegalArgumentException("Template name cannot be blank"))

    val b = breakfast?.trim()?.ifBlank { null }
    val l = lunch?.trim()?.ifBlank { null }
    val d = dinner?.trim()?.ifBlank { null }

    if (b == null && l == null && d == null) {
      return Result.failure(IllegalArgumentException("Template must contain at least one meal item."))
    }

    val existing = templateDao.getTemplateByIdOnce(templateId)
      ?: return Result.failure(IllegalStateException("Template not found"))

    val updated = existing.copy(
      templateName = name,
      breakfast = b,
      lunch = l,
      dinner = d
    )
    templateDao.update(updated)
    return Result.success(Unit)
  }

  suspend fun saveDayAsTemplate(templateName: String, date: String): Result<Long> {
    val dayMenus = menuDao.getMenuForDate(date).first()
    val breakfast = dayMenus.find { it.mealType == MealType.BREAKFAST }?.description
    val lunch = dayMenus.find { it.mealType == MealType.LUNCH }?.description
    val dinner = dayMenus.find { it.mealType == MealType.DINNER }?.description

    val b = breakfast?.trim()?.ifBlank { null }
    val l = lunch?.trim()?.ifBlank { null }
    val d = dinner?.trim()?.ifBlank { null }

    if (b == null && l == null && d == null) {
      return Result.failure(IllegalStateException("No meals planned on $date to save as template. Please plan at least one meal first."))
    }

    return saveAsTemplate(templateName, b, l, d)
  }

  suspend fun applyTemplateToDate(template: MenuTemplate, date: String): Result<Unit> {
    val b = template.breakfast?.trim()?.ifBlank { null }
    val l = template.lunch?.trim()?.ifBlank { null }
    val d = template.dinner?.trim()?.ifBlank { null }

    if (b == null && l == null && d == null) {
      return Result.failure(IllegalStateException("Template '${template.templateName}' has no meal items defined."))
    }

    if (b != null) setMenu(date, MealType.BREAKFAST, b)
    if (l != null) setMenu(date, MealType.LUNCH, l)
    if (d != null) setMenu(date, MealType.DINNER, d)
    return Result.success(Unit)
  }

  suspend fun applyTemplateToDate(templateId: Long, date: String): Result<Unit> {
    if (templateDao == null) return Result.failure(IllegalStateException("Template DAO not available"))
    val template = templateDao.getTemplateByIdOnce(templateId)
      ?: return Result.failure(IllegalStateException("Template not found"))
    return applyTemplateToDate(template, date)
  }

  suspend fun seedDefaultTemplatesIfEmpty(): Result<Int> {
    if (templateDao == null) return Result.failure(IllegalStateException("Template DAO not available"))
    val existing = templateDao.getAllTemplates().first()
    if (existing.isNotEmpty()) return Result.success(0)
    return restoreDefaultTemplates()
  }

  suspend fun restoreDefaultTemplates(): Result<Int> {
    if (templateDao == null) return Result.failure(IllegalStateException("Template DAO not available"))
    val defaultList = getDefaultTemplates()
    templateDao.insertAll(defaultList)
    return Result.success(defaultList.size)
  }

  companion object {
    fun getDefaultTemplates(): List<MenuTemplate> {
      return listOf(
        MenuTemplate(
          templateName = "North Indian Regular",
          breakfast = "Poha, Boiled Eggs / Banana, Masala Chai",
          lunch = "Dal Tadka, Seasonal Sabzi, Paneer, Jeera Rice, Phulka & Salad",
          dinner = "Aloo Matar, Dal Makhani, Steamed Rice & Roti"
        ),
        MenuTemplate(
          templateName = "South Indian Special",
          breakfast = "Idli, Medu Vada, Coconut Chutney & Sambar",
          lunch = "Avial, Sambar, Rasam, Beetroot Poriyal, Steamed Rice, Papad & Curd",
          dinner = "Lemon Rice / Curd Rice with Potato Roast & Pickle"
        ),
        MenuTemplate(
          templateName = "Weekend Feast",
          breakfast = "Aloo Paratha, Fresh Curd, Pickle & Mint Tea",
          lunch = "Paneer Biryani / Chicken Biryani, Mirchi Ka Salan, Raita & Gulab Jamun",
          dinner = "Pav Bhaji, Veg Pulao & Roasted Papad"
        ),
        MenuTemplate(
          templateName = "Light & Healthy Diet",
          breakfast = "Oats Upma, Boiled Sprouts Salad & Herbal Green Tea",
          lunch = "Moong Dal Khichdi, Gujarati Kadhi, Steamed Veggies & Curd",
          dinner = "Mixed Vegetable Soup, Dalia / Multigrain Phulka & Cucumber Salad"
        ),
        MenuTemplate(
          templateName = "Punjabi Dhaba Special",
          breakfast = "Chole Bhature with Pickled Onions & Sweet Lassi",
          lunch = "Rajma Masala, Jeera Rice, Butter Phulka & Kachumber Salad",
          dinner = "Paneer Tikka Masala, Dal Makhani, Tandoori Roti & Gulab Jamun"
        ),
        MenuTemplate(
          templateName = "Fast Food & Chinese Night",
          breakfast = "Vegetable Grilled Sandwich & Filter Coffee",
          lunch = "Veg Fried Rice, Veg Manchurian Gravy & Crispy Spring Rolls",
          dinner = "Hakka Noodles, Chili Paneer Gravy & Hot & Sour Soup"
        ),
        MenuTemplate(
          templateName = "Quick Working Day",
          breakfast = "Rava Upma, Coconut Chutney & Filter Coffee",
          lunch = "Dal Fry, Aloo Gobi, Steamed Rice & Roti",
          dinner = "Egg Curry / Paneer Bhurji, Tawa Paratha, Rice & Onion Rings"
        )
      )
    }
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
