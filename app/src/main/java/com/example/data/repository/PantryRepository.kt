package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.dao.PantryDao
import com.example.data.entity.Expense
import com.example.data.entity.PantryItem
import com.example.data.entity.PantryUnit
import com.example.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class PantryRepository(
  private val pantryDao: PantryDao,
  private val expenseDao: ExpenseDao? = null
) {

  val allPantryItems: Flow<List<PantryItem>> = pantryDao.getAllPantryItems()
  val shoppingList: Flow<List<PantryItem>> = pantryDao.getShoppingList()
  val lowStockCount: Flow<Int> = pantryDao.getLowStockCount()

  fun getPantryItemsByCategory(category: ExpenseCategory): Flow<List<PantryItem>> =
    pantryDao.getPantryItemsByCategory(category)

  suspend fun addPantryItem(
    itemName: String,
    category: ExpenseCategory = ExpenseCategory.GROCERIES,
    currentQuantity: Double = 0.0,
    unit: PantryUnit = PantryUnit.KG,
    minThreshold: Double = 2.0,
    estimatedPricePaise: Long = 0L,
    isNeededOnShoppingList: Boolean = false,
    notes: String? = null
  ): Result<Long> {
    val trimmed = itemName.trim()
    if (trimmed.isBlank()) {
      return Result.failure(IllegalArgumentException("Item name cannot be blank"))
    }
    val item = PantryItem(
      itemName = trimmed,
      category = category,
      currentQuantity = currentQuantity.coerceAtLeast(0.0),
      unit = unit,
      minThreshold = minThreshold.coerceAtLeast(0.0),
      estimatedPricePaise = estimatedPricePaise.coerceAtLeast(0L),
      isNeededOnShoppingList = isNeededOnShoppingList || (currentQuantity <= minThreshold),
      notes = notes?.trim()?.ifBlank { null }
    )
    val id = pantryDao.insert(item)
    return Result.success(id)
  }

  suspend fun updateStock(id: Long, newQuantity: Double): Result<Unit> {
    val item = pantryDao.getItemByIdOnce(id) ?: return Result.failure(IllegalStateException("Item not found"))
    val clamped = newQuantity.coerceAtLeast(0.0)
    pantryDao.updateQuantity(id, clamped)
    // If quantity is now above threshold and was on shopping list, we can keep or adjust
    return Result.success(Unit)
  }

  suspend fun toggleShoppingList(id: Long, needed: Boolean): Result<Unit> {
    pantryDao.setShoppingListFlag(id, needed)
    return Result.success(Unit)
  }

  suspend fun markItemPurchased(
    itemId: Long,
    purchasedQuantity: Double,
    actualCostPaise: Long,
    vendor: String? = null,
    purchaseDate: String = LocalDate.now().toString()
  ): Result<Long?> {
    val item = pantryDao.getItemByIdOnce(itemId)
      ?: return Result.failure(IllegalStateException("Item not found"))

    // 1. Update pantry item quantity and reset shopping flag
    val updatedQuantity = item.currentQuantity + purchasedQuantity
    pantryDao.update(
      item.copy(
        currentQuantity = updatedQuantity,
        isNeededOnShoppingList = false,
        lastPurchasedDate = purchaseDate,
        updatedAt = System.currentTimeMillis()
      )
    )

    // 2. If expenseDao is provided, automatically record this purchase as an Expense!
    var expenseId: Long? = null
    if (expenseDao != null && actualCostPaise > 0L) {
      val expense = Expense(
        date = purchaseDate,
        description = "${item.itemName} (${purchasedQuantity} ${item.unit.symbol})",
        category = item.category,
        amountPaise = actualCostPaise,
        vendor = vendor?.trim()?.ifBlank { null }
      )
      expenseId = expenseDao.insert(expense)
    }

    return Result.success(expenseId)
  }

  suspend fun deleteItem(id: Long) = pantryDao.deleteById(id)

  suspend fun seedDefaultPantryIfEmpty() {
    // Helpful starter inventory for office mess
    val defaults = listOf(
      PantryItem(itemName = "Rice (Basmati/Kolam)", category = ExpenseCategory.GROCERIES, currentQuantity = 15.0, unit = PantryUnit.KG, minThreshold = 5.0, estimatedPricePaise = 90000L),
      PantryItem(itemName = "Toor Dal", category = ExpenseCategory.GROCERIES, currentQuantity = 6.0, unit = PantryUnit.KG, minThreshold = 3.0, estimatedPricePaise = 75000L),
      PantryItem(itemName = "Wheat Flour (Atta)", category = ExpenseCategory.GROCERIES, currentQuantity = 20.0, unit = PantryUnit.KG, minThreshold = 8.0, estimatedPricePaise = 85000L),
      PantryItem(itemName = "Cooking Oil (Sunflower)", category = ExpenseCategory.GROCERIES, currentQuantity = 4.0, unit = PantryUnit.LITRE, minThreshold = 2.0, estimatedPricePaise = 60000L),
      PantryItem(itemName = "Onions", category = ExpenseCategory.VEGETABLES, currentQuantity = 8.0, unit = PantryUnit.KG, minThreshold = 4.0, estimatedPricePaise = 30000L),
      PantryItem(itemName = "Potatoes", category = ExpenseCategory.VEGETABLES, currentQuantity = 7.0, unit = PantryUnit.KG, minThreshold = 3.0, estimatedPricePaise = 25000L),
      PantryItem(itemName = "Tomatoes", category = ExpenseCategory.VEGETABLES, currentQuantity = 3.0, unit = PantryUnit.KG, minThreshold = 2.0, estimatedPricePaise = 15000L),
      PantryItem(itemName = "Milk (Toned)", category = ExpenseCategory.DAIRY, currentQuantity = 2.0, unit = PantryUnit.LITRE, minThreshold = 2.0, estimatedPricePaise = 12000L, isNeededOnShoppingList = true),
      PantryItem(itemName = "Paneer (Fresh)", category = ExpenseCategory.DAIRY, currentQuantity = 0.5, unit = PantryUnit.KG, minThreshold = 1.0, estimatedPricePaise = 22000L, isNeededOnShoppingList = true),
      PantryItem(itemName = "LPG Cooking Gas", category = ExpenseCategory.GAS_FUEL, currentQuantity = 1.0, unit = PantryUnit.CYLINDER, minThreshold = 1.0, estimatedPricePaise = 110000L)
    )
    pantryDao.insertAll(defaults)
  }
}
