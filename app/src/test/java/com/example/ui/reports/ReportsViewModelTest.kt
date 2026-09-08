package com.example.ui.reports

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.PaymentRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ReportsViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: MessDatabase
  private lateinit var employeeRepository: EmployeeRepository
  private lateinit var attendanceRepository: AttendanceRepository
  private lateinit var expenseRepository: ExpenseRepository
  private lateinit var paymentRepository: PaymentRepository
  private lateinit var viewModel: ReportsViewModel

  private val testMonth = "2026-09"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    employeeRepository = EmployeeRepository(db.employeeDao())
    attendanceRepository = AttendanceRepository(db.mealAttendanceDao())
    expenseRepository = ExpenseRepository(db.expenseDao())
    paymentRepository = PaymentRepository(db.paymentDao())

    viewModel = ReportsViewModel(
      employeeRepository,
      attendanceRepository,
      expenseRepository,
      paymentRepository
    )
    viewModel.setMonth(testMonth)
  }

  @After
  fun teardown() {
    db.close()
    try {
      Dispatchers.resetMain()
    } catch (_: Throwable) {}
  }

  @Test
  fun testInitialEmptyReportState() = runBlocking {
    val state = viewModel.uiState.first { !it.isLoading }
    assertEquals(testMonth, state.selectedMonth)
    assertEquals(0L, state.totalExpensesPaise)
    assertEquals(0, state.totalMeals)
    assertEquals(0, state.breakfastCount)
    assertEquals(0, state.lunchCount)
    assertEquals(0, state.dinnerCount)
    assertEquals(0, state.activeEmployeeCount)
    assertEquals(0.0, state.costPerMealRupees, 0.0001)
    assertTrue(state.employeeBills.isEmpty())
  }

  @Test
  fun testMonthlyTotalsAndMealBreakdown() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()

    // Add attendance on 2026-09-01
    // Breakfast: 2 meals
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.BREAKFAST, true)
    attendanceRepository.setAttendance(emp2, "2026-09-01", MealType.BREAKFAST, true)

    // Lunch: 2 meals
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.LUNCH, true)
    attendanceRepository.setAttendance(emp2, "2026-09-01", MealType.LUNCH, true)

    // Dinner: 1 meal
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.DINNER, true)
    attendanceRepository.setAttendance(emp2, "2026-09-01", MealType.DINNER, false)

    // Add attendance on 2026-09-02
    // Lunch: 1 meal
    attendanceRepository.setAttendance(emp1, "2026-09-02", MealType.LUNCH, true)

    // Total: Breakfast = 2, Lunch = 3, Dinner = 1, Total = 6 meals
    val state = viewModel.uiState.first { it.totalMeals == 6 }
    assertEquals(2, state.breakfastCount)
    assertEquals(3, state.lunchCount)
    assertEquals(1, state.dinnerCount)
    assertEquals(6, state.totalMeals)
    assertEquals(2, state.activeEmployeeCount)
  }

  @Test
  fun testCostPerMealCalculation() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()

    // Record 10 meals
    for (i in 1..10) {
      attendanceRepository.setAttendance(emp1, "2026-09-0$i", MealType.LUNCH, true)
    }

    // Record expenses of ₹2,500.00 (250,000 paise)
    expenseRepository.addExpense(
      date = "2026-09-02",
      description = "Groceries",
      category = ExpenseCategory.GROCERIES,
      amountPaise = 250000L
    )

    val state = viewModel.uiState.first { it.totalMeals == 10 && it.totalExpensesPaise == 250000L }

    // Cost per meal = ₹2,500 / 10 = ₹250.00
    assertEquals(250.0, state.costPerMealRupees, 0.0001)
  }

  @Test
  fun testEmployeeMealTotals() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()

    // emp1: 2 breakfast, 1 lunch, 1 dinner = 4 meals
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.BREAKFAST, true)
    attendanceRepository.setAttendance(emp1, "2026-09-02", MealType.BREAKFAST, true)
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.LUNCH, true)
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.DINNER, true)

    // emp2: 1 lunch = 1 meal
    attendanceRepository.setAttendance(emp2, "2026-09-01", MealType.LUNCH, true)

    val state = viewModel.uiState.first { it.totalMeals == 5 }

    val billEmp1 = state.employeeBills.first { it.employee.id == emp1 }
    assertEquals(2, billEmp1.breakfastCount)
    assertEquals(1, billEmp1.lunchCount)
    assertEquals(1, billEmp1.dinnerCount)
    assertEquals(4, billEmp1.totalMeals)

    val billEmp2 = state.employeeBills.first { it.employee.id == emp2 }
    assertEquals(0, billEmp2.breakfastCount)
    assertEquals(1, billEmp2.lunchCount)
    assertEquals(0, billEmp2.dinnerCount)
    assertEquals(1, billEmp2.totalMeals)
  }

  @Test
  fun testEmployeeBillingCalculation() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()
    val emp3 = employeeRepository.addEmployee("EMP003", "Priya Joshi", "HR").getOrThrow()

    // emp1 takes 4 meals
    for (i in 1..4) {
      attendanceRepository.setAttendance(emp1, "2026-09-0$i", MealType.LUNCH, true)
    }

    // emp2 takes 1 meal
    attendanceRepository.setAttendance(emp2, "2026-09-01", MealType.LUNCH, true)

    // emp3 takes 0 meals

    // Total meals = 5. Add expenses = ₹100.00 (10,000 paise).
    // Cost per meal = ₹100 / 5 = ₹20.00
    expenseRepository.addExpense(
      date = "2026-09-01",
      description = "Daily Veggies",
      category = ExpenseCategory.VEGETABLES,
      amountPaise = 10000L
    )

    val state = viewModel.uiState.first { it.totalMeals == 5 && it.totalExpensesPaise == 10000L }
    assertEquals(20.0, state.costPerMealRupees, 0.0001)

    // emp1: 4 meals * ₹20 = ₹80.00 (8,000 paise)
    val bill1 = state.employeeBills.first { it.employee.id == emp1 }
    assertEquals(80.0, bill1.payableRupees, 0.01)
    assertEquals(8000L, bill1.payablePaise)

    // emp2: 1 meal * ₹20 = ₹20.00 (2,000 paise)
    val bill2 = state.employeeBills.first { it.employee.id == emp2 }
    assertEquals(20.0, bill2.payableRupees, 0.01)
    assertEquals(2000L, bill2.payablePaise)

    // emp3: 0 meals * ₹20 = ₹0.00 (0 paise)
    val bill3 = state.employeeBills.first { it.employee.id == emp3 }
    assertEquals(0.0, bill3.payableRupees, 0.01)
    assertEquals(0L, bill3.payablePaise)
  }

  @Test
  fun testZeroMealsCase() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()

    // Add expenses but zero meals
    expenseRepository.addExpense(
      date = "2026-09-01",
      description = "Monthly Gas Cylinder",
      category = ExpenseCategory.GAS_FUEL,
      amountPaise = 120000L
    )

    val state = viewModel.uiState.first { it.totalExpensesPaise == 120000L }
    assertEquals(0, state.totalMeals)
    assertEquals(0.0, state.costPerMealRupees, 0.0001)

    val bill = state.employeeBills.first { it.employee.id == emp1 }
    assertEquals(0, bill.totalMeals)
    assertEquals(0.0, bill.payableRupees, 0.0001)
    assertEquals(0L, bill.payablePaise)
  }

  @Test
  fun testHistoricalDeactivatedEmployeeBilling() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val emp2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()

    // Both take meals
    attendanceRepository.setAttendance(emp1, "2026-09-01", MealType.LUNCH, true)
    attendanceRepository.setAttendance(emp2, "2026-09-01", MealType.LUNCH, true)

    // Deactivate emp1
    employeeRepository.deactivateEmployee(emp1)

    // Expenses: ₹40.00 (4,000 paise), 2 meals -> ₹20/meal
    expenseRepository.addExpense(
      date = "2026-09-01",
      description = "Lunch essentials",
      category = ExpenseCategory.GROCERIES,
      amountPaise = 4000L
    )

    val state = viewModel.uiState.first { it.totalMeals == 2 && it.activeEmployeeCount == 1 }

    // Verify active count is 1
    assertEquals(1, state.activeEmployeeCount)

    // emp1 is deactivated, but appears in billing with 1 meal and ₹20.00 payable
    val deactivatedBill = state.employeeBills.first { it.employee.id == emp1 }
    assertEquals(false, deactivatedBill.employee.isActive)
    assertEquals(1, deactivatedBill.totalMeals)
    assertEquals(20.0, deactivatedBill.payableRupees, 0.01)
    assertEquals(2000L, deactivatedBill.payablePaise)
  }

  @Test
  fun testMonthNavigation() = runBlocking {
    viewModel.setMonth("2026-09")
    assertEquals("2026-09", viewModel.selectedMonth.value)

    viewModel.previousMonth()
    assertEquals("2026-08", viewModel.selectedMonth.value)

    viewModel.nextMonth()
    assertEquals("2026-09", viewModel.selectedMonth.value)
  }

  @Test
  fun testSearchFiltering() = runBlocking {
    employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT")
    employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts")

    val initial = viewModel.uiState.first { it.employeeBills.size == 2 }
    assertEquals(2, initial.filteredEmployeeBills.size)

    viewModel.setSearchQuery("Rahul")
    val filteredByName = viewModel.uiState.first { it.searchQuery == "Rahul" }
    assertEquals(1, filteredByName.filteredEmployeeBills.size)
    assertEquals("Rahul Sharma", filteredByName.filteredEmployeeBills.first().employee.name)

    viewModel.setSearchQuery("Accounts")
    val filteredByDept = viewModel.uiState.first { it.searchQuery == "Accounts" }
    assertEquals(1, filteredByDept.filteredEmployeeBills.size)
    assertEquals("Amit Patil", filteredByDept.filteredEmployeeBills.first().employee.name)

    viewModel.setSearchQuery("")
    val reset = viewModel.uiState.first { it.searchQuery == "" }
    assertEquals(2, reset.filteredEmployeeBills.size)
  }

  @Test
  fun testPaymentSettlementInReports() = runBlocking {
    val emp1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()

    // 10 meals
    for (i in 1..10) {
      attendanceRepository.setAttendance(emp1, "2026-09-0$i", MealType.LUNCH, true)
    }

    // Expense ₹1000.00 -> 100,000 paise -> ₹100/meal -> Rahul owes ₹1000.00
    expenseRepository.addExpense(
      date = "2026-09-01",
      description = "Monthly Ration",
      category = ExpenseCategory.GROCERIES,
      amountPaise = 100000L
    )

    // Rahul pays ₹600.00 (60,000 paise)
    paymentRepository.recordPayment(
      employeeId = emp1,
      month = testMonth,
      amountPaise = 60000L,
      paymentDate = "2026-09-08",
      paymentMethod = "UPI"
    )

    val state = viewModel.uiState.first { it.totalExpensesPaise == 100000L && it.totalCollectedPaise == 60000L }
    assertEquals(60000L, state.totalCollectedPaise)
    assertEquals(40000L, state.totalPendingPaise)

    val bill = state.employeeBills.first { it.employee.id == emp1 }
    assertEquals(100000L, bill.payablePaise)
    assertEquals(60000L, bill.paidPaise)
    assertEquals(40000L, bill.pendingPaise)
    assertEquals(PaymentStatus.PARTIAL, bill.paymentStatus)
  }
}
