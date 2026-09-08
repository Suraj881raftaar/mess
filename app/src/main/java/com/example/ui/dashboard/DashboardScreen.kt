package com.example.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.CurrencyUtils

@Composable
fun DashboardScreen(
  viewModel: DashboardViewModel,
  onNavigateToAttendance: () -> Unit,
  onNavigateToMenu: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onNavigateToEmployees: () -> Unit,
  onNavigateToReports: () -> Unit = {},
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
    // Top Hero Header
    item {
      DashboardHeader(
        formattedDate = uiState.formattedDate,
        activeEmployeeCount = uiState.activeEmployeeCount,
        onNavigateToEmployees = onNavigateToEmployees
      )
    }

    // Key Stats Summary Cards
    item {
      KeyMetricsOverview(
        costPerMealRupees = uiState.currentMonthCostPerMealRupees,
        totalMonthExpensesPaise = uiState.currentMonthTotalExpensePaise,
        totalMonthMeals = uiState.currentMonthTotalMeals,
        totalTodayMeals = uiState.totalTodayMeals
      )
    }

    // Today's Attendance Card
    item {
      TodayAttendanceCard(
        breakfastCount = uiState.todayBreakfastCount,
        lunchCount = uiState.todayLunchCount,
        dinnerCount = uiState.todayDinnerCount,
        totalMeals = uiState.totalTodayMeals,
        onTakeAttendance = onNavigateToAttendance
      )
    }

    // Today's Menu Card
    item {
      TodayMenuCard(
        breakfastMenu = uiState.todayBreakfastMenu?.description,
        lunchMenu = uiState.todayLunchMenu?.description,
        dinnerMenu = uiState.todayDinnerMenu?.description,
        onManageMenu = onNavigateToMenu
      )
    }

    // Current Month Financial & Cost per Meal Card
    item {
      MonthlyFinancialCard(
        formattedMonth = uiState.formattedMonth,
        totalExpensePaise = uiState.currentMonthTotalExpensePaise,
        totalMeals = uiState.currentMonthTotalMeals,
        costPerMealRupees = uiState.currentMonthCostPerMealRupees,
        onManageExpenses = onNavigateToExpenses,
        onNavigateToReports = onNavigateToReports
      )
    }

    // Quick Actions
    item {
      QuickActionsSection(
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToMenu = onNavigateToMenu,
        onNavigateToExpenses = onNavigateToExpenses,
        onNavigateToEmployees = onNavigateToEmployees,
        onNavigateToReports = onNavigateToReports
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun DashboardHeader(
  formattedDate: String,
  activeEmployeeCount: Int,
  onNavigateToEmployees: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    ),
    shape = RoundedCornerShape(20.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Office Mess Manager",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = formattedDate,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
              fontWeight = FontWeight.Medium
            )
          }
        }

        Surface(
          onClick = onNavigateToEmployees,
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 2.dp,
          modifier = Modifier.testTag("btn_navigate_employees")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "$activeEmployeeCount Active",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("active_employee_count")
              )
              Text(
                text = "Employees",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun KeyMetricsOverview(
  costPerMealRupees: Double,
  totalMonthExpensesPaise: Long,
  totalMonthMeals: Int,
  totalTodayMeals: Int
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Cost per Meal Metric Card
    ElevatedCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.TrendingUp,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }
          Text(
            text = "This Month",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = if (totalMonthMeals > 0) CurrencyUtils.formatRupees(costPerMealRupees) else "₹0.00",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.testTag("cost_per_meal")
        )
        Text(
          text = "Cost per Meal",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Today's Meals Metric Card
    ElevatedCard(
      modifier = Modifier.weight(1f),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Restaurant,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onTertiaryContainer,
              modifier = Modifier.size(20.dp)
            )
          }
          Text(
            text = "Today",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "$totalTodayMeals",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.tertiary,
          modifier = Modifier.testTag("today_total_meals")
        )
        Text(
          text = "Total Meals Today",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun TodayAttendanceCard(
  breakfastCount: Int,
  lunchCount: Int,
  dinnerCount: Int,
  totalMeals: Int,
  onTakeAttendance: () -> Unit
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Today's Meal Attendance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer
        ) {
          Text(
            text = "$totalMeals served",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Breakdown in 3 meal columns
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MealAttendanceItem(
          title = "Breakfast",
          count = breakfastCount,
          icon = Icons.Default.FreeBreakfast,
          testTag = "today_breakfast_count",
          modifier = Modifier.weight(1f)
        )
        MealAttendanceItem(
          title = "Lunch",
          count = lunchCount,
          icon = Icons.Default.LunchDining,
          testTag = "today_lunch_count",
          modifier = Modifier.weight(1f)
        )
        MealAttendanceItem(
          title = "Dinner",
          count = dinnerCount,
          icon = Icons.Default.DinnerDining,
          testTag = "today_dinner_count",
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Button(
        onClick = onTakeAttendance,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_navigate_attendance"),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Restaurant,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Mark Attendance")
      }
    }
  }
}

@Composable
private fun MealAttendanceItem(
  title: String,
  count: Int,
  icon: ImageVector,
  testTag: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "$count",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag(testTag)
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
private fun TodayMenuCard(
  breakfastMenu: String?,
  lunchMenu: String?,
  dinnerMenu: String?,
  onManageMenu: () -> Unit
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.RestaurantMenu,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Today's Menu",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        OutlinedButton(
          onClick = onManageMenu,
          modifier = Modifier.testTag("btn_navigate_menu"),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Manage Menu", style = MaterialTheme.typography.labelSmall)
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Breakfast Row
      MenuRowItem(
        label = "Breakfast",
        description = breakfastMenu,
        icon = Icons.Default.FreeBreakfast,
        testTag = "today_breakfast_menu"
      )

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )

      // Lunch Row
      MenuRowItem(
        label = "Lunch",
        description = lunchMenu,
        icon = Icons.Default.LunchDining,
        testTag = "today_lunch_menu"
      )

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
      )

      // Dinner Row
      MenuRowItem(
        label = "Dinner",
        description = dinnerMenu,
        icon = Icons.Default.DinnerDining,
        testTag = "today_dinner_menu"
      )
    }
  }
}

@Composable
private fun MenuRowItem(
  label: String,
  description: String?,
  icon: ImageVector,
  testTag: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(16.dp)
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = if (description.isNullOrBlank()) "No menu planned yet" else description,
        style = MaterialTheme.typography.bodyMedium,
        color = if (description.isNullOrBlank()) {
          MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        } else {
          MaterialTheme.colorScheme.onSurface
        },
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.testTag(testTag)
      )
    }
  }
}

@Composable
private fun MonthlyFinancialCard(
  formattedMonth: String,
  totalExpensePaise: Long,
  totalMeals: Int,
  costPerMealRupees: Double,
  onManageExpenses: () -> Unit,
  onNavigateToReports: () -> Unit
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f, fill = false),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AccountBalanceWallet,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Financials ($formattedMonth)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onManageExpenses,
            modifier = Modifier.testTag("btn_navigate_expenses"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Expenses", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(12.dp)
            )
          }

          Button(
            onClick = onNavigateToReports,
            modifier = Modifier.testTag("btn_navigate_reports"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Reports", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Stats row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedCard(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Total Expenses",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyUtils.formatPaise(totalExpensePaise),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.testTag("monthly_expenses")
            )
          }
        }

        OutlinedCard(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Total Meals Served",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "$totalMeals meals",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.testTag("monthly_meals")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Cost calculation highlight card
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Shared Cost per Meal",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = if (totalMeals > 0) {
                "${CurrencyUtils.formatPaise(totalExpensePaise)} ÷ $totalMeals meals"
              } else {
                "No meals served yet this month"
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
          }

          Text(
            text = if (totalMeals > 0) CurrencyUtils.formatRupees(costPerMealRupees) else "₹0.00",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun QuickActionsSection(
  onNavigateToAttendance: () -> Unit,
  onNavigateToMenu: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onNavigateToEmployees: () -> Unit,
  onNavigateToReports: () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Quick Navigation",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickActionButton(
        label = "Attendance",
        icon = Icons.Default.Restaurant,
        onClick = onNavigateToAttendance,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        label = "Menu",
        icon = Icons.Default.RestaurantMenu,
        onClick = onNavigateToMenu,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        label = "Expenses",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        onClick = onNavigateToExpenses,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      QuickActionButton(
        label = "Staff Directory",
        icon = Icons.Default.Badge,
        onClick = onNavigateToEmployees,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        label = "Reports & Bill",
        icon = Icons.Default.Assessment,
        onClick = onNavigateToReports,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier.height(68.dp),
    shape = RoundedCornerShape(12.dp),
    contentPadding = PaddingValues(4.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
