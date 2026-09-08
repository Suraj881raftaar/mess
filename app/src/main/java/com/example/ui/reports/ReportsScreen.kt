package com.example.ui.reports

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BreakfastAccent
import com.example.ui.theme.BreakfastContainerLight
import com.example.ui.theme.DinnerAccent
import com.example.ui.theme.DinnerContainerLight
import com.example.ui.theme.LunchAccent
import com.example.ui.theme.LunchContainerLight
import com.example.util.CurrencyUtils
import com.example.util.ReportExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
  viewModel: ReportsViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  var menuExpanded by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("reports_screen"),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Mess Reports & Bills",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = uiState.formattedMonth + " • " + CurrencyUtils.formatPaise(uiState.totalExpensesPaise),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          val currentMonth = ReportsViewModel.getCurrentMonthString()
          if (uiState.selectedMonth != currentMonth) {
            IconButton(
              onClick = { viewModel.resetToCurrentMonth() },
              modifier = Modifier.testTag("btn_reset_current_month")
            ) {
              Icon(
                imageVector = Icons.Default.Today,
                contentDescription = "This Month",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          Box {
            IconButton(
              onClick = { menuExpanded = true },
              modifier = Modifier.testTag("btn_export_menu")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Export & Share Report",
                tint = MaterialTheme.colorScheme.primary
              )
            }

            DropdownMenu(
              expanded = menuExpanded,
              onDismissRequest = { menuExpanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("Export CSV (Excel)") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null
                  )
                },
                onClick = {
                  menuExpanded = false
                  ReportExportUtils.shareReportCsv(context, uiState)
                },
                modifier = Modifier.testTag("menu_item_export_csv")
              )
              DropdownMenuItem(
                text = { Text("Share Text Summary") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null
                  )
                },
                onClick = {
                  menuExpanded = false
                  ReportExportUtils.shareReportText(context, uiState)
                },
                modifier = Modifier.testTag("menu_item_share_summary")
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Top Bar: Month Selection Controls
      MonthSelectionHeader(
        formattedMonth = uiState.formattedMonth,
        selectedMonth = uiState.selectedMonth,
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() },
        onResetCurrentMonth = { viewModel.resetToCurrentMonth() }
      )

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // High-level Financial Summary Overview Card
        item {
          MonthlyOverviewSummaryCard(
            totalExpensesPaise = uiState.totalExpensesPaise,
            totalMeals = uiState.totalMeals,
            costPerMealRupees = uiState.costPerMealRupees,
            activeEmployees = uiState.activeEmployeeCount,
            onExportCsv = { ReportExportUtils.shareReportCsv(context, uiState) },
            onShareSummary = { ReportExportUtils.shareReportText(context, uiState) }
          )
        }

        // Meal Breakdown Card (Breakfast, Lunch, Dinner)
        item {
          MealBreakdownSummaryCard(
            breakfastCount = uiState.breakfastCount,
            lunchCount = uiState.lunchCount,
            dinnerCount = uiState.dinnerCount,
            totalMeals = uiState.totalMeals
          )
        }

        // Section Header: Employee-Wise Billing
        item {
          EmployeeBillingHeader(
            totalEmployees = uiState.employeeBills.size,
            searchQuery = uiState.searchQuery,
            onSearchChange = { viewModel.setSearchQuery(it) },
            showOnlyWithMeals = uiState.showOnlyWithMeals,
            onToggleOnlyWithMeals = { viewModel.setShowOnlyWithMeals(it) }
          )
        }

        // Employee Bills List
        if (uiState.filteredEmployeeBills.isEmpty()) {
          item {
            EmptyReportState(
              searchQuery = uiState.searchQuery,
              showOnlyWithMeals = uiState.showOnlyWithMeals
            )
          }
        } else {
          items(
            items = uiState.filteredEmployeeBills,
            key = { it.employee.id }
          ) { item ->
            EmployeeBillCard(
              item = item,
              costPerMealRupees = uiState.costPerMealRupees,
              onClick = { viewModel.selectEmployeeForDetail(item) }
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }

  // Employee Bill Detail Dialog
  uiState.selectedEmployeeForDetail?.let { detail ->
    EmployeeBillDetailDialog(
      item = detail,
      formattedMonth = uiState.formattedMonth,
      costPerMealRupees = uiState.costPerMealRupees,
      onDismiss = { viewModel.selectEmployeeForDetail(null) }
    )
  }
}

@Composable
private fun MonthSelectionHeader(
  formattedMonth: String,
  selectedMonth: String,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onResetCurrentMonth: () -> Unit
) {
  val isCurrentMonth = selectedMonth == ReportsViewModel.getCurrentMonthString()

  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
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
          text = formattedMonth.ifBlank { selectedMonth },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("report_selected_month")
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
private fun MonthlyOverviewSummaryCard(
  totalExpensesPaise: Long,
  totalMeals: Int,
  costPerMealRupees: Double,
  activeEmployees: Int,
  onExportCsv: () -> Unit,
  onShareSummary: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Monthly Financial Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
          Text(
            text = "$activeEmployees Active Staff",
            modifier = Modifier
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("report_active_staff"),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Financial Metrics Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Expenses
        ElevatedCard(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Total Expenses",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalExpensesPaise),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.testTag("report_total_expenses")
            )
          }
        }

        // Total Meals
        ElevatedCard(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Total Meals",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$totalMeals",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.testTag("report_total_meals")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Highlight Cost Per Meal Box
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Shared Cost per Meal",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (totalMeals > 0) {
                "${CurrencyUtils.formatPaise(totalExpensesPaise)} ÷ $totalMeals meals"
              } else {
                "No meals served this month (₹0.00)"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Text(
            text = if (totalMeals > 0) CurrencyUtils.formatRupees(costPerMealRupees) else "₹0.00",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("report_cost_per_meal")
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Export Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = onExportCsv,
          modifier = Modifier
            .weight(1f)
            .testTag("btn_export_csv_card"),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Export CSV", style = MaterialTheme.typography.labelMedium)
        }

        OutlinedButton(
          onClick = onShareSummary,
          modifier = Modifier
            .weight(1f)
            .testTag("btn_share_summary_card"),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Share Report", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}

@Composable
private fun MealBreakdownSummaryCard(
  breakfastCount: Int,
  lunchCount: Int,
  dinnerCount: Int,
  totalMeals: Int
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Restaurant,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Monthly Meal Breakdown",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MealCountTile(
          title = "Breakfast",
          count = breakfastCount,
          percentage = if (totalMeals > 0) (breakfastCount * 100 / totalMeals) else 0,
          icon = Icons.Default.FreeBreakfast,
          accentColor = BreakfastAccent,
          containerColor = BreakfastContainerLight,
          testTag = "report_breakfast_count",
          modifier = Modifier.weight(1f)
        )
        MealCountTile(
          title = "Lunch",
          count = lunchCount,
          percentage = if (totalMeals > 0) (lunchCount * 100 / totalMeals) else 0,
          icon = Icons.Default.LunchDining,
          accentColor = LunchAccent,
          containerColor = LunchContainerLight,
          testTag = "report_lunch_count",
          modifier = Modifier.weight(1f)
        )
        MealCountTile(
          title = "Dinner",
          count = dinnerCount,
          percentage = if (totalMeals > 0) (dinnerCount * 100 / totalMeals) else 0,
          icon = Icons.Default.DinnerDining,
          accentColor = DinnerAccent,
          containerColor = DinnerContainerLight,
          testTag = "report_dinner_count",
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun MealCountTile(
  title: String,
  count: Int,
  percentage: Int,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  containerColor: Color,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = containerColor.copy(alpha = 0.5f)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag(testTag)
      )
      Text(
        text = "$title ($percentage%)",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun EmployeeBillingHeader(
  totalEmployees: Int,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  showOnlyWithMeals: Boolean,
  onToggleOnlyWithMeals: (Boolean) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Employee Billing",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "$totalEmployees Total",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Search Box
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("report_employee_search"),
      placeholder = { Text("Search by name, ID or department...") },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(onClick = { onSearchChange("") }) {
            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Filter Chips
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = !showOnlyWithMeals,
        onClick = { onToggleOnlyWithMeals(false) },
        label = { Text("All Staff") },
        colors = FilterChipDefaults.filterChipColors(),
        modifier = Modifier.testTag("chip_all_staff")
      )

      FilterChip(
        selected = showOnlyWithMeals,
        onClick = { onToggleOnlyWithMeals(true) },
        label = { Text("With Meals Only") },
        colors = FilterChipDefaults.filterChipColors(),
        modifier = Modifier.testTag("chip_with_meals_only")
      )
    }
  }
}

@Composable
private fun EmployeeBillCard(
  item: EmployeeReportItem,
  costPerMealRupees: Double,
  onClick: () -> Unit
) {
  val employee = item.employee
  val hasMeals = item.totalMeals > 0

  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("employee_bill_card_${employee.employeeCode}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (hasMeals) {
        MaterialTheme.colorScheme.surface
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      }
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar with initials
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(
            if (hasMeals) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
          ),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = employee.name.take(2).uppercase(),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = if (hasMeals) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Employee Info & Meal Breakdown
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = employee.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          if (!employee.isActive) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.errorContainer
            ) {
              Text(
                text = "Inactive",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = "${employee.employeeCode} • ${employee.department}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Meals count breakdown
        Text(
          text = if (hasMeals) {
            "${item.totalMeals} meals (B: ${item.breakfastCount} • L: ${item.lunchCount} • D: ${item.dinnerCount})"
          } else {
            "0 meals taken this month"
          },
          style = MaterialTheme.typography.labelSmall,
          color = if (hasMeals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          fontWeight = if (hasMeals) FontWeight.Medium else FontWeight.Normal,
          modifier = Modifier.testTag("employee_meals_${employee.employeeCode}")
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Bill Amount
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = CurrencyUtils.formatPaise(item.payablePaise),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (item.payablePaise > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.testTag("employee_bill_${employee.employeeCode}")
        )
        Text(
          text = "Payable",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun EmployeeBillDetailDialog(
  item: EmployeeReportItem,
  formattedMonth: String,
  costPerMealRupees: Double,
  onDismiss: () -> Unit
) {
  val employee = item.employee
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Close")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          ReportExportUtils.shareEmployeeBillText(
            context = context,
            item = item,
            formattedMonth = formattedMonth,
            costPerMealRupees = costPerMealRupees
          )
        }
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("Share Slip")
      }
    },
    title = {
      Column {
        Text(
          text = employee.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${employee.employeeCode} • ${employee.department} ($formattedMonth)",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
      ) {
        // Shared meal cost note
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Cost per Meal Rate",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = CurrencyUtils.formatRupees(costPerMealRupees),
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Meal Itemizations
        DetailMealRow(label = "Breakfast Meals", count = item.breakfastCount)
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        DetailMealRow(label = "Lunch Meals", count = item.lunchCount)
        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        DetailMealRow(label = "Dinner Meals", count = item.dinnerCount)

        HorizontalDivider(
          modifier = Modifier.padding(vertical = 8.dp),
          thickness = 1.5.dp,
          color = MaterialTheme.colorScheme.outline
        )

        // Total Meals
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Total Meals",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${item.totalMeals}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Total Payable Card
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Total Payable Bill",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "${item.totalMeals} × ${CurrencyUtils.formatRupees(costPerMealRupees)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }

            Text(
              text = CurrencyUtils.formatPaise(item.payablePaise),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    }
  )
}

@Composable
private fun DetailMealRow(
  label: String,
  count: Int
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = "$count",
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
private fun EmptyReportState(
  searchQuery: String,
  showOnlyWithMeals: Boolean
) {
  OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.FilterList,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = if (searchQuery.isNotBlank()) {
          "No employees match \"$searchQuery\""
        } else if (showOnlyWithMeals) {
          "No employees took meals in this month"
        } else {
          "No employee records found"
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Try adjusting your search query or switching months.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
    }
  }
}
