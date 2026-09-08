package com.example.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Expense
import com.example.data.model.ExpenseCategory
import com.example.util.CurrencyUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
  viewModel: ExpenseViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(uiState.userMessage) {
    uiState.userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("expense_screen"),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Mess Expenses",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = uiState.formattedMonth + " • " + CurrencyUtils.formatPaise(uiState.monthlyTotalPaise),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          val currentMonth = ExpenseViewModel.getCurrentMonthString()
          if (uiState.selectedMonth != currentMonth) {
            IconButton(
              onClick = { viewModel.selectCurrentMonth() },
              modifier = Modifier.testTag("btn_this_month")
            ) {
              Icon(
                imageVector = Icons.Default.Today,
                contentDescription = "Current Month",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.openAddDialog() },
        modifier = Modifier.testTag("add_expense_fab"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Expense")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Month Navigator
      MonthNavigator(
        formattedMonth = uiState.formattedMonth,
        onPreviousMonth = { viewModel.selectPreviousMonth() },
        onNextMonth = { viewModel.selectNextMonth() }
      )

      // 2. Summary Card
      MonthlyExpenseSummaryCard(
        monthlyTotalPaise = uiState.monthlyTotalPaise,
        filteredTotalPaise = uiState.filteredTotalPaise,
        selectedCategory = uiState.selectedCategory,
        expenseCount = uiState.expenses.size,
        formattedMonth = uiState.formattedMonth
      )

      // 3. Category Filter Chips
      CategoryFilterRow(
        selectedCategory = uiState.selectedCategory,
        onSelectCategory = { viewModel.setCategory(it) }
      )

      // 4. Expenses List or Empty State
      if (uiState.isLoading && uiState.expenses.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      } else if (uiState.expenses.isEmpty()) {
        EmptyExpenseState(
          formattedMonth = uiState.formattedMonth,
          category = uiState.selectedCategory,
          onAddExpense = { viewModel.openAddDialog() }
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .testTag("expense_list"),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(uiState.expenses, key = { it.id }) { expense ->
            ExpenseItemCard(
              expense = expense,
              onEdit = { viewModel.openEditDialog(expense) },
              onDelete = { viewModel.requestDelete(expense) }
            )
          }
        }
      }
    }
  }

  // Add / Edit Dialog
  if (uiState.showAddEditDialog) {
    AddEditExpenseDialog(
      editingExpense = uiState.editingExpense,
      defaultDate = if (uiState.selectedMonth == ExpenseViewModel.getCurrentMonthString()) {
        ExpenseViewModel.getTodayString()
      } else {
        "${uiState.selectedMonth}-01"
      },
      onDismiss = { viewModel.dismissAddEditDialog() },
      onSave = { id, date, desc, cat, amt, qty, unit, vendor, notes ->
        viewModel.saveExpense(
          id = id,
          date = date,
          description = desc,
          category = cat,
          amountRupeesStr = amt,
          quantityStr = qty,
          unit = unit,
          vendor = vendor,
          notes = notes
        )
      }
    )
  }

  // Delete Confirmation Dialog
  uiState.deleteConfirmExpense?.let { expenseToDelete ->
    AlertDialog(
      onDismissRequest = { viewModel.dismissDeleteDialog() },
      title = { Text("Delete Expense?") },
      text = {
        Text(
          "Are you sure you want to delete \"${expenseToDelete.description}\" (${CurrencyUtils.formatPaise(expenseToDelete.amountPaise)}) from ${expenseToDelete.date}?\n\nThis will update your monthly expense totals."
        )
      },
      confirmButton = {
        Button(
          onClick = { viewModel.confirmDelete() },
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          modifier = Modifier.testTag("btn_confirm_delete_expense")
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(
          onClick = { viewModel.dismissDeleteDialog() },
          modifier = Modifier.testTag("btn_cancel_delete_expense")
        ) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun MonthNavigator(
  formattedMonth: String,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      IconButton(
        onClick = onPreviousMonth,
        modifier = Modifier.testTag("btn_prev_month")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Previous Month"
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CalendarMonth,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = formattedMonth,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
      }

      IconButton(
        onClick = onNextMonth,
        modifier = Modifier.testTag("btn_next_month")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Next Month"
        )
      }
    }
  }
}

@Composable
private fun MonthlyExpenseSummaryCard(
  monthlyTotalPaise: Long,
  filteredTotalPaise: Long,
  selectedCategory: ExpenseCategory?,
  expenseCount: Int,
  formattedMonth: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("expense_summary_card"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    shape = RoundedCornerShape(16.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = if (selectedCategory == null) "Total Expenses ($formattedMonth)" else "${selectedCategory.displayName} Expenses",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (selectedCategory == null) {
            CurrencyUtils.formatPaise(monthlyTotalPaise)
          } else {
            CurrencyUtils.formatPaise(filteredTotalPaise)
          },
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        if (selectedCategory != null) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Month Total: ${CurrencyUtils.formatPaise(monthlyTotalPaise)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
          )
        }
      }

      Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        modifier = Modifier.size(44.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = expenseCount.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary
          )
        }
      }
    }
  }
}

@Composable
private fun CategoryFilterRow(
  selectedCategory: ExpenseCategory?,
  onSelectCategory: (ExpenseCategory?) -> Unit
) {
  LazyRow(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    item {
      FilterChip(
        selected = selectedCategory == null,
        onClick = { onSelectCategory(null) },
        label = { Text("All") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        },
        modifier = Modifier.testTag("filter_all")
      )
    }

    items(ExpenseCategory.values()) { category ->
      FilterChip(
        selected = selectedCategory == category,
        onClick = { onSelectCategory(category) },
        label = { Text(category.displayName) },
        leadingIcon = {
          Icon(
            imageVector = getCategoryIcon(category),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        },
        modifier = Modifier.testTag("filter_${category.name.lowercase()}")
      )
    }
  }
}

@Composable
private fun ExpenseItemCard(
  expense: Expense,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("expense_card_${expense.id}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Row 1: Category badge + Date + Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = getCategoryIcon(expense.category),
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = expense.category.displayName,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }

        Text(
          text = formatDisplayDate(expense.date),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row {
          IconButton(
            onClick = onEdit,
            modifier = Modifier
              .size(32.dp)
              .testTag("btn_edit_expense_${expense.id}")
          ) {
            Icon(
              Icons.Default.Edit,
              contentDescription = "Edit",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier
              .size(32.dp)
              .testTag("btn_delete_expense_${expense.id}")
          ) {
            Icon(
              Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Row 2: Description & Amount
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = expense.description,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = CurrencyUtils.formatPaise(expense.amountPaise),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      // Row 3: Optional Vendor, Quantity, Notes
      val hasExtra = !expense.vendor.isNullOrBlank() || expense.quantity != null || !expense.notes.isNullOrBlank()
      if (hasExtra) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          if (!expense.vendor.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = expense.vendor,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (expense.quantity != null) {
            val qtyText = if (expense.unit.isNullOrBlank()) "${expense.quantity}" else "${expense.quantity} ${expense.unit}"
            Text(
              text = "Qty: $qtyText",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        if (!expense.notes.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Note: ${expense.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyExpenseState(
  formattedMonth: String,
  category: ExpenseCategory?,
  onAddExpense: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp)
      .testTag("expense_empty_state"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = CircleShape,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "No Expenses Found",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(6.dp))

      val emptyDesc = if (category != null) {
        "No ${category.displayName} expenses recorded for $formattedMonth."
      } else {
        "No mess expenses recorded for $formattedMonth."
      }

      Text(
        text = emptyDesc,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      Button(
        onClick = onAddExpense,
        modifier = Modifier.testTag("btn_empty_add_expense")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Add Expense")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditExpenseDialog(
  editingExpense: Expense?,
  defaultDate: String,
  onDismiss: () -> Unit,
  onSave: (
    id: Long,
    date: String,
    desc: String,
    category: ExpenseCategory,
    amountStr: String,
    quantityStr: String?,
    unit: String?,
    vendor: String?,
    notes: String?
  ) -> Unit
) {
  var date by remember { mutableStateOf(editingExpense?.date ?: defaultDate) }
  var description by remember { mutableStateOf(editingExpense?.description ?: "") }
  var category by remember { mutableStateOf(editingExpense?.category ?: ExpenseCategory.GROCERIES) }
  var amountStr by remember {
    mutableStateOf(
      if (editingExpense != null) {
        CurrencyUtils.paiseToRupees(editingExpense.amountPaise).stripTrailingZeros().toPlainString()
      } else {
        ""
      }
    )
  }
  var quantityStr by remember { mutableStateOf(editingExpense?.quantity?.toString() ?: "") }
  var unit by remember { mutableStateOf(editingExpense?.unit ?: "") }
  var vendor by remember { mutableStateOf(editingExpense?.vendor ?: "") }
  var notes by remember { mutableStateOf(editingExpense?.notes ?: "") }

  var showDatePicker by remember { mutableStateOf(false) }
  var categoryExpanded by remember { mutableStateOf(false) }

  var descError by remember { mutableStateOf<String?>(null) }
  var amountError by remember { mutableStateOf<String?>(null) }
  var dateError by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (editingExpense == null) "Add New Expense" else "Edit Expense",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Date Selector Field
        OutlinedTextField(
          value = date,
          onValueChange = {
            date = it
            dateError = null
          },
          label = { Text("Date (YYYY-MM-DD)*") },
          isError = dateError != null,
          supportingText = dateError?.let { { Text(it) } },
          trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
              Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_expense_date")
        )

        // Description Field
        OutlinedTextField(
          value = description,
          onValueChange = {
            description = it
            descError = null
          },
          label = { Text("Description/Item*") },
          placeholder = { Text("e.g. Vegetables, Milk, Gas cylinder") },
          isError = descError != null,
          supportingText = descError?.let { { Text(it) } },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_expense_desc")
        )

        // Category Dropdown
        ExposedDropdownMenuBox(
          expanded = categoryExpanded,
          onExpandedChange = { categoryExpanded = !categoryExpanded }
        ) {
          OutlinedTextField(
            value = category.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category*") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
            modifier = Modifier
              .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
              .fillMaxWidth()
              .testTag("input_expense_category")
          )
          ExposedDropdownMenu(
            expanded = categoryExpanded,
            onDismissRequest = { categoryExpanded = false }
          ) {
            ExpenseCategory.values().forEach { cat ->
              DropdownMenuItem(
                text = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(getCategoryIcon(cat), contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(cat.displayName)
                  }
                },
                onClick = {
                  category = cat
                  categoryExpanded = false
                },
                modifier = Modifier.testTag("category_option_${cat.name.lowercase()}")
              )
            }
          }
        }

        // Amount Field in Rupees
        OutlinedTextField(
          value = amountStr,
          onValueChange = {
            amountStr = it
            amountError = null
          },
          label = { Text("Amount in Rupees (₹)*") },
          prefix = { Text("₹ ") },
          placeholder = { Text("0.00") },
          isError = amountError != null,
          supportingText = amountError?.let { { Text(it) } },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_expense_amount")
        )

        // Quantity and Unit Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = quantityStr,
            onValueChange = { quantityStr = it },
            label = { Text("Quantity (optional)") },
            placeholder = { Text("e.g. 5") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("input_expense_qty")
          )

          OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            label = { Text("Unit (optional)") },
            placeholder = { Text("kg, litres") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("input_expense_unit")
          )
        }

        // Vendor Field
        OutlinedTextField(
          value = vendor,
          onValueChange = { vendor = it },
          label = { Text("Vendor / Supplier (optional)") },
          placeholder = { Text("e.g. Local Mandi, Amul Store") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_expense_vendor")
        )

        // Notes Field
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (optional)") },
          placeholder = { Text("Additional details...") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_expense_notes")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          var hasError = false

          if (date.isBlank()) {
            dateError = "Date is required"
            hasError = true
          }

          if (description.isBlank()) {
            descError = "Description cannot be blank"
            hasError = true
          }

          val paise = ExpenseViewModel.parseRupeesToPaise(amountStr)
          if (paise == null) {
            amountError = "Amount must be greater than ₹0"
            hasError = true
          }

          if (!hasError) {
            onSave(
              editingExpense?.id ?: 0L,
              date,
              description,
              category,
              amountStr,
              quantityStr.ifBlank { null },
              unit.ifBlank { null },
              vendor.ifBlank { null },
              notes.ifBlank { null }
            )
          }
        },
        modifier = Modifier.testTag("btn_save_expense")
      ) {
        Text("Save Expense")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("btn_cancel_expense")
      ) {
        Text("Cancel")
      }
    }
  )

  // Date picker dialog
  if (showDatePicker) {
    ExpenseDatePickerDialog(
      initialDateString = date,
      onDateSelected = {
        date = it
        dateError = null
        showDatePicker = false
      },
      onDismiss = { showDatePicker = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseDatePickerDialog(
  initialDateString: String,
  onDateSelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val initialMillis = remember(initialDateString) {
    try {
      val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(initialDateString)
      parsed?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
      System.currentTimeMillis()
    }
  }

  val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = initialMillis
  )

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          val selected = datePickerState.selectedDateMillis
          if (selected != null) {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
              timeInMillis = selected
            }
            val yyyy = cal.get(Calendar.YEAR)
            val mm = String.format(Locale.US, "%02d", cal.get(Calendar.MONTH) + 1)
            val dd = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
            onDateSelected("$yyyy-$mm-$dd")
          } else {
            onDismiss()
          }
        },
        modifier = Modifier.testTag("confirm_expense_date_button")
      ) {
        Text("OK")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  ) {
    DatePicker(state = datePickerState)
  }
}

private fun getCategoryIcon(category: ExpenseCategory): ImageVector {
  return when (category) {
    ExpenseCategory.GROCERIES -> Icons.Default.ShoppingBag
    ExpenseCategory.VEGETABLES -> Icons.Default.Restaurant
    ExpenseCategory.DAIRY -> Icons.Default.Store
    ExpenseCategory.GAS_FUEL -> Icons.Default.LocalGasStation
    ExpenseCategory.SALARY_LABOUR -> Icons.Default.Work
    ExpenseCategory.UTILITIES -> Icons.Default.Payments
    ExpenseCategory.OTHER -> Icons.Default.Category
  }
}

private fun formatDisplayDate(dateStr: String): String {
  return try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
    if (parsed != null) {
      SimpleDateFormat("dd MMM yyyy", Locale.US).format(parsed)
    } else {
      dateStr
    }
  } catch (_: Exception) {
    dateStr
  }
}
