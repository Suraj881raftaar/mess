package com.example.ui.dashboard

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MenuRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DashboardViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: MessDatabase
  private lateinit var employeeRepository: EmployeeRepository
  private lateinit var attendanceRepository: AttendanceRepository
  private lateinit var menuRepository: MenuRepository
  private lateinit var expenseRepository: ExpenseRepository
  private lateinit var paymentRepository: PaymentRepository
  private lateinit var settingsRepository: SettingsRepository
  private lateinit var viewModel: DashboardViewModel

  private val testDate = "2026-09-08"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    employeeRepository = EmployeeRepository(db.employeeDao())
    attendanceRepository = AttendanceRepository(db.mealAttendanceDao())
    menuRepository = MenuRepository(db.menuDao(), db.menuTemplateDao())
    expenseRepository = ExpenseRepository(db.expenseDao())
    paymentRepository = PaymentRepository(db.paymentDao())
    settingsRepository = SettingsRepository(db.messSettingDao())

    viewModel = DashboardViewModel(
      employeeRepository,
      attendanceRepository,
      menuRepository,
      expenseRepository,
      paymentRepository,
      settingsRepository
    )
    viewModel.setDate(testDate)
  }

  @After
  fun teardown() {
    db.close()
    Dispatchers.resetMain()
  }

  @Test
  fun testEmptyDashboardInitialState() = runBlocking {
    val state = viewModel.uiState.first { !it.isLoading }
    assertEquals(0, state.activeEmployeeCount)
    assertEquals(0, state.todayBreakfastCount)
    assertEquals(0, state.todayLunchCount)
    assertEquals(0, state.todayDinnerCount)
    assertEquals(0, state.totalTodayMeals)
    assertEquals(0L, state.currentMonthTotalExpensePaise)
    assertEquals(0, state.currentMonthTotalMeals)
    assertEquals(0.0, state.currentMonthCostPerMealRupees, 0.0001)
    assertNull(state.todayBreakfastMenu)
    assertNull(state.todayLunchMenu)
    assertNull(state.todayDinnerMenu)
  }

  @Test
  fun testActiveEmployeeCountReactive() = runBlocking {
    val emp1Id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2Id = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()

    val stateWithTwo = viewModel.uiState.first { it.activeEmployeeCount == 2 }
    assertEquals(2, stateWithTwo.activeEmployeeCount)

    // Deactivate one employee
    employeeRepository.deactivateEmployee(emp1Id)

    val stateWithOne = viewModel.uiState.first { it.activeEmployeeCount == 1 }
    assertEquals(1, stateWithOne.activeEmployeeCount)
  }

  @Test
  fun testTodayMealAttendanceCounts() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()
    val emp3 = employeeRepository.addEmployee("EMP003", "Priya Joshi", "HR").getOrThrow()

    // Breakfast: 2 present (emp1, emp2)
    attendanceRepository.setAttendance(emp1, testDate, MealType.BREAKFAST, true)
    attendanceRepository.setAttendance(emp2, testDate, MealType.BREAKFAST, true)
    attendanceRepository.setAttendance(emp3, testDate, MealType.BREAKFAST, false)

    // Lunch: 3 present (emp1, emp2, emp3)
    attendanceRepository.setAttendance(emp1, testDate, MealType.LUNCH, true)
    attendanceRepository.setAttendance(emp2, testDate, MealType.LUNCH, true)
    attendanceRepository.setAttendance(emp3, testDate, MealType.LUNCH, true)

    // Dinner: 1 present (emp1)
    attendanceRepository.setAttendance(emp1, testDate, MealType.DINNER, true)
    attendanceRepository.setAttendance(emp2, testDate, MealType.DINNER, false)
    attendanceRepository.setAttendance(emp3, testDate, MealType.DINNER, false)

    val state = viewModel.uiState.first { it.totalTodayMeals == 6 }
    assertEquals(2, state.todayBreakfastCount)
    assertEquals(3, state.todayLunchCount)
    assertEquals(1, state.todayDinnerCount)
    assertEquals(6, state.totalTodayMeals)
  }

  @Test
  fun testTodayMenus() = runBlocking {
    menuRepository.setMenu(testDate, MealType.BREAKFAST, "Poha + Masala Chai")
    menuRepository.setMenu(testDate, MealType.LUNCH, "Dal Tadka + Jeera Rice + Roti")
    menuRepository.setMenu(testDate, MealType.DINNER, "Paneer Butter Masala + Phulka")

    val state = viewModel.uiState.first {
      it.todayBreakfastMenu != null && it.todayLunchMenu != null && it.todayDinnerMenu != null
    }

    assertEquals("Poha + Masala Chai", state.todayBreakfastMenu?.description)
    assertEquals("Dal Tadka + Jeera Rice + Roti", state.todayLunchMenu?.description)
    assertEquals("Paneer Butter Masala + Phulka", state.todayDinnerMenu?.description)
  }

  @Test
  fun testMonthFinancialCalculations() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()

    // Add meals for September: total 10 meals
    for (day in 1..5) {
      val d = "2026-09-0$day"
      attendanceRepository.setAttendance(emp1, d, MealType.LUNCH, true)
      attendanceRepository.setAttendance(emp2, d, MealType.LUNCH, true)
    }

    // Add expenses for September: Total = ₹5,000 (500000 paise)
    expenseRepository.addExpense(
      date = "2026-09-02",
      description = "Groceries Stock",
      category = ExpenseCategory.GROCERIES,
      amountPaise = 300000L // ₹3,000
    )
    expenseRepository.addExpense(
      date = "2026-09-04",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountPaise = 200000L // ₹2,000
    )

    val state = viewModel.uiState.first { it.currentMonthTotalExpensePaise == 500000L && it.currentMonthTotalMeals == 10 }
    assertEquals(500000L, state.currentMonthTotalExpensePaise)
    assertEquals(10, state.currentMonthTotalMeals)

    // Cost per meal: ₹5,000 / 10 meals = ₹500.00
    assertEquals(500.0, state.currentMonthCostPerMealRupees, 0.001)
  }

  @Test
  fun testPaymentTrackingInDashboard() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()

    paymentRepository.recordPayment(
      employeeId = emp1,
      month = "2026-09",
      amountPaise = 2500000L, // ₹25,000
      paymentDate = "2026-09-08",
      paymentMethod = "Cash"
    )

    val state = viewModel.uiState.first { it.currentMonthCollectedPaise == 2500000L }
    assertEquals(2500000L, state.currentMonthCollectedPaise)
  }

  @Test
  fun testZeroMealsDivisionByZeroSafe() = runBlocking {
    // Add expenses but zero meals
    expenseRepository.addExpense(
      date = "2026-09-02",
      description = "Kitchen Utensils",
      category = ExpenseCategory.OTHER,
      amountPaise = 150000L
    )

    val state = viewModel.uiState.first { it.currentMonthTotalExpensePaise == 150000L }
    assertEquals(150000L, state.currentMonthTotalExpensePaise)
    assertEquals(0, state.currentMonthTotalMeals)
    assertEquals(0.0, state.currentMonthCostPerMealRupees, 0.0001)
  }
}
