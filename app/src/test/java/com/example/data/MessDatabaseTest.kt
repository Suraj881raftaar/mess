package com.example.data

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.EmployeeDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MealAttendanceDao
import com.example.data.dao.MealFeedbackDao
import com.example.data.dao.MenuDao
import com.example.data.dao.MenuTemplateDao
import com.example.data.dao.MessSettingDao
import com.example.data.dao.PantryDao
import com.example.data.dao.PaymentDao
import com.example.data.database.MessDatabase
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.entity.MealFeedback
import com.example.data.entity.Menu
import com.example.data.entity.MenuTemplate
import com.example.data.entity.PantryItem
import com.example.data.entity.PantryUnit
import com.example.data.entity.Payment
import com.example.data.model.DietaryPreference
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.MealFeedbackRepository
import com.example.data.repository.MenuRepository
import com.example.data.repository.PantryRepository
import com.example.data.repository.PaymentRepository
import com.example.data.repository.SettingsRepository
import com.example.util.CurrencyUtils
import com.example.util.RecipeCalculator
import com.example.util.UpiUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
  private lateinit var paymentDao: PaymentDao
  private lateinit var menuTemplateDao: MenuTemplateDao
  private lateinit var messSettingDao: MessSettingDao

  private lateinit var employeeRepo: EmployeeRepository
  private lateinit var attendanceRepo: AttendanceRepository
  private lateinit var menuRepo: MenuRepository
  private lateinit var expenseRepo: ExpenseRepository
  private lateinit var paymentRepo: PaymentRepository
  private lateinit var settingsRepo: SettingsRepository

  private lateinit var pantryDao: PantryDao
  private lateinit var mealFeedbackDao: MealFeedbackDao
  private lateinit var pantryRepo: PantryRepository
  private lateinit var feedbackRepo: MealFeedbackRepository

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    employeeDao = db.employeeDao()
    attendanceDao = db.mealAttendanceDao()
    menuDao = db.menuDao()
    expenseDao = db.expenseDao()
    paymentDao = db.paymentDao()
    menuTemplateDao = db.menuTemplateDao()
    messSettingDao = db.messSettingDao()
    pantryDao = db.pantryDao()
    mealFeedbackDao = db.mealFeedbackDao()

    employeeRepo = EmployeeRepository(employeeDao)
    attendanceRepo = AttendanceRepository(attendanceDao)
    menuRepo = MenuRepository(menuDao, menuTemplateDao)
    expenseRepo = ExpenseRepository(expenseDao)
    paymentRepo = PaymentRepository(paymentDao)
    settingsRepo = SettingsRepository(messSettingDao)
    pantryRepo = PantryRepository(pantryDao, expenseDao)
    feedbackRepo = MealFeedbackRepository(mealFeedbackDao)
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
  fun testMenuTemplates() = runBlocking {
    menuRepo.setMenu("2026-09-01", MealType.BREAKFAST, "Idli & Vada")
    menuRepo.setMenu("2026-09-01", MealType.LUNCH, "South Indian Thali")
    menuRepo.setMenu("2026-09-01", MealType.DINNER, "Curd Rice")

    val saveRes = menuRepo.saveDayAsTemplate("South Special", "2026-09-01")
    assertTrue(saveRes.isSuccess)

    val templates = menuRepo.allTemplates.first()
    assertEquals(1, templates.size)
    assertEquals("South Special", templates[0].templateName)

    // Apply to new date 2026-09-10
    val applyRes = menuRepo.applyTemplateToDate(templates[0].id, "2026-09-10")
    assertTrue(applyRes.isSuccess)

    val applied = menuRepo.getMenuForDate("2026-09-10").first()
    assertEquals(3, applied.size)
    assertEquals("Idli & Vada", applied.find { it.mealType == MealType.BREAKFAST }?.description)
  }

  @Test
  fun testPaymentTracking() = runBlocking {
    val empId = employeeDao.insert(
      Employee(
        employeeCode = "EMP100",
        name = "Vikram Singh",
        department = "Operations"
      )
    )

    // Record Payment
    val payRes = paymentRepo.recordPayment(
      employeeId = empId,
      month = "2026-09",
      amountPaise = 84000L, // ₹840.00
      paymentDate = "2026-09-08",
      paymentMethod = "UPI",
      notes = "GPay payment received"
    )
    assertTrue(payRes.isSuccess)

    val payments = paymentRepo.getPaymentsForMonth("2026-09").first()
    assertEquals(1, payments.size)
    assertEquals(84000L, payments[0].amountPaise)
    assertEquals("UPI", payments[0].paymentMethod)

    val totalPaid = paymentRepo.getTotalPaidForMonthOnce("2026-09")
    assertEquals(84000L, totalPaid)
  }

  @Test
  fun testSettingsPersistence() = runBlocking {
    settingsRepo.setMessName("Alpha Tech Mess")
    val name = settingsRepo.getMessNameOnce()
    assertEquals("Alpha Tech Mess", name)

    settingsRepo.setRemindAttendance(true)
    assertTrue(settingsRepo.getRemindAttendance().first())

    settingsRepo.setThemeMode("DARK")
    assertEquals("DARK", settingsRepo.getThemeMode().first())
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

  @Test
  fun testDietaryPreferencePersistence() = runBlocking {
    val id = employeeDao.insert(
      Employee(
        employeeCode = "EMP_VEG",
        name = "Kavita Patel",
        department = "Design",
        dietaryPreference = DietaryPreference.JAIN
      )
    )
    val fetched = employeeDao.getEmployeeByIdOnce(id)
    assertNotNull(fetched)
    assertEquals(DietaryPreference.JAIN, fetched!!.dietaryPreference)
    assertEquals("Jain", fetched.dietaryPreference.displayName)
    assertEquals("🌿 Jain", fetched.dietaryPreference.label)
  }

  @Test
  fun testPantryOperationsAndLowStock() = runBlocking {
    // Add Rice (50 kg, min 10 kg)
    val riceResult = pantryRepo.addPantryItem(
      itemName = "Basmati Rice",
      category = ExpenseCategory.GROCERIES,
      currentQuantity = 50.0,
      minThreshold = 10.0,
      unit = PantryUnit.KG,
      estimatedPricePaise = 6000L
    )
    assertTrue(riceResult.isSuccess)
    val riceId = riceResult.getOrThrow()

    // Add Mustard Oil (2 Litres, min 5 Litres -> Low Stock!)
    val oilResult = pantryRepo.addPantryItem(
      itemName = "Mustard Oil",
      category = ExpenseCategory.GROCERIES,
      currentQuantity = 2.0,
      minThreshold = 5.0,
      unit = PantryUnit.LITRE,
      estimatedPricePaise = 15000L,
      isNeededOnShoppingList = true
    )
    assertTrue(oilResult.isSuccess)

    // Verify low stock query
    val lowStockCount = pantryRepo.lowStockCount.first()
    assertEquals(1, lowStockCount)

    // Verify shopping list generated items
    val shoppingList = pantryRepo.shoppingList.first()
    assertEquals(1, shoppingList.size)
    assertEquals("Mustard Oil", shoppingList[0].itemName)

    // Update rice quantity to 5 kg (< 10.0 minimum)
    val reduceRes = pantryRepo.updateStock(riceId, 5.0)
    assertTrue(reduceRes.isSuccess)

    val updatedLowStockCount = pantryRepo.lowStockCount.first()
    assertEquals(2, updatedLowStockCount)
  }

  @Test
  fun testPantryRestockAutoExpenseCreation() = runBlocking {
    val itemRes = pantryRepo.addPantryItem(
      itemName = "Toor Dal",
      category = ExpenseCategory.GROCERIES,
      currentQuantity = 5.0,
      minThreshold = 10.0,
      unit = PantryUnit.KG,
      estimatedPricePaise = 12000L // ₹120/kg
    )
    assertTrue(itemRes.isSuccess)
    val itemId = itemRes.getOrThrow()

    // Restock 15 kg with expense creation at ₹120/kg = ₹1,800.00 (180,000 paise)
    val purchaseRes = pantryRepo.markItemPurchased(
      itemId = itemId,
      purchasedQuantity = 15.0,
      actualCostPaise = 180000L,
      vendor = "Wholesale Grocery Market",
      purchaseDate = "2026-09-08"
    )
    assertTrue(purchaseRes.isSuccess)

    // Check item quantity is now 20 kg
    val item = pantryRepo.allPantryItems.first().find { it.id == itemId }
    assertNotNull(item)
    assertEquals(20.0, item!!.currentQuantity, 0.001)

    // Check that an expense entry was automatically created in expense table
    val expenses = expenseDao.getExpensesForMonth("2026-09").first()
    val restockExpense = expenses.find { it.description.contains("Toor Dal") }
    assertNotNull(restockExpense)
    assertEquals(180000L, restockExpense!!.amountPaise)
    assertEquals("Wholesale Grocery Market", restockExpense.vendor)
  }

  @Test
  fun testMealFeedbackOperations() = runBlocking {
    val empId = employeeDao.insert(
      Employee(employeeCode = "EMP_FD1", name = "Aman Gupta", department = "Sales")
    )

    // Submit ratings
    val f1 = feedbackRepo.submitFeedback(
      date = "2026-09-08",
      mealType = MealType.LUNCH,
      employeeId = empId,
      employeeName = "Aman Gupta",
      rating = 5,
      comment = "Excellent Paneer Butter Masala!"
    )
    assertTrue(f1.isSuccess)

    val empId2 = employeeDao.insert(
      Employee(employeeCode = "EMP_FD2", name = "Priya Singh", department = "Design")
    )
    val f2 = feedbackRepo.submitFeedback(
      date = "2026-09-08",
      mealType = MealType.LUNCH,
      employeeId = empId2,
      employeeName = "Priya Singh",
      rating = 3,
      comment = "Rotis were slightly cold."
    )
    assertTrue(f2.isSuccess)

    // Check stats
    val avg = feedbackRepo.getAverageRatingForDateAndMeal("2026-09-08", MealType.LUNCH).first()
    assertEquals(4.0, avg ?: 0.0, 0.001)

    val list = feedbackRepo.getFeedbacksForDateAndMeal("2026-09-08", MealType.LUNCH).first()
    assertEquals(2, list.size)
  }

  @Test
  fun testRecipeCalculator() {
    val paneerDish = RecipeCalculator.dishes.find { it.id == "paneer_butter_masala" }
    assertNotNull(paneerDish)
    val scaled50 = RecipeCalculator.calculateIngredients(paneerDish!!, headcount = 50)
    assertEquals(6, scaled50.size)

    val paneer = scaled50.find { it.first.name == "Fresh Paneer" }
    assertNotNull(paneer)
    // 50 people * 0.12 kg = 6.0 kg
    assertEquals(6.0, paneer!!.second, 0.001)
    assertEquals("kg", paneer.first.unit)

    val biryaniDish = RecipeCalculator.dishes.find { it.id == "veg_biryani" }
    assertNotNull(biryaniDish)
    val scaled100 = RecipeCalculator.calculateIngredients(biryaniDish!!, headcount = 100)
    val rice = scaled100.find { it.first.name == "Basmati Rice" }
    assertNotNull(rice)
    // 100 people * 0.10 kg = 10.0 kg
    assertEquals(10.0, rice!!.second, 0.001)
  }

  @Test
  fun testUpiUtilsUriGeneration() {
    val uri = UpiUtils.generateUpiUri(
      upiId = "officemess@upi",
      payeeName = "Office Mess",
      amountRupees = 840.50,
      note = "Mess Bill Sep 2026"
    )
    assertTrue(uri.startsWith("upi://pay?"))
    assertTrue(uri.contains("pa=officemess@upi") || uri.contains("pa=officemess%40upi"))
    assertTrue(uri.contains("am=840.50"))
  }
}
