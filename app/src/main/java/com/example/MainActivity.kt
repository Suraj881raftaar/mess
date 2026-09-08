package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.database.MessDatabase
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MenuRepository
import com.example.ui.attendance.AttendanceScreen
import com.example.ui.attendance.AttendanceViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.employees.EmployeesScreen
import com.example.ui.employees.EmployeesViewModel
import com.example.ui.expenses.ExpenseScreen
import com.example.ui.expenses.ExpenseViewModel
import com.example.ui.menu.MenuScreen
import com.example.ui.menu.MenuViewModel
import com.example.ui.reports.ReportsScreen
import com.example.ui.reports.ReportsViewModel
import com.example.ui.theme.MyApplicationTheme

enum class MainDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
  DASHBOARD("Dashboard", Icons.Default.Dashboard),
  EMPLOYEES("Staff", Icons.Default.Badge),
  ATTENDANCE("Meals", Icons.Default.Restaurant),
  MENU("Menu", Icons.Default.RestaurantMenu),
  EXPENSES("Expense", Icons.AutoMirrored.Filled.ReceiptLong),
  REPORTS("Reports", Icons.Default.Assessment)
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
    MenuRepository(database.menuDao())
  }

  private val expenseRepository by lazy {
    val database = MessDatabase.getDatabase(applicationContext)
    ExpenseRepository(database.expenseDao())
  }

  private val dashboardViewModel: DashboardViewModel by viewModels {
    DashboardViewModel.Factory(
      employeeRepository,
      attendanceRepository,
      menuRepository,
      expenseRepository
    )
  }

  private val employeesViewModel: EmployeesViewModel by viewModels {
    EmployeesViewModel.Factory(employeeRepository)
  }

  private val attendanceViewModel: AttendanceViewModel by viewModels {
    AttendanceViewModel.Factory(attendanceRepository, employeeRepository)
  }

  private val menuViewModel: MenuViewModel by viewModels {
    MenuViewModel.Factory(menuRepository)
  }

  private val expenseViewModel: ExpenseViewModel by viewModels {
    ExpenseViewModel.Factory(expenseRepository)
  }

  private val reportsViewModel: ReportsViewModel by viewModels {
    ReportsViewModel.Factory(
      employeeRepository,
      attendanceRepository,
      expenseRepository
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
          expenseViewModel = expenseViewModel,
          reportsViewModel = reportsViewModel
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
  expenseViewModel: ExpenseViewModel,
  reportsViewModel: ReportsViewModel,
  modifier: Modifier = Modifier
) {
  var currentDestination by remember { mutableStateOf(MainDestination.DASHBOARD) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    bottomBar = {
      NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
        MainDestination.values().forEach { destination ->
          NavigationBarItem(
            selected = currentDestination == destination,
            onClick = { currentDestination = destination },
            icon = { Icon(destination.icon, contentDescription = destination.label) },
            label = { Text(destination.label) },
            modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
          )
        }
      }
    }
  ) { innerPadding ->
    Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      when (currentDestination) {
        MainDestination.DASHBOARD -> {
          DashboardScreen(
            viewModel = dashboardViewModel,
            onNavigateToAttendance = { currentDestination = MainDestination.ATTENDANCE },
            onNavigateToMenu = { currentDestination = MainDestination.MENU },
            onNavigateToExpenses = { currentDestination = MainDestination.EXPENSES },
            onNavigateToEmployees = { currentDestination = MainDestination.EMPLOYEES },
            onNavigateToReports = { currentDestination = MainDestination.REPORTS }
          )
        }
        MainDestination.ATTENDANCE -> {
          AttendanceScreen(
            viewModel = attendanceViewModel,
            onNavigateToEmployees = { currentDestination = MainDestination.EMPLOYEES }
          )
        }
        MainDestination.MENU -> {
          MenuScreen(viewModel = menuViewModel)
        }
        MainDestination.EXPENSES -> {
          ExpenseScreen(viewModel = expenseViewModel)
        }
        MainDestination.EMPLOYEES -> {
          EmployeesScreen(viewModel = employeesViewModel)
        }
        MainDestination.REPORTS -> {
          ReportsScreen(viewModel = reportsViewModel)
        }
      }
    }
  }
}
