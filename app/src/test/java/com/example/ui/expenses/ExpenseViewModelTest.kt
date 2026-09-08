package com.example.ui.expenses

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.model.ExpenseCategory
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
class ExpenseViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: MessDatabase
  private lateinit var expenseRepository: ExpenseRepository
  private lateinit var viewModel: ExpenseViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    expenseRepository = ExpenseRepository(db.expenseDao())
    viewModel = ExpenseViewModel(expenseRepository)
  }

  @After
  fun teardown() {
    db.close()
    Dispatchers.resetMain()
  }

  @Test
  fun testParseRupeesToPaise() {
    assertEquals(10000L, ExpenseViewModel.parseRupeesToPaise("100"))
    assertEquals(2050L, ExpenseViewModel.parseRupeesToPaise("20.50"))
    assertEquals(50L, ExpenseViewModel.parseRupeesToPaise("0.50"))
    assertEquals(123456L, ExpenseViewModel.parseRupeesToPaise("1234.56"))
    assertNull(ExpenseViewModel.parseRupeesToPaise("0"))
    assertNull(ExpenseViewModel.parseRupeesToPaise("-10"))
    assertNull(ExpenseViewModel.parseRupeesToPaise(""))
    assertNull(ExpenseViewModel.parseRupeesToPaise("abc"))
  }

  @Test
  fun testAddExpenseSuccess() = runBlocking {
    viewModel.setMonth("2026-09")

    val result = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "Basmati Rice 25kg",
      category = ExpenseCategory.GROCERIES,
      amountRupeesStr = "2150.00",
      quantityStr = "25",
      unit = "kg",
      vendor = "Local Grocery Store",
      notes = "Monthly ration stock"
    )

    assertTrue(result.isSuccess)
    val id = result.getOrThrow()
    assertTrue(id > 0)

    val state = viewModel.uiState.first { it.expenses.isNotEmpty() }
    assertEquals(1, state.expenses.size)
    val expense = state.expenses[0]
    assertEquals("Basmati Rice 25kg", expense.description)
    assertEquals(ExpenseCategory.GROCERIES, expense.category)
    assertEquals(215000L, expense.amountPaise)
    assertEquals(215000L, state.monthlyTotalPaise)
    assertEquals(215000L, state.filteredTotalPaise)
    assertEquals("Local Grocery Store", expense.vendor)
  }

  @Test
  fun testAddExpenseValidationRejections() = runBlocking {
    viewModel.setMonth("2026-09")

    // Blank description
    val res1 = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "   ",
      category = ExpenseCategory.GROCERIES,
      amountRupeesStr = "100"
    )
    assertTrue(res1.isFailure)

    // Blank date
    val res2 = viewModel.saveExpenseSync(
      id = 0L,
      date = "",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "100"
    )
    assertTrue(res2.isFailure)

    // Invalid date format
    val res3 = viewModel.saveExpenseSync(
      id = 0L,
      date = "invalid-date",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "100"
    )
    assertTrue(res3.isFailure)

    // Zero or negative amount
    val res4 = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "0"
    )
    assertTrue(res4.isFailure)

    val res5 = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "-50"
    )
    assertTrue(res5.isFailure)

    // Invalid quantity
    val res6 = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "50",
      quantityStr = "-2"
    )
    assertTrue(res6.isFailure)
  }

  @Test
  fun testEditExpense() = runBlocking {
    viewModel.setMonth("2026-09")

    val insertResult = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "Milk 10 Litres",
      category = ExpenseCategory.DAIRY,
      amountRupeesStr = "600",
      quantityStr = "10",
      unit = "L"
    )
    val id = insertResult.getOrThrow()

    // Now edit the expense: increase amount to 650 and add notes
    val editResult = viewModel.saveExpenseSync(
      id = id,
      date = "2026-09-08",
      description = "Milk 10 Litres + Paneer",
      category = ExpenseCategory.DAIRY,
      amountRupeesStr = "850.50",
      quantityStr = "10",
      unit = "L",
      notes = "Added 500g paneer"
    )
    assertTrue(editResult.isSuccess)

    val state = viewModel.uiState.first { it.expenses.any { exp -> exp.id == id && exp.amountPaise == 85050L } }
    val edited = state.expenses.first { it.id == id }
    assertEquals("Milk 10 Litres + Paneer", edited.description)
    assertEquals(85050L, edited.amountPaise)
    assertEquals("Added 500g paneer", edited.notes)
    assertEquals(85050L, state.monthlyTotalPaise)
  }

  @Test
  fun testDeleteExpense() = runBlocking {
    viewModel.setMonth("2026-09")

    val id1 = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-08",
      description = "LPG Cylinder Refill",
      category = ExpenseCategory.GAS_FUEL,
      amountRupeesStr = "1150"
    ).getOrThrow()

    val id2 = viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-09",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "450"
    ).getOrThrow()

    val initial = viewModel.uiState.first { it.expenses.size == 2 && it.monthlyTotalPaise == 160000L }
    assertEquals(160000L, initial.monthlyTotalPaise)

    val expenseToDelete = initial.expenses.first { it.id == id1 }
    viewModel.deleteExpenseSync(expenseToDelete)

    val afterDelete = viewModel.uiState.first { it.expenses.size == 1 && it.monthlyTotalPaise == 45000L }
    assertEquals(1, afterDelete.expenses.size)
    assertEquals(id2, afterDelete.expenses[0].id)
    assertEquals(45000L, afterDelete.monthlyTotalPaise)
  }

  @Test
  fun testMonthFiltering() = runBlocking {
    // Add Sept expense
    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-10",
      description = "Sept Groceries",
      category = ExpenseCategory.GROCERIES,
      amountRupeesStr = "3000"
    )

    // Add Oct expense
    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-10-02",
      description = "Oct Groceries",
      category = ExpenseCategory.GROCERIES,
      amountRupeesStr = "4500"
    )

    // Verify Sept view
    viewModel.setMonth("2026-09")
    val septState = viewModel.uiState.first { it.selectedMonth == "2026-09" && it.expenses.isNotEmpty() }
    assertEquals(1, septState.expenses.size)
    assertEquals("Sept Groceries", septState.expenses[0].description)
    assertEquals(300000L, septState.monthlyTotalPaise)

    // Verify Oct view
    viewModel.setMonth("2026-10")
    val octState = viewModel.uiState.first { it.selectedMonth == "2026-10" && it.expenses.isNotEmpty() }
    assertEquals(1, octState.expenses.size)
    assertEquals("Oct Groceries", octState.expenses[0].description)
    assertEquals(450000L, octState.monthlyTotalPaise)
  }

  @Test
  fun testCategoryFiltering() = runBlocking {
    viewModel.setMonth("2026-09")

    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-01",
      description = "Vegetables",
      category = ExpenseCategory.VEGETABLES,
      amountRupeesStr = "500"
    )

    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-02",
      description = "Cook Salary",
      category = ExpenseCategory.SALARY_LABOUR,
      amountRupeesStr = "8000"
    )

    // Unfiltered view
    viewModel.setCategory(null)
    val allState = viewModel.uiState.first { it.expenses.size == 2 }
    assertEquals(2, allState.expenses.size)
    assertEquals(850000L, allState.monthlyTotalPaise)
    assertEquals(850000L, allState.filteredTotalPaise)

    // Filter by VEGETABLES
    viewModel.setCategory(ExpenseCategory.VEGETABLES)
    val vegState = viewModel.uiState.first { it.selectedCategory == ExpenseCategory.VEGETABLES && it.expenses.size == 1 }
    assertEquals(1, vegState.expenses.size)
    assertEquals("Vegetables", vegState.expenses[0].description)
    assertEquals(850000L, vegState.monthlyTotalPaise) // Total monthly expenses remain ₹8,500
    assertEquals(50000L, vegState.filteredTotalPaise) // Filtered total is ₹500

    // Filter by DAIRY (empty)
    viewModel.setCategory(ExpenseCategory.DAIRY)
    val dairyState = viewModel.uiState.first { it.selectedCategory == ExpenseCategory.DAIRY }
    assertEquals(0, dairyState.expenses.size)
    assertEquals(850000L, dairyState.monthlyTotalPaise)
    assertEquals(0L, dairyState.filteredTotalPaise)
  }

  @Test
  fun testCategorySummaries() = runBlocking {
    viewModel.setMonth("2026-09")

    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-01",
      description = "Rice",
      category = ExpenseCategory.GROCERIES,
      amountRupeesStr = "1200"
    )
    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-05",
      description = "Oil & Spices",
      category = ExpenseCategory.GROCERIES,
      amountRupeesStr = "800"
    )
    viewModel.saveExpenseSync(
      id = 0L,
      date = "2026-09-03",
      description = "Electricity Bill",
      category = ExpenseCategory.UTILITIES,
      amountRupeesStr = "1500"
    )

    val state = viewModel.uiState.first { it.categorySummaries.isNotEmpty() }
    assertEquals(350000L, state.monthlyTotalPaise)

    val groceriesSummary = state.categorySummaries.firstOrNull { it.category == ExpenseCategory.GROCERIES }
    assertNotNull(groceriesSummary)
    assertEquals(200000L, groceriesSummary?.totalPaise)
    assertEquals(2, groceriesSummary?.expenseCount)

    val utilSummary = state.categorySummaries.firstOrNull { it.category == ExpenseCategory.UTILITIES }
    assertNotNull(utilSummary)
    assertEquals(150000L, utilSummary?.totalPaise)
    assertEquals(1, utilSummary?.expenseCount)
  }

  @Test
  fun testMonthNavigation() {
    viewModel.setMonth("2026-05")
    assertEquals("2026-05", viewModel.selectedMonth.value)

    viewModel.selectPreviousMonth()
    assertEquals("2026-04", viewModel.selectedMonth.value)

    viewModel.selectNextMonth()
    assertEquals("2026-05", viewModel.selectedMonth.value)

    viewModel.selectNextMonth()
    assertEquals("2026-06", viewModel.selectedMonth.value)

    viewModel.selectCurrentMonth()
    assertEquals(ExpenseViewModel.getCurrentMonthString(), viewModel.selectedMonth.value)
  }
}
