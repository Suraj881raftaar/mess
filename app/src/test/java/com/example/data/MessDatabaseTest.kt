package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.EmployeeDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MealAttendanceDao
import com.example.data.dao.MenuDao
import com.example.data.database.MessDatabase
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.entity.Menu
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MenuRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MessDatabaseTest {

  private lateinit var db: MessDatabase
  private lateinit var employeeDao: EmployeeDao
  private lateinit var attendanceDao: MealAttendanceDao
  private lateinit var menuDao: MenuDao
  private lateinit var expenseDao: ExpenseDao

  private lateinit var employeeRepo: EmployeeRepository
  private lateinit var attendanceRepo: AttendanceRepository
  private lateinit var menuRepo: MenuRepository
  private lateinit var expenseRepo: ExpenseRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    employeeDao = db.employeeDao()
    attendanceDao = db.mealAttendanceDao()
    menuDao = db.menuDao()
    expenseDao = db.expenseDao()

    employeeRepo = EmployeeRepository(employeeDao)
    attendanceRepo = AttendanceRepository(attendanceDao)
    menuRepo = MenuRepository(menuDao)
    expenseRepo = ExpenseRepository(expenseDao)
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun testEmployeeInsertAndUniqueCodeConstraint() = runBlocking {
    val id1 = employeeDao.insert(
      Employee(
        employeeCode = "EMP001",
        name = "Rahul Sharma",
        department = "IT",
        phone = "9876543210"
      )
    )
    assertTrue(id1 > 0)

    val emp = employeeDao.getEmployeeByIdOnce(id1)
    assertNotNull(emp)
    assertEquals("Rahul Sharma", emp?.name)
    assertEquals(true, emp?.isActive)

    // Attempting to insert duplicate employeeCode must throw SQLiteConstraintException
    try {
      employeeDao.insert(
        Employee(
          employeeCode = "EMP001",
          name = "Duplicate Rahul",
          department = "HR"
        )
      )
      fail("Expected SQLiteConstraintException for duplicate employeeCode")
    } catch (e: SQLiteConstraintException) {
      // Expected
    }
  }

  @Test
  fun testEmployeeDeactivationPreservesRecordAndHistoricalAttendance() = runBlocking {
    val empId = employeeDao.insert(
      Employee(
        employeeCode = "EMP002",
        name = "Amit Patil",
        department = "Accounts"
      )
    )

    // Record attendance on 2026-09-01
    attendanceRepo.setAttendance(empId, "2026-09-01", MealType.LUNCH, true)

    val activeBefore = employeeDao.getActiveEmployees().first()
    assertEquals(1, activeBefore.size)

    // Deactivate employee
    employeeRepo.deactivateEmployee(empId)

    val activeAfter = employeeDao.getActiveEmployees().first()
    assertEquals(0, activeAfter.size)

    val allAfter = employeeDao.getAllEmployees().first()
    assertEquals(1, allAfter.size)
    assertFalse(allAfter[0].isActive)

    // Attendance is still intact
    val records = attendanceDao.getAllAttendanceForEmployee(empId).first()
    assertEquals(1, records.size)
    assertTrue(records[0].present)
  }

  @Test
  fun testMealAttendanceUniqueConstraintAndUpsert() = runBlocking {
    val empId = employeeDao.insert(
      Employee(
        employeeCode = "EMP003",
        name = "Priya Joshi",
        department = "HR"
      )
    )

    // Insert attendance
    attendanceDao.insertStrict(
      MealAttendance(
        employeeId = empId,
        date = "2026-09-05",
        mealType = MealType.BREAKFAST,
        present = true
      )
    )

    // Inserting strict duplicate (same employee + date + mealType) must fail
    try {
      attendanceDao.insertStrict(
        MealAttendance(
          employeeId = empId,
          date = "2026-09-05",
          mealType = MealType.BREAKFAST,
          present = false
        )
      )
      fail("Expected SQLiteConstraintException on duplicate attendance")
    } catch (e: SQLiteConstraintException) {
      // Expected
    }

    // Changing status via repository (upsert) should update cleanly without duplicating
    attendanceRepo.setAttendance(empId, "2026-09-05", MealType.BREAKFAST, false)
    val list = attendanceDao.getAttendanceForDateAndMeal("2026-09-05", MealType.BREAKFAST).first()
    assertEquals(1, list.size)
    assertFalse(list[0].present)
  }

  @Test
  fun testMealAttendanceForeignKeyConstraint() = runBlocking {
    // Attempting to record attendance for non-existent employee ID
    try {
      attendanceDao.insertStrict(
        MealAttendance(
          employeeId = 9999L,
          date = "2026-09-05",
          mealType = MealType.LUNCH,
          present = true
        )
      )
      fail("Expected SQLiteConstraintException due to foreign key violation")
    } catch (e: SQLiteConstraintException) {
      // Expected
    }
  }

  @Test
  fun testMenuUniqueConstraintAndCopy() = runBlocking {
    menuDao.insertStrict(
      Menu(
        date = "2026-09-01",
        mealType = MealType.BREAKFAST,
        description = "Poha + Tea"
      )
    )
    menuDao.insertStrict(
      Menu(
        date = "2026-09-01",
        mealType = MealType.LUNCH,
        description = "Dal + Rice + Sabzi + Roti"
      )
    )

    // Duplicate date + mealType must fail strict insert
    try {
      menuDao.insertStrict(
        Menu(
          date = "2026-09-01",
          mealType = MealType.BREAKFAST,
          description = "Idli + Sambar"
        )
      )
      fail("Expected SQLiteConstraintException on duplicate date + mealType")
    } catch (e: SQLiteConstraintException) {
      // Expected
    }

    // Copy menu from 2026-09-01 to 2026-09-02
    val result = menuRepo.copyMenu("2026-09-01", "2026-09-02")
    assertTrue(result.isSuccess)
    assertEquals(2, result.getOrNull())

    val targetMenus = menuDao.getMenuForDate("2026-09-02").first()
    assertEquals(2, targetMenus.size)
    val breakfast = targetMenus.find { it.mealType == MealType.BREAKFAST }
    assertEquals("Poha + Tea", breakfast?.description)
  }

  @Test
  fun testExpenseStorageAndSumInPaise() = runBlocking {
    // Add expenses: ₹150.50 (15050 paise) and ₹249.50 (24950 paise)
    val id1 = expenseRepo.addExpense(
      date = "2026-09-02",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountPaise = 15050L,
      vendor = "Local Mandi"
    )
    assertTrue(id1.isSuccess)

    val id2 = expenseRepo.addExpense(
      date = "2026-09-03",
      description = "Milk & Curd",
      category = ExpenseCategory.DAIRY,
      amountPaise = 24950L,
      vendor = "Amul"
    )
    assertTrue(id2.isSuccess)

    // Sum should be exactly 40000 paise (₹400.00) without float rounding errors
    val totalPaise = expenseDao.getTotalExpensesPaiseForMonthOnce("2026-09")
    assertEquals(40000L, totalPaise)
    assertEquals("₹400.00", CurrencyUtils.formatPaise(totalPaise))

    // Validation: 0 or negative paise must fail
    val invalid = expenseRepo.addExpense(
      date = "2026-09-04",
      description = "Invalid",
      category = ExpenseCategory.OTHER,
      amountPaise = 0L
    )
    assertTrue(invalid.isFailure)
  }

  @Test
  fun testMasterSpecBillingCalculations() = runBlocking {
    // Spec Example:
    // Total expenses = ₹42,000 (4,200,000 paise)
    // Total meals = 2,100
    // Cost per meal = ₹20.00
    // Employee meals = 42
    // Payable = ₹840.00

    val totalExpensePaise = 4200000L
    val totalMeals = 2100

    val costPerMeal = CurrencyUtils.calculateCostPerMealRupees(totalExpensePaise, totalMeals)
    assertEquals(20.0, costPerMeal, 0.0001)

    val payable = CurrencyUtils.calculateEmployeePayable(42, costPerMeal)
    assertEquals(840.0, payable, 0.0001)
    assertEquals("₹840.00", CurrencyUtils.formatRupees(payable))

    // Edge case: 0 total meals does not divide by zero
    val zeroCost = CurrencyUtils.calculateCostPerMealRupees(totalExpensePaise, 0)
    assertEquals(0.0, zeroCost, 0.0001)
  }
}
