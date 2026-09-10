package com.example.ui.dashboard

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel,
  onNavigateToAttendance: () -> Unit,
  onNavigateToMenu: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onNavigateToEmployees: () -> Unit,
  onNavigateToReports: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Airy Header
    item {
      DashboardHeader(
        messName = uiState.messName,
        formattedDate = uiState.formattedDate,
        activeEmployeeCount = uiState.activeEmployeeCount,
        onNavigateToEmployees = onNavigateToEmployees,
        onNavigateToSettings = onNavigateToSettings
      )
    }

    // 2. Subtle Alerts Chip (Non-bloated)
    if (uiState.unmarkedMealsToday.isNotEmpty() || uiState.currentMonthPendingPaise > 0) {
      item {
        DashboardAlertChip(
          unmarkedMeals = uiState.unmarkedMealsToday.map { it.displayName },
          pendingPaise = uiState.currentMonthPendingPaise,
          onMarkAttendance = onNavigateToAttendance,
          onViewBills = onNavigateToReports
        )
      }
    }

    // 3. Hero Financial Pulse (Total Collection & Total Expense)
    item {
      HeroFinancialPulseCard(
        formattedMonth = uiState.formattedMonth,
        totalCollectionPaise = uiState.currentMonthCollectedPaise,
        totalExpensePaise = uiState.currentMonthTotalExpensePaise,
        pendingPaise = uiState.currentMonthPendingPaise,
        collectionPercentage = uiState.currentMonthCollectionPercentage,
        costPerMealRupees = uiState.currentMonthCostPerMealRupees,
        totalMeals = uiState.currentMonthTotalMeals,
        onViewBilling = onNavigateToReports
      )
    }

    // 4. Today's Meals & Menu
    item {
      TodayKitchenOverviewCard(
        breakfastCount = uiState.todayBreakfastCount,
        lunchCount = uiState.todayLunchCount,
        dinnerCount = uiState.todayDinnerCount,
        totalTodayMeals = uiState.totalTodayMeals,
        breakfastMenu = uiState.todayBreakfastMenu?.description,
        lunchMenu = uiState.todayLunchMenu?.description,
        dinnerMenu = uiState.todayDinnerMenu?.description,
        onTakeAttendance = onNavigateToAttendance,
        onManageMenu = onNavigateToMenu
      )
    }

    // 5. Spacious Quick Navigation Tiles
    item {
      QuickNavigationGrid(
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToReports = onNavigateToReports,
        onNavigateToExpenses = onNavigateToExpenses,
        onNavigateToEmployees = onNavigateToEmployees,
        onNavigateToMenu = onNavigateToMenu
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun DashboardHeader(
  messName: String,
  formattedDate: String,
  activeEmployeeCount: Int,
  onNavigateToEmployees: () -> Unit,
  onNavigateToSettings: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 4.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = messName.ifBlank { "Office Mess Manager" },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(2.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CalendarMonth,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = formattedDate,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      // Active Staff Chip
      Surface(
        onClick = onNavigateToEmployees,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        modifier = Modifier.testTag("btn_navigate_employees")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$activeEmployeeCount Staff",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.testTag("active_employee_count")
          )
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      IconButton(
        onClick = onNavigateToSettings,
        modifier = Modifier.testTag("dashboard_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun DashboardAlertChip(
  unmarkedMeals: List<String>,
  pendingPaise: Long,
  onMarkAttendance: () -> Unit,
  onViewBills: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onTertiaryContainer,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (unmarkedMeals.isNotEmpty()) {
            "Record ${unmarkedMeals.joinToString(", ")} attendance"
          } else {
            "${CurrencyUtils.formatPaise(pendingPaise)} dues left to collect"
          },
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onTertiaryContainer,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      TextButton(
        onClick = if (unmarkedMeals.isNotEmpty()) onMarkAttendance else onViewBills,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
      ) {
        Text(
          text = if (unmarkedMeals.isNotEmpty()) "Mark Now" else "Collect",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
private fun HeroFinancialPulseCard(
  formattedMonth: String,
  totalCollectionPaise: Long,
  totalExpensePaise: Long,
  pendingPaise: Long,
  collectionPercentage: Float,
  costPerMealRupees: Double,
  totalMeals: Int,
  onViewBilling: () -> Unit
) {
  Card(
    onClick = onViewBilling,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("btn_navigate_reports"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header
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
            text = "Financial Overview",
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
            text = formattedMonth,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Total Collection vs Total Expense Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Total Collection
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFE8F5E9)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Total Collection",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF1B5E20),
                fontWeight = FontWeight.Medium
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalCollectionPaise),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B5E20)
            )
          }
        }

        // Total Expense
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFFFFF3E0)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = Color(0xFFD84315),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Total Expense",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFBF360C),
                fontWeight = FontWeight.Medium
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalExpensePaise),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFBF360C),
              modifier = Modifier.testTag("monthly_expenses")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Progress Bar of Collection
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Collection Rate: ${String.format(java.util.Locale.US, "%.0f%%", collectionPercentage)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Left to Collect: ${CurrencyUtils.formatPaise(pendingPaise)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (pendingPaise > 0) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
          )
        }
        Spacer(modifier = Modifier.height(6.dp))
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
      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row: Cost per meal + Total Meals + View Bills Action
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
              modifier = Modifier.testTag("cost_per_meal")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "($totalMeals meals)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.testTag("monthly_meals")
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text(
            text = "View Ledger",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun TodayKitchenOverviewCard(
  breakfastCount: Int,
  lunchCount: Int,
  dinnerCount: Int,
  totalTodayMeals: Int,
  breakfastMenu: String?,
  lunchMenu: String?,
  dinnerMenu: String?,
  onTakeAttendance: () -> Unit,
  onManageMenu: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Today's Kitchen",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
          Text(
            text = "$totalTodayMeals Served",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
              .padding(horizontal = 8.dp, vertical = 4.dp)
              .testTag("today_total_meals")
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3 Meal Attendance Badges
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MealHeadcountPill(
          title = "Breakfast",
          count = breakfastCount,
          icon = Icons.Default.FreeBreakfast,
          accentColor = BreakfastAccent,
          containerColor = BreakfastContainerLight,
          modifier = Modifier.weight(1f)
        )
        MealHeadcountPill(
          title = "Lunch",
          count = lunchCount,
          icon = Icons.Default.LunchDining,
          accentColor = LunchAccent,
          containerColor = LunchContainerLight,
          modifier = Modifier.weight(1f)
        )
        MealHeadcountPill(
          title = "Dinner",
          count = dinnerCount,
          icon = Icons.Default.DinnerDining,
          accentColor = DinnerAccent,
          containerColor = DinnerContainerLight,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Today's Menu Highlight
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Today's Menu",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = when {
              !lunchMenu.isNullOrBlank() -> "Lunch: $lunchMenu"
              !dinnerMenu.isNullOrBlank() -> "Dinner: $dinnerMenu"
              !breakfastMenu.isNullOrBlank() -> "Breakfast: $breakfastMenu"
              else -> "No menu items planned for today yet"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onTakeAttendance,
          modifier = Modifier
            .weight(1f)
            .testTag("btn_navigate_attendance"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Attendance", style = MaterialTheme.typography.labelMedium)
        }

        OutlinedButton(
          onClick = onManageMenu,
          modifier = Modifier
            .weight(1f)
            .testTag("btn_navigate_menu"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(text = "Menu", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}

@Composable
private fun MealHeadcountPill(
  title: String,
  count: Int,
  icon: ImageVector,
  accentColor: Color,
  containerColor: Color,
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
        .padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun QuickNavigationGrid(
  onNavigateToAttendance: () -> Unit,
  onNavigateToReports: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onNavigateToEmployees: () -> Unit,
  onNavigateToMenu: () -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Text(
      text = "Quick Actions",
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 4.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickActionTile(
        title = "Mark Meals",
        subtitle = "Daily Check-in",
        icon = Icons.Default.Restaurant,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        iconTint = MaterialTheme.colorScheme.primary,
        onClick = onNavigateToAttendance,
        modifier = Modifier.weight(1f)
      )

      QuickActionTile(
        title = "Deposits & Bills",
        subtitle = "Collections & Dues",
        icon = Icons.Default.Assessment,
        containerColor = Color(0xFFE8F5E9),
        iconTint = Color(0xFF2E7D32),
        onClick = onNavigateToReports,
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickActionTile(
        title = "Add Expense",
        subtitle = "Groceries & Bills",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        containerColor = Color(0xFFFFF3E0),
        iconTint = Color(0xFFE65100),
        onClick = onNavigateToExpenses,
        testTag = "btn_navigate_expenses",
        modifier = Modifier.weight(1f)
      )

      QuickActionTile(
        title = "Food Menu",
        subtitle = "Weekly Schedule",
        icon = Icons.Default.RestaurantMenu,
        containerColor = Color(0xFFEDE7F6),
        iconTint = Color(0xFF512DA8),
        onClick = onNavigateToMenu,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun QuickActionTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: Color,
  iconTint: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String? = null
) {
  Card(
    onClick = onClick,
    modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(containerColor.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}
