package com.example.ui.pantry

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PantryItem
import com.example.data.entity.PantryUnit
import com.example.data.model.ExpenseCategory
import com.example.util.RecipeCalculator
import com.example.util.RecipeDish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
  viewModel: PantryViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  var showAddEditDialog by remember { mutableStateOf<PantryItem?>(null) }
  var isCreatingNew by remember { mutableStateOf(false) }
  var purchaseItemDialog by remember { mutableStateOf<PantryItem?>(null) }
  var deleteConfirmItem by remember { mutableStateOf<PantryItem?>(null) }

  LaunchedEffect(uiState.userMessage) {
    uiState.userMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("pantry_screen"),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
      if (uiState.selectedTab != PantryTab.BULK_RECIPES) {
        FloatingActionButton(
          onClick = { isCreatingNew = true },
          modifier = Modifier.testTag("add_pantry_item_fab")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Pantry Item")
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      // Top Header & Stats Overview
      PantryHeader(
        lowStockCount = uiState.lowStockCount,
        shoppingCount = uiState.shoppingList.size,
        estimatedCostPaise = uiState.estimatedShoppingCostPaise
      )

      // Tab Navigation
      ScrollableTabRow(
        selectedTabIndex = uiState.selectedTab.ordinal,
        edgePadding = 16.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        PantryTab.values().forEach { tab ->
          Tab(
            selected = uiState.selectedTab == tab,
            onClick = { viewModel.setTab(tab) },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  when (tab) {
                    PantryTab.ALL_ITEMS -> Icons.Default.Inventory2
                    PantryTab.SHOPPING_LIST -> Icons.Default.ShoppingCart
                    PantryTab.BULK_RECIPES -> Icons.Default.Calculate
                  },
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(tab.title)
              }
            }
          )
        }
      }

      when (uiState.selectedTab) {
        PantryTab.ALL_ITEMS -> {
          // Search & Filter
          PantryFilterBar(
            searchQuery = uiState.searchQuery,
            onSearchChange = { viewModel.setSearchQuery(it) },
            selectedCategory = uiState.selectedCategoryFilter,
            onCategorySelect = { viewModel.setCategoryFilter(it) }
          )

          if (uiState.items.isEmpty()) {
            EmptyPantryState(
              title = if (uiState.searchQuery.isNotBlank()) "No items match search" else "No pantry items yet",
              subtitle = "Add ingredients and supplies to track stock levels"
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              items(uiState.items, key = { it.id }) { item ->
                PantryItemCard(
                  item = item,
                  onIncrement = { viewModel.updateStock(item.id, item.currentQuantity + 1.0) },
                  onDecrement = { viewModel.updateStock(item.id, (item.currentQuantity - 1.0).coerceAtLeast(0.0)) },
                  onToggleShopping = { viewModel.toggleShoppingList(item.id, !item.isNeededOnShoppingList) },
                  onMarkBought = { purchaseItemDialog = item },
                  onEdit = { showAddEditDialog = item },
                  onDelete = { deleteConfirmItem = item }
                )
              }
              item { Spacer(modifier = Modifier.height(70.dp)) }
            }
          }
        }

        PantryTab.SHOPPING_LIST -> {
          if (uiState.shoppingList.isEmpty()) {
            EmptyPantryState(
              title = "Shopping List is Clear! 🎉",
              subtitle = "All items have sufficient stock. Tap 'Add to List' on any pantry item when running low."
            )
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                Card(
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(
                        "${uiState.shoppingList.size} items to purchase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                      )
                      Text(
                        "Estimated budget: ₹${String.format(java.util.Locale.US, "%.2f", uiState.estimatedShoppingCostPaise / 100.0)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                      )
                    }
                    Icon(
                      Icons.Default.ShoppingCart,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(32.dp)
                    )
                  }
                }
              }

              items(uiState.shoppingList, key = { it.id }) { item ->
                ShoppingListItemCard(
                  item = item,
                  onMarkBought = { purchaseItemDialog = item },
                  onRemoveFromList = { viewModel.toggleShoppingList(item.id, false) }
                )
              }
              item { Spacer(modifier = Modifier.height(70.dp)) }
            }
          }
        }

        PantryTab.BULK_RECIPES -> {
          CooksRecipeCalculatorTab()
        }
      }
    }
  }

  // Dialogs
  if (isCreatingNew) {
    AddEditPantryItemDialog(
      item = null,
      onDismiss = { isCreatingNew = false },
      onSave = { name, cat, qty, unit, min, price, needed, notes ->
        viewModel.addOrUpdateItem(
          id = 0L,
          name = name,
          category = cat,
          quantity = qty,
          unit = unit,
          minThreshold = min,
          estimatedPricePaise = price,
          isNeeded = needed,
          notes = notes
        )
        isCreatingNew = false
      }
    )
  }

  showAddEditDialog?.let { itemToEdit ->
    AddEditPantryItemDialog(
      item = itemToEdit,
      onDismiss = { showAddEditDialog = null },
      onSave = { name, cat, qty, unit, min, price, needed, notes ->
        viewModel.addOrUpdateItem(
          id = itemToEdit.id,
          name = name,
          category = cat,
          quantity = qty,
          unit = unit,
          minThreshold = min,
          estimatedPricePaise = price,
          isNeeded = needed,
          notes = notes
        )
        showAddEditDialog = null
      }
    )
  }

  purchaseItemDialog?.let { itemToBuy ->
    MarkPurchasedDialog(
      item = itemToBuy,
      onDismiss = { purchaseItemDialog = null },
      onConfirm = { qty, costPaise, vendor ->
        viewModel.markPurchasedAndLogExpense(itemToBuy.id, qty, costPaise, vendor)
        purchaseItemDialog = null
      }
    )
  }

  deleteConfirmItem?.let { itemToDelete ->
    AlertDialog(
      onDismissRequest = { deleteConfirmItem = null },
      title = { Text("Delete Item") },
      text = { Text("Are you sure you want to delete '${itemToDelete.itemName}' from the pantry?") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteItem(itemToDelete.id)
            deleteConfirmItem = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteConfirmItem = null }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun PantryHeader(
  lowStockCount: Int,
  shoppingCount: Int,
  estimatedCostPaise: Long,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth().padding(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          "Pantry & Kitchen Stock",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          "Track rations, auto-generate shopping lists & calculate portions",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (lowStockCount > 0) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.errorContainer)
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                "$lowStockCount Low",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun PantryFilterBar(
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedCategory: ExpenseCategory?,
  onCategorySelect: (ExpenseCategory?) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      modifier = Modifier.fillMaxWidth(),
      placeholder = { Text("Search ingredients, pulses, milk, spices...") },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchChange("") }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          selected = selectedCategory == null,
          onClick = { onCategorySelect(null) },
          label = { Text("All") }
        )
      }
      items(ExpenseCategory.values()) { cat ->
        FilterChip(
          selected = selectedCategory == cat,
          onClick = { onCategorySelect(cat) },
          label = { Text(cat.displayName) }
        )
      }
    }
  }
}

@Composable
fun PantryItemCard(
  item: PantryItem,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  onToggleShopping: () -> Unit,
  onMarkBought: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isLowStock = item.currentQuantity <= item.minThreshold

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
      else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              item.itemName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            if (isLowStock) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                "⚠️ LOW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
              )
            }
          }
          Text(
            "${item.category.displayName} • Min threshold: ${item.minThreshold} ${item.unit.symbol}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Quantity Controls
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
          }

          Text(
            "${item.currentQuantity} ${item.unit.symbol}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
          )

          IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
          }
        }

        // Action Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          if (item.isNeededOnShoppingList || isLowStock) {
            Button(
              onClick = onMarkBought,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text("Mark Bought", fontSize = 12.sp)
            }
          } else {
            OutlinedButton(
              onClick = onToggleShopping,
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Add to List", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
fun ShoppingListItemCard(
  item: PantryItem,
  onMarkBought: () -> Unit,
  onRemoveFromList: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          item.itemName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          "Current: ${item.currentQuantity} ${item.unit.symbol} (Target: >${item.minThreshold} ${item.unit.symbol})",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (item.estimatedPricePaise > 0L) {
          Text(
            "Est: ₹${String.format(java.util.Locale.US, "%.2f", item.estimatedPricePaise / 100.0)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onRemoveFromList) {
          Icon(Icons.Default.Clear, contentDescription = "Remove from list")
        }
        Button(
          onClick = onMarkBought,
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Bought")
        }
      }
    }
  }
}

@Composable
fun CooksRecipeCalculatorTab(modifier: Modifier = Modifier) {
  var selectedDish by remember { mutableStateOf(RecipeCalculator.dishes.first()) }
  var headcount by remember { mutableIntStateOf(35) }

  val calculated = remember(selectedDish, headcount) {
    RecipeCalculator.calculateIngredients(selectedDish, headcount)
  }

  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
          Text(
            "🧑‍🍳 Cook's Bulk Quantity Calculator",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Text(
            "Accurately calculate raw ingredients required for any headcount to prevent food waste and shortages.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
          )
        }
      }
    }

    item {
      Text("Select Recipe / Dish", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
      ) {
        items(RecipeCalculator.dishes) { dish ->
          FilterChip(
            selected = selectedDish.id == dish.id,
            onClick = { selectedDish = dish },
            label = { Text(dish.name) }
          )
        }
      }
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
          Text("Headcount (People to serve)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              listOf(15, 25, 35, 50, 100).forEach { preset ->
                FilterChip(
                  selected = headcount == preset,
                  onClick = { headcount = preset },
                  label = { Text("$preset") }
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = { headcount = (headcount - 5).coerceAtLeast(1) },
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
              ) {
                Icon(Icons.Default.Remove, contentDescription = "-5")
              }
              Text(
                "$headcount",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
              )
              IconButton(
                onClick = { headcount += 5 },
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
              ) {
                Icon(Icons.Default.Add, contentDescription = "+5")
              }
            }
          }
        }
      }
    }

    item {
      Text(
        "Required Ingredients for ${selectedDish.name} ($headcount people):",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    }

    items(calculated) { (ingredient, amount) ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(ingredient.name, fontWeight = FontWeight.SemiBold)
            ingredient.notes?.let {
              Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Text(
            "$amount ${ingredient.unit}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
    item { Spacer(modifier = Modifier.height(70.dp)) }
  }
}

@Composable
fun EmptyPantryState(title: String, subtitle: String) {
  Box(
    modifier = Modifier.fillMaxSize().padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        Icons.Default.Inventory2,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPantryItemDialog(
  item: PantryItem?,
  onDismiss: () -> Unit,
  onSave: (name: String, cat: ExpenseCategory, qty: Double, unit: PantryUnit, min: Double, pricePaise: Long, needed: Boolean, notes: String?) -> Unit
) {
  var name by remember { mutableStateOf(item?.itemName ?: "") }
  var category by remember { mutableStateOf(item?.category ?: ExpenseCategory.GROCERIES) }
  var quantityStr by remember { mutableStateOf(item?.currentQuantity?.toString() ?: "5.0") }
  var unit by remember { mutableStateOf(item?.unit ?: PantryUnit.KG) }
  var minStr by remember { mutableStateOf(item?.minThreshold?.toString() ?: "2.0") }
  var priceStr by remember { mutableStateOf(item?.let { (it.estimatedPricePaise / 100.0).toString() } ?: "") }
  var isNeeded by remember { mutableStateOf(item?.isNeededOnShoppingList ?: false) }
  var notes by remember { mutableStateOf(item?.notes ?: "") }

  var expandedUnit by remember { mutableStateOf(false) }
  var expandedCat by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (item == null) "Add Pantry Item" else "Edit Pantry Item") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Item Name *") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
          expanded = expandedCat,
          onExpandedChange = { expandedCat = it }
        ) {
          OutlinedTextField(
            value = category.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = expandedCat,
            onDismissRequest = { expandedCat = false }
          ) {
            ExpenseCategory.values().forEach { cat ->
              DropdownMenuItem(
                text = { Text(cat.displayName) },
                onClick = {
                  category = cat
                  expandedCat = false
                }
              )
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = quantityStr,
            onValueChange = { quantityStr = it },
            label = { Text("Current Qty") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )

          ExposedDropdownMenuBox(
            expanded = expandedUnit,
            onExpandedChange = { expandedUnit = it },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = unit.symbol,
              onValueChange = {},
              readOnly = true,
              label = { Text("Unit") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
              modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = expandedUnit,
              onDismissRequest = { expandedUnit = false }
            ) {
              PantryUnit.values().forEach { u ->
                DropdownMenuItem(
                  text = { Text("${u.name} (${u.symbol})") },
                  onClick = {
                    unit = u
                    expandedUnit = false
                  }
                )
              }
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = minStr,
            onValueChange = { minStr = it },
            label = { Text("Low Stock Min") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = priceStr,
            onValueChange = { priceStr = it },
            label = { Text("Est. Price (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val qty = quantityStr.toDoubleOrNull() ?: 0.0
            val min = minStr.toDoubleOrNull() ?: 2.0
            val pricePaise = ((priceStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
            onSave(name, category, qty, unit, min, pricePaise, isNeeded, notes)
          }
        }
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun MarkPurchasedDialog(
  item: PantryItem,
  onDismiss: () -> Unit,
  onConfirm: (quantity: Double, costPaise: Long, vendor: String?) -> Unit
) {
  var quantityStr by remember { mutableStateOf("5.0") }
  var costRupeesStr by remember { mutableStateOf(if (item.estimatedPricePaise > 0L) (item.estimatedPricePaise / 100.0).toString() else "") }
  var vendor by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Mark '${item.itemName}' Purchased") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          "This will add to pantry inventory and automatically record an Expense in accounts.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = quantityStr,
          onValueChange = { quantityStr = it },
          label = { Text("Purchased Quantity (${item.unit.symbol}) *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = costRupeesStr,
          onValueChange = { costRupeesStr = it },
          label = { Text("Actual Total Cost (₹) *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = vendor,
          onValueChange = { vendor = it },
          label = { Text("Vendor / Shop Name (Optional)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val qty = quantityStr.toDoubleOrNull() ?: 0.0
          val costPaise = ((costRupeesStr.toDoubleOrNull() ?: 0.0) * 100).toLong()
          onConfirm(qty, costPaise, vendor)
        }
      ) {
        Text("Confirm & Record Expense")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
