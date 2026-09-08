package com.example.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.PantryItem
import com.example.data.entity.PantryUnit
import com.example.data.model.ExpenseCategory
import com.example.data.repository.PantryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class PantryFilterState(
  val tab: PantryTab,
  val categoryFilter: ExpenseCategory?,
  val search: String,
  val msg: String?
)

class PantryViewModel(
  private val pantryRepository: PantryRepository
) : ViewModel() {

  private val _selectedTab = MutableStateFlow(PantryTab.ALL_ITEMS)
  private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
  private val _searchQuery = MutableStateFlow("")
  private val _userMessage = MutableStateFlow<String?>(null)

  private val _filtersFlow = combine(
    _selectedTab,
    _selectedCategoryFilter,
    _searchQuery,
    _userMessage
  ) { tab, cat, search, msg ->
    PantryFilterState(tab, cat, search, msg)
  }

  val uiState: StateFlow<PantryUiState> = combine(
    pantryRepository.allPantryItems,
    pantryRepository.shoppingList,
    _filtersFlow
  ) { items, shopping, filter ->
    val query = filter.search.trim().lowercase()

    val filteredItems = items.filter { item ->
      val matchCat = filter.categoryFilter == null || item.category == filter.categoryFilter
      val matchSearch = query.isEmpty() || item.itemName.lowercase().contains(query)
      matchCat && matchSearch
    }

    val filteredShopping = shopping.filter { item ->
      query.isEmpty() || item.itemName.lowercase().contains(query)
    }

    val estimatedCost = filteredShopping.sumOf { it.estimatedPricePaise }
    val lowStock = items.count { it.currentQuantity <= it.minThreshold }

    PantryUiState(
      items = filteredItems,
      shoppingList = filteredShopping,
      selectedTab = filter.tab,
      selectedCategoryFilter = filter.categoryFilter,
      lowStockCount = lowStock,
      estimatedShoppingCostPaise = estimatedCost,
      searchQuery = filter.search,
      isLoading = false,
      userMessage = filter.msg
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = PantryUiState(isLoading = true)
  )

  init {
    viewModelScope.launch {
      pantryRepository.allPantryItems.collect { list ->
        if (list.isEmpty()) {
          pantryRepository.seedDefaultPantryIfEmpty()
        }
      }
    }
  }

  fun setTab(tab: PantryTab) {
    _selectedTab.value = tab
  }

  fun setCategoryFilter(category: ExpenseCategory?) {
    _selectedCategoryFilter.value = category
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  fun addOrUpdateItem(
    id: Long = 0L,
    name: String,
    category: ExpenseCategory,
    quantity: Double,
    unit: PantryUnit,
    minThreshold: Double,
    estimatedPricePaise: Long,
    isNeeded: Boolean,
    notes: String?
  ) {
    viewModelScope.launch {
      if (id == 0L) {
        val res = pantryRepository.addPantryItem(
          itemName = name,
          category = category,
          currentQuantity = quantity,
          unit = unit,
          minThreshold = minThreshold,
          estimatedPricePaise = estimatedPricePaise,
          isNeededOnShoppingList = isNeeded,
          notes = notes
        )
        if (res.isSuccess) {
          _userMessage.value = "Added $name to pantry"
        } else {
          _userMessage.value = res.exceptionOrNull()?.message ?: "Failed to add item"
        }
      } else {
        pantryRepository.updateStock(id, quantity)
        pantryRepository.toggleShoppingList(id, isNeeded)
        _userMessage.value = "Updated $name"
      }
    }
  }

  fun updateStock(id: Long, newQuantity: Double) {
    viewModelScope.launch {
      pantryRepository.updateStock(id, newQuantity)
    }
  }

  fun toggleShoppingList(id: Long, needed: Boolean) {
    viewModelScope.launch {
      pantryRepository.toggleShoppingList(id, needed)
    }
  }

  fun markPurchasedAndLogExpense(
    itemId: Long,
    purchasedQty: Double,
    actualCostPaise: Long,
    vendor: String?
  ) {
    viewModelScope.launch {
      val res = pantryRepository.markItemPurchased(
        itemId = itemId,
        purchasedQuantity = purchasedQty,
        actualCostPaise = actualCostPaise,
        vendor = vendor
      )
      if (res.isSuccess) {
        _userMessage.value = "Item marked purchased & added to Expenses!"
      } else {
        _userMessage.value = res.exceptionOrNull()?.message ?: "Failed to record purchase"
      }
    }
  }

  fun deleteItem(id: Long) {
    viewModelScope.launch {
      pantryRepository.deleteItem(id)
      _userMessage.value = "Item removed"
    }
  }

  class Factory(
    private val pantryRepository: PantryRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return PantryViewModel(pantryRepository) as T
    }
  }
}
