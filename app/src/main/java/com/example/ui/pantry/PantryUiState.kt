package com.example.ui.pantry

import com.example.data.entity.PantryItem
import com.example.data.model.ExpenseCategory

data class PantryUiState(
  val items: List<PantryItem> = emptyList(),
  val shoppingList: List<PantryItem> = emptyList(),
  val selectedTab: PantryTab = PantryTab.ALL_ITEMS,
  val selectedCategoryFilter: ExpenseCategory? = null,
  val lowStockCount: Int = 0,
  val estimatedShoppingCostPaise: Long = 0L,
  val searchQuery: String = "",
  val isLoading: Boolean = false,
  val userMessage: String? = null
)

enum class PantryTab(val title: String) {
  ALL_ITEMS("Pantry Stock"),
  SHOPPING_LIST("Shopping List"),
  BULK_RECIPES("Cook's Calculator")
}
