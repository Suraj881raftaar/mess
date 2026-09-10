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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
  var upiTargetEmployee by remember { mutableStateOf<EmployeeReportItem?>(null) }
  var depositTargetEmployee by remember { mutableStateOf<EmployeeReportItem?>(null) }
  var passbookTargetEmployee by remember { mutableStateOf<EmployeeReportItem?>(null) }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("reports_screen"),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Mess Billing & Collections",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${uiState.formattedMonth} • Collected ${CurrencyUtils.formatPaise(uiState.totalCollectedPaise)}",
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
                  Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
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
                  Icon(imageVector = Icons.Default.Share, contentDescription = null)
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Month Selector Header
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
        // 1. Hero Financial Ledger (Total Collection vs Total Expense)
        item {
          FinancialLedgerHeroCard(
            totalCollectionPaise = uiState.totalCollectedPaise,
            totalExpensePaise = uiState.totalExpensesPaise,
            totalPendingPaise = uiState.totalPendingPaise,
            collectionPercentage = uiState.collectionPercentage,
            costPerMealRupees = uiState.costPerMealRupees,
            totalMeals = uiState.totalMeals,
            activeStaffCount = uiState.activeEmployeeCount,
            onExportCsv = { ReportExportUtils.shareReportCsv(context, uiState) },
            onShareSummary = { ReportExportUtils.shareReportText(context, uiState) }
          )
        }

        // 2. Search & Payment Filter Chips
        item {
          BillingFilterSection(
            searchQuery = uiState.searchQuery,
            onSearchChange = { viewModel.setSearchQuery(it) },
            selectedPaymentFilter = uiState.paymentFilter,
            onPaymentFilterChange = { viewModel.setPaymentFilter(it) },
            showOnlyWithMeals = uiState.showOnlyWithMeals,
            onToggleOnlyWithMeals = { viewModel.setShowOnlyWithMeals(it) },
            dueCount = uiState.employeeBills.count { it.pendingPaise > 0 },
            settledCount = uiState.employeeBills.count { it.paymentStatus == PaymentStatus.PAID || (it.paidPaise >= it.payablePaise && it.payablePaise > 0) },
            totalCount = uiState.employeeBills.size
          )
        }

        // 3. Employee Bills & Deposits List
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
              onClick = { viewModel.selectEmployeeForDetail(item) },
              onOpenDeposit = { depositTargetEmployee = item },
              onOpenPassbook = { passbookTargetEmployee = item },
              onOpenUpi = { upiTargetEmployee = item }
            )
          }
        }

        item {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }

  // Record Deposit Dialog
  depositTargetEmployee?.let { item ->
    RecordPaymentDialog(
      employee = item.employee,
      month = uiState.selectedMonth,
      payablePaise = item.payablePaise,
      alreadyPaidPaise = item.paidPaise,
      pendingPaise = item.pendingPaise,
      onDismiss = { depositTargetEmployee = null },
      onSavePayment = { amountPaise, date, method, notes ->
        viewModel.recordPayment(
          employeeId = item.employee.id,
          month = uiState.selectedMonth,
          amountPaise = amountPaise,
          paymentDate = date,
          paymentMethod = method,
          notes = notes
        ) {
          depositTargetEmployee = null
        }
      }
    )
  }

  // Passbook Dialog
  passbookTargetEmployee?.let { item ->
    EmployeePassbookDialog(
      item = item,
      formattedMonth = uiState.formattedMonth,
      onDismiss = { passbookTargetEmployee = null },
      onDeletePayment = { payment ->
        viewModel.deletePayment(payment)
      },
      onOpenDepositDialog = {
        val target = passbookTargetEmployee
        passbookTargetEmployee = null
        depositTargetEmployee = target
      }
    )
  }

  // Employee Bill Detail Dialog
  uiState.selectedEmployeeForDetail?.let { detail ->
    EmployeeBillDetailDialog(
      item = detail,
      formattedMonth = uiState.formattedMonth,
      costPerMealRupees = uiState.costPerMealRupees,
      onOpenDeposit = {
        viewModel.selectEmployeeForDetail(null)
        depositTargetEmployee = detail
      },
      onOpenPassbook = {
        viewModel.selectEmployeeForDetail(null)
        passbookTargetEmployee = detail
      },
      onOpenUpi = {
        viewModel.selectEmployeeForDetail(null)
        upiTargetEmployee = detail
      },
      onDismiss = { viewModel.selectEmployeeForDetail(null) }
    )
  }

  // UPI QR Dialog
  upiTargetEmployee?.let { detail ->
    val upiAmount = if (detail.pendingPaise > 0) detail.pendingPaise / 100.0 else detail.payablePaise / 100.0
    UpiPaymentDialog(
      employeeName = detail.employee.name,
      month = uiState.formattedMonth,
      amountRupees = upiAmount,
      upiId = "officemess@upi",
      payeeName = "Office Mess Account",
      onDismiss = { upiTargetEmployee = null },
      onPaymentRecorded = {
        upiTargetEmployee = null
      }
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
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { if (!isCurrentMonth) onResetCurrentMonth() }
      ) {
        Text(
          text = formattedMonth,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("report_selected_month")
        )
        if (isCurrentMonth) {
          Spacer(modifier = Modifier.width(6.dp))
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer
          ) {
            Text(
              text = "Current",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
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
private fun FinancialLedgerHeroCard(
  totalCollectionPaise: Long,
  totalExpensePaise: Long,
  totalPendingPaise: Long,
  collectionPercentage: Float,
  costPerMealRupees: Double,
  totalMeals: Int,
  activeStaffCount: Int,
  onExportCsv: () -> Unit,
  onShareSummary: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            imageVector = Icons.Default.Payments,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Financial Ledger",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ) {
          Text(
            text = "$activeStaffCount Staff",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("report_active_staff")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3-Column Financial Summary
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Total Collection
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFE8F5E9)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Total Collection",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF1B5E20),
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalCollectionPaise),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B5E20),
              modifier = Modifier.testTag("report_total_collection")
            )
          }
        }

        // Total Expense
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFF3E0)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Total Expense",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFBF360C),
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalExpensePaise),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFBF360C),
              modifier = Modifier.testTag("report_total_expenses")
            )
          }
        }

        // Left to Pay
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = if (totalPendingPaise > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text(
              text = "Left to Collect",
              style = MaterialTheme.typography.labelSmall,
              color = if (totalPendingPaise > 0) Color(0xFFC62828) else Color(0xFF1B5E20),
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalPendingPaise),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (totalPendingPaise > 0) Color(0xFFC62828) else Color(0xFF1B5E20),
              modifier = Modifier.testTag("report_total_pending")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Linear Progress Bar for Collection
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Collection Rate",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = String.format(java.util.Locale.US, "%.0f%%", collectionPercentage),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { (collectionPercentage / 100f).coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = Color(0xFF2E7D32),
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
      Spacer(modifier = Modifier.height(10.dp))

      // Shared Cost Rate & Meals count
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Cost per Meal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = CurrencyUtils.formatRupees(costPerMealRupees),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.testTag("report_cost_per_meal")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "($totalMeals meals)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.testTag("report_total_meals")
            )
          }
        }

        // Export Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onExportCsv,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("btn_export_csv_card")
          ) {
            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("CSV", style = MaterialTheme.typography.labelSmall)
          }

          OutlinedButton(
            onClick = onShareSummary,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("btn_share_summary_card")
          ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Share", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Composable
private fun BillingFilterSection(
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedPaymentFilter: PaymentFilterTab,
  onPaymentFilterChange: (PaymentFilterTab) -> Unit,
  showOnlyWithMeals: Boolean,
  onToggleOnlyWithMeals: (Boolean) -> Unit,
  dueCount: Int,
  settledCount: Int,
  totalCount: Int
) {
  Column(modifier = Modifier.fillMaxWidth()) {
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
      shape = RoundedCornerShape(14.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedPaymentFilter == PaymentFilterTab.ALL,
        onClick = { onPaymentFilterChange(PaymentFilterTab.ALL) },
        label = { Text("All ($totalCount)") },
        modifier = Modifier.testTag("chip_all_staff")
      )

      FilterChip(
        selected = selectedPaymentFilter == PaymentFilterTab.HAS_DUES,
        onClick = { onPaymentFilterChange(PaymentFilterTab.HAS_DUES) },
        label = { Text("With Dues ($dueCount)") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
          selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.testTag("chip_with_dues")
      )

      FilterChip(
        selected = selectedPaymentFilter == PaymentFilterTab.SETTLED,
        onClick = { onPaymentFilterChange(PaymentFilterTab.SETTLED) },
        label = { Text("Settled ($settledCount)") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = Color(0xFFC8E6C9),
          selectedLabelColor = Color(0xFF1B5E20)
        ),
        modifier = Modifier.testTag("chip_settled")
      )

      FilterChip(
        selected = showOnlyWithMeals,
        onClick = { onToggleOnlyWithMeals(!showOnlyWithMeals) },
        label = { Text("Meals > 0") },
        modifier = Modifier.testTag("chip_with_meals_only")
      )
    }
  }
}

@Composable
private fun EmployeeBillCard(
  item: EmployeeReportItem,
  costPerMealRupees: Double,
  onClick: () -> Unit,
  onOpenDeposit: () -> Unit,
  onOpenPassbook: () -> Unit,
  onOpenUpi: () -> Unit
) {
  val employee = item.employee
  val hasMeals = item.totalMeals > 0

  Card(
    onClick = onClick,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("employee_bill_card_${employee.employeeCode}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header: Avatar, Name, Code, and Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
              if (item.pendingPaise > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
              else if (hasMeals) Color(0xFFC8E6C9)
              else MaterialTheme.colorScheme.surfaceVariant
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = employee.name.take(2).uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = if (item.pendingPaise > 0) MaterialTheme.colorScheme.onErrorContainer
            else if (hasMeals) Color(0xFF1B5E20)
            else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

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
              Spacer(modifier = Modifier.width(4.dp))
              Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Text(
                  text = "Inactive",
                  style = MaterialTheme.typography.labelSmall,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }
          }
          Text(
            text = "${employee.employeeCode} • ${employee.department}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Status Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when {
            !hasMeals -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            item.pendingPaise == 0L && item.paidPaise > 0L -> Color(0xFFE8F5E9)
            item.pendingPaise > 0L -> Color(0xFFFFEBEE)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          }
        ) {
          Text(
            text = when {
              !hasMeals -> "No Meals"
              item.pendingPaise == 0L && item.paidPaise > 0L -> "✓ Settled"
              item.pendingPaise > 0L -> "${CurrencyUtils.formatPaise(item.pendingPaise)} Due"
              else -> "No Dues"
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when {
              !hasMeals -> MaterialTheme.colorScheme.onSurfaceVariant
              item.pendingPaise == 0L && item.paidPaise > 0L -> Color(0xFF1B5E20)
              item.pendingPaise > 0L -> Color(0xFFC62828)
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(10.dp))

      // 3-Column Stats: Total Bill | Deposited | Left to Pay
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Bill",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyUtils.formatPaise(item.payablePaise),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("employee_bill_${employee.employeeCode}")
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Deposited",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyUtils.formatPaise(item.paidPaise),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Left to Pay",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = CurrencyUtils.formatPaise(item.pendingPaise),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (item.pendingPaise > 0) Color(0xFFC62828) else Color(0xFF2E7D32)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Meals count subtitle
      Text(
        text = if (hasMeals) {
          "${item.totalMeals} meals (B: ${item.breakfastCount} • L: ${item.lunchCount} • D: ${item.dinnerCount})"
        } else {
          "0 meals taken this month"
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        modifier = Modifier.testTag("employee_meals_${employee.employeeCode}")
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Quick Action Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Record Deposit Button
        Button(
          onClick = onOpenDeposit,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Deposit", style = MaterialTheme.typography.labelSmall)
        }

        // Passbook Button
        OutlinedButton(
          onClick = onOpenPassbook,
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.weight(1f)
        ) {
          Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Passbook", style = MaterialTheme.typography.labelSmall)
        }

        // UPI QR Button
        if (item.pendingPaise > 0 || item.payablePaise > 0) {
          OutlinedButton(
            onClick = onOpenUpi,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.QrCode2, contentDescription = "UPI QR", modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun EmployeeBillDetailDialog(
  item: EmployeeReportItem,
  formattedMonth: String,
  costPerMealRupees: Double,
  onOpenDeposit: () -> Unit,
  onOpenPassbook: () -> Unit,
  onOpenUpi: () -> Unit,
  onDismiss: () -> Unit
) {
  val employee = item.employee
  val context = LocalContext.current

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = onOpenDeposit,
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Record Deposit")
        }
        TextButton(onClick = onDismiss) {
          Text("Close")
        }
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
        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
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
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Balance Summary
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Total Bill", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyUtils.formatPaise(item.payablePaise), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Deposited", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyUtils.formatPaise(item.paidPaise), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Left to Pay", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(CurrencyUtils.formatPaise(item.pendingPaise), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (item.pendingPaise > 0) Color(0xFFC62828) else Color(0xFF2E7D32))
            }
          }
        }

        // Meal breakdown
        DetailMealRow(label = "Breakfast Meals", count = item.breakfastCount)
        HorizontalDivider()
        DetailMealRow(label = "Lunch Meals", count = item.lunchCount)
        HorizontalDivider()
        DetailMealRow(label = "Dinner Meals", count = item.dinnerCount)
        HorizontalDivider(thickness = 1.5.dp)
        DetailMealRow(label = "Total Meals", count = item.totalMeals)

        Spacer(modifier = Modifier.height(6.dp))

        // Action Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onOpenPassbook,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Passbook (${item.payments.size})", style = MaterialTheme.typography.labelSmall)
          }

          if (item.pendingPaise > 0 || item.payablePaise > 0) {
            OutlinedButton(
              onClick = onOpenUpi,
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("UPI QR", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }
  )
}

@Composable
private fun DetailMealRow(label: String, count: Int) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = "$count", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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
        text = if (searchQuery.isNotBlank()) "No employees match \"$searchQuery\""
        else if (showOnlyWithMeals) "No employees took meals in this month"
        else "No employee records found",
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
