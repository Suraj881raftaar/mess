package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.database.MessDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.BackupRepository
import com.example.data.repository.DataManagementRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MealFeedbackRepository
import com.example.data.repository.MenuRepository
import com.example.data.repository.PantryRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.attendance.AttendanceScreen
import com.example.ui.attendance.AttendanceViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.employees.EmployeesScreen
import com.example.ui.employees.EmployeesViewModel
import com.example.ui.expenses.ExpenseScreen
import com.example.ui.expenses.ExpenseViewModel
import com.example.ui.hub.MoreHubScreen
import com.example.ui.menu.MenuScreen
import com.example.ui.menu.MenuViewModel
import com.example.ui.pantry.PantryScreen
import com.example.ui.pantry.PantryViewModel
import com.example.ui.reports.ReportsScreen
import com.example.ui.reports.ReportsViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme

enum class MainNavTab(
  val label: String,
  val icon: ImageVector,
  val testTag: String
) {
  DASHBOARD("Home", Icons.Default.Dashboard, "nav_item_dashboard"),
  ATTENDANCE("Meals", Icons.Default.Restaurant, "nav_item_attendance"),
  REPORTS("Billing", Icons.Default.Assessment, "nav_item_reports"),
  MENU("Menu", Icons.Default.RestaurantMenu, "nav_item_menu"),
  MORE("More", Icons.Default.GridView, "nav_item_more")
}

enum class ScreenDestination {
  DASHBOARD,
  ATTENDANCE,
  REPORTS,
  MENU,
  MORE_HUB,
  EMPLOYEES,
  PANTRY,
  EXPENSES,
  SETTINGS
}

class MainActivity : ComponentActivity() {

  private val employeeRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    EmployeeRepository(database.employeeDao())
  }

  private val attendanceRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    AttendanceRepository(database.mealAttendanceDao())
  }

  private val menuRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    MenuRepository(database.menuDao(), database.menuTemplateDao())
  }

  private val expenseRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    ExpenseRepository(database.expenseDao())
  }

  private val paymentRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    PaymentRepository(database.paymentDao())
  }

  private val settingsRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    SettingsRepository(database.messSettingDao())
  }

  private val feedbackRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    MealFeedbackRepository(database.mealFeedbackDao())
  }

  private val pantryRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    PantryRepository(database.pantryDao(), database.expenseDao())
  }

  private val backupRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    BackupRepository(applicationContext, database)
  }

  private val dataManagementRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    DataManagementRepository(database)
  }

  private val dashboardViewModel: DashboardViewModel by viewModels {
    DashboardViewModel.Factory(
      employeeRepository,
      attendanceRepository,
      menuRepository,
      expenseRepository,
      paymentRepository,
      settingsRepository
    )
  }

  private val employeesViewModel: EmployeesViewModel by viewModels {
    EmployeesViewModel.Factory(employeeRepository)
  }

  private val attendanceViewModel: AttendanceViewModel by viewModels {
    AttendanceViewModel.Factory(attendanceRepository, employeeRepository)
  }

  private val menuViewModel: MenuViewModel by viewModels {
    MenuViewModel.Factory(menuRepository, feedbackRepository)
  }

  private val pantryViewModel: PantryViewModel by viewModels {
    PantryViewModel.Factory(pantryRepository)
  }

  private val expenseViewModel: ExpenseViewModel by viewModels {
    ExpenseViewModel.Factory(expenseRepository)
  }

  private val reportsViewModel: ReportsViewModel by viewModels {
    ReportsViewModel.Factory(
      employeeRepository,
      attendanceRepository,
      expenseRepository,
      paymentRepository
    )
  }

  private val settingsViewModel: SettingsViewModel by viewModels {
    SettingsViewModel.Factory(
      settingsRepository,
      backupRepository,
      dataManagementRepository
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        MainApp(
          dashboardViewModel = dashboardViewModel,
          employeesViewModel = employeesViewModel,
          attendanceViewModel = attendanceViewModel,
          menuViewModel = menuViewModel,
          pantryViewModel = pantryViewModel,
          expenseViewModel = expenseViewModel,
          reportsViewModel = reportsViewModel,
          settingsViewModel = settingsViewModel
        )
      }
    }
  }
}

@Composable
fun MainApp(
  dashboardViewModel: DashboardViewModel,
  employeesViewModel: EmployeesViewModel,
  attendanceViewModel: AttendanceViewModel,
  menuViewModel: MenuViewModel,
  pantryViewModel: PantryViewModel,
  expenseViewModel: ExpenseViewModel,
  reportsViewModel: ReportsViewModel,
  settingsViewModel: SettingsViewModel,
  modifier: Modifier = Modifier
) {
  var currentDestination by remember { mutableStateOf(ScreenDestination.DASHBOARD) }
  val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
  val pantryUiState by pantryViewModel.uiState.collectAsStateWithLifecycle()
  val expenseUiState by expenseViewModel.uiState.collectAsStateWithLifecycle()

  val selectedTab = when (currentDestination) {
    ScreenDestination.DASHBOARD -> MainNavTab.DASHBOARD
    ScreenDestination.ATTENDANCE -> MainNavTab.ATTENDANCE
    ScreenDestination.REPORTS -> MainNavTab.REPORTS
    ScreenDestination.MENU -> MainNavTab.MENU
    ScreenDestination.MORE_HUB,
    ScreenDestination.EMPLOYEES,
    ScreenDestination.PANTRY,
    ScreenDestination.EXPENSES,
    ScreenDestination.SETTINGS -> MainNavTab.MORE
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
        MainNavTab.values().forEach { tab ->
          val isSelected = selectedTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = {
              currentDestination = when (tab) {
                MainNavTab.DASHBOARD -> ScreenDestination.DASHBOARD
                MainNavTab.ATTENDANCE -> ScreenDestination.ATTENDANCE
                MainNavTab.REPORTS -> ScreenDestination.REPORTS
                MainNavTab.MENU -> ScreenDestination.MENU
                MainNavTab.MORE -> ScreenDestination.MORE_HUB
              }
            },
            icon = {
              Icon(
                tab.icon,
                contentDescription = tab.label,
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              )
            },
            alwaysShowLabel = true,
            modifier = Modifier.testTag(tab.testTag)
          )
        }
      }
    }
  ) { innerPadding ->
    Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      when (currentDestination) {
        ScreenDestination.DASHBOARD -> {
          DashboardScreen(
            viewModel = dashboardViewModel,
            onNavigateToAttendance = { currentDestination = ScreenDestination.ATTENDANCE },
            onNavigateToMenu = { currentDestination = ScreenDestination.MENU },
            onNavigateToExpenses = { currentDestination = ScreenDestination.EXPENSES },
            onNavigateToEmployees = { currentDestination = ScreenDestination.EMPLOYEES },
            onNavigateToReports = { currentDestination = ScreenDestination.REPORTS },
            onNavigateToSettings = { currentDestination = ScreenDestination.SETTINGS }
          )
        }

        ScreenDestination.ATTENDANCE -> {
          AttendanceScreen(
            viewModel = attendanceViewModel,
            onNavigateToEmployees = { currentDestination = ScreenDestination.EMPLOYEES }
          )
        }

        ScreenDestination.REPORTS -> {
          ReportsScreen(viewModel = reportsViewModel)
        }

        ScreenDestination.MENU -> {
          MenuScreen(viewModel = menuViewModel)
        }

        ScreenDestination.MORE_HUB -> {
          MoreHubScreen(
            activeEmployeeCount = dashboardUiState.activeEmployeeCount,
            totalExpensesCount = expenseUiState.expenses.size,
            pantryItemsCount = pantryUiState.items.size,
            onNavigateToEmployees = { currentDestination = ScreenDestination.EMPLOYEES },
            onNavigateToExpenses = { currentDestination = ScreenDestination.EXPENSES },
            onNavigateToPantry = { currentDestination = ScreenDestination.PANTRY },
            onNavigateToSettings = { currentDestination = ScreenDestination.SETTINGS }
          )
        }

        ScreenDestination.EMPLOYEES -> {
          Column(modifier = Modifier.fillMaxSize()) {
            SubScreenHeader(
              title = "Staff Directory",
              onBack = { currentDestination = ScreenDestination.MORE_HUB }
            )
            EmployeesScreen(viewModel = employeesViewModel, modifier = Modifier.weight(1f))
          }
        }

        ScreenDestination.PANTRY -> {
          Column(modifier = Modifier.fillMaxSize()) {
            SubScreenHeader(
              title = "Pantry Inventory",
              onBack = { currentDestination = ScreenDestination.MORE_HUB }
            )
            PantryScreen(viewModel = pantryViewModel, modifier = Modifier.weight(1f))
          }
        }

        ScreenDestination.EXPENSES -> {
          Column(modifier = Modifier.fillMaxSize()) {
            SubScreenHeader(
              title = "Expense Register",
              onBack = { currentDestination = ScreenDestination.MORE_HUB }
            )
            ExpenseScreen(viewModel = expenseViewModel, modifier = Modifier.weight(1f))
          }
        }

        ScreenDestination.SETTINGS -> {
          SettingsScreen(
            viewModel = settingsViewModel,
            onNavigateBack = { currentDestination = ScreenDestination.MORE_HUB }
          )
        }
      }
    }
  }
}

@Composable
private fun SubScreenHeader(
  title: String,
  onBack: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onBack) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to Hub"
        )
      }
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Back to Hub",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable { onBack() }
      )
    }
  }
}
