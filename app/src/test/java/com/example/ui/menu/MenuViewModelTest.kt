package com.example.ui.menu

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.model.MealType
import com.example.data.repository.MenuRepository
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
class MenuViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: MessDatabase
  private lateinit var menuRepository: MenuRepository
  private lateinit var viewModel: MenuViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    menuRepository = MenuRepository(db.menuDao(), db.menuTemplateDao())
    viewModel = MenuViewModel(menuRepository)
  }

  @After
  fun teardown() {
    db.close()
    Dispatchers.resetMain()
  }

  @Test
  fun testCreateBreakfastMenu() = runBlocking {
    viewModel.setDate("2026-09-08")
    val success = viewModel.saveMealMenuSync("2026-09-08", MealType.BREAKFAST, "Poha + Masala Tea")
    assertTrue(success)

    val state = viewModel.dailyMenuState.first { !it.isLoading }
    assertNotNull(state.breakfast)
    assertEquals("Poha + Masala Tea", state.breakfast?.description)
    assertNull(state.lunch)
    assertNull(state.dinner)
  }

  @Test
  fun testCreateLunchMenu() = runBlocking {
    viewModel.setDate("2026-09-08")
    val success = viewModel.saveMealMenuSync("2026-09-08", MealType.LUNCH, "Dal Tadka + Jeera Rice + Roti + Mixed Veg")
    assertTrue(success)

    val state = viewModel.dailyMenuState.first { !it.isLoading }
    assertNotNull(state.lunch)
    assertEquals("Dal Tadka + Jeera Rice + Roti + Mixed Veg", state.lunch?.description)
  }

  @Test
  fun testCreateDinnerMenu() = runBlocking {
    viewModel.setDate("2026-09-08")
    val success = viewModel.saveMealMenuSync("2026-09-08", MealType.DINNER, "Paneer Butter Masala + Phulka + Green Salad")
    assertTrue(success)

    val state = viewModel.dailyMenuState.first { !it.isLoading }
    assertNotNull(state.dinner)
    assertEquals("Paneer Butter Masala + Phulka + Green Salad", state.dinner?.description)
  }

  @Test
  fun testEditExistingMenu() = runBlocking {
    viewModel.setDate("2026-09-08")
    // Initial creation
    viewModel.saveMealMenuSync("2026-09-08", MealType.LUNCH, "Dal + Rice")
    val initial = viewModel.dailyMenuState.first { !it.isLoading }
    assertEquals("Dal + Rice", initial.lunch?.description)

    // Edit to new value
    val success = viewModel.saveMealMenuSync("2026-09-08", MealType.LUNCH, "Dal Makhani + Jeera Rice + 3 Rotis")
    assertTrue(success)

    val updated = viewModel.dailyMenuState.first { it.lunch?.description == "Dal Makhani + Jeera Rice + 3 Rotis" }
    assertEquals("Dal Makhani + Jeera Rice + 3 Rotis", updated.lunch?.description)

    // Verify Room database count for this date is exactly 1
    val dbList = menuRepository.getMenuForDate("2026-09-08").first()
    assertEquals(1, dbList.size)
  }

  @Test
  fun testSameDateSameMealDoesNotCreateDuplicates() = runBlocking {
    val date = "2026-09-08"
    // Repeated saves on the same composite key
    viewModel.saveMealMenuSync(date, MealType.BREAKFAST, "Idli Sambar")
    viewModel.saveMealMenuSync(date, MealType.BREAKFAST, "Idli Vada Sambar")
    viewModel.saveMealMenuSync(date, MealType.BREAKFAST, "Idli Vada Sambar + Chutney")

    val records = menuRepository.getMenuForDate(date).first()
    assertEquals(1, records.size)
    assertEquals("Idli Vada Sambar + Chutney", records[0].description)
  }

  @Test
  fun testDifferentMealsOnSameDateRemainIndependent() = runBlocking {
    val date = "2026-09-08"
    viewModel.setDate(date)

    viewModel.saveMealMenuSync(date, MealType.BREAKFAST, "Aloo Paratha + Curd")
    viewModel.saveMealMenuSync(date, MealType.LUNCH, "Rajma Chawal + Papad")
    viewModel.saveMealMenuSync(date, MealType.DINNER, "Sev Tamatar + Bhakri")

    val state = viewModel.dailyMenuState.first { !it.isLoading }
    assertEquals("Aloo Paratha + Curd", state.breakfast?.description)
    assertEquals("Rajma Chawal + Papad", state.lunch?.description)
    assertEquals("Sev Tamatar + Bhakri", state.dinner?.description)

    val allForDate = menuRepository.getMenuForDate(date).first()
    assertEquals(3, allForDate.size)
  }

  @Test
  fun testDifferentDatesRemainIndependent() = runBlocking {
    val day1 = "2026-09-08"
    val day2 = "2026-09-09"

    viewModel.saveMealMenuSync(day1, MealType.LUNCH, "Chole Bhature")
    viewModel.saveMealMenuSync(day2, MealType.LUNCH, "Kadhi Pakoda + Rice")

    viewModel.setDate(day1)
    val state1 = viewModel.dailyMenuState.first { !it.isLoading && it.date == day1 }
    assertEquals("Chole Bhature", state1.lunch?.description)

    viewModel.setDate(day2)
    val state2 = viewModel.dailyMenuState.first { !it.isLoading && it.date == day2 }
    assertEquals("Kadhi Pakoda + Rice", state2.lunch?.description)
  }

  @Test
  fun testWeeklyMenuQueryReturnsCorrectDateRange() = runBlocking {
    // Week starting Monday 2026-09-07 to Sunday 2026-09-13
    val monday = "2026-09-07"
    viewModel.selectCurrentWeek()

    // Add menus on Wednesday (2026-09-09) and Friday (2026-09-11)
    viewModel.saveMealMenuSync("2026-09-09", MealType.LUNCH, "Wed Special Lunch")
    viewModel.saveMealMenuSync("2026-09-11", MealType.DINNER, "Fri Special Dinner")
    // Add menu outside this week (2026-09-15)
    viewModel.saveMealMenuSync("2026-09-15", MealType.LUNCH, "Next Week Lunch")

    val weekly = viewModel.weeklyMenuState.first { !it.isLoading }
    assertEquals(7, weekly.days.size)

    val wednesdayItem = weekly.days.find { it.date == "2026-09-09" }
    assertNotNull(wednesdayItem)
    assertEquals("Wed Special Lunch", wednesdayItem?.lunch)

    val fridayItem = weekly.days.find { it.date == "2026-09-11" }
    assertNotNull(fridayItem)
    assertEquals("Fri Special Dinner", fridayItem?.dinner)

    // Date outside this week must not appear in this week's 7 days
    assertNull(weekly.days.find { it.date == "2026-09-15" })
  }

  @Test
  fun testCopyCompleteDayToAnotherDate() = runBlocking {
    val sourceDate = "2026-09-07"
    val targetDate = "2026-09-11"

    // Set Monday menu
    viewModel.saveMealMenuSync(sourceDate, MealType.BREAKFAST, "Poha + Tea")
    viewModel.saveMealMenuSync(sourceDate, MealType.LUNCH, "Dal + Rice + Sabzi + Roti")
    viewModel.saveMealMenuSync(sourceDate, MealType.DINNER, "Paneer + Roti + Salad")

    // Copy to Friday
    val copyResult = viewModel.copyMenuSync(sourceDate, targetDate)
    assertTrue(copyResult.isSuccess)
    assertEquals(3, copyResult.getOrNull())

    // Verify target day has identical menus
    viewModel.setDate(targetDate)
    val targetState = viewModel.dailyMenuState.first { !it.isLoading && it.date == targetDate }
    assertEquals("Poha + Tea", targetState.breakfast?.description)
    assertEquals("Dal + Rice + Sabzi + Roti", targetState.lunch?.description)
    assertEquals("Paneer + Roti + Salad", targetState.dinner?.description)
  }

  @Test
  fun testCopyingDoesNotCreateDuplicateDateMealRecords() = runBlocking {
    val sourceDate = "2026-09-07"
    val targetDate = "2026-09-08"

    viewModel.saveMealMenuSync(sourceDate, MealType.LUNCH, "Rajma Chawal")
    // Target already had a different lunch
    viewModel.saveMealMenuSync(targetDate, MealType.LUNCH, "Old Lunch")

    // Copying overwrites and updates rather than duplicating
    val copyResult = viewModel.copyMenuSync(sourceDate, targetDate)
    assertTrue(copyResult.isSuccess)

    val targetRecords = menuRepository.getMenuForDate(targetDate).first()
    assertEquals(1, targetRecords.size)
    assertEquals("Rajma Chawal", targetRecords[0].description)
  }

  @Test
  fun testCopySameDateRejection() = runBlocking {
    val date = "2026-09-07"
    viewModel.saveMealMenuSync(date, MealType.BREAKFAST, "Upma")
    val result = viewModel.copyMenuSync(date, date)
    assertFalse(result.isSuccess)
  }

  @Test
  fun testMenuTemplateFlow() = runBlocking {
    val date = "2026-09-08"
    viewModel.setDate(date)
    viewModel.saveMealMenuSync(date, MealType.BREAKFAST, "Poha")
    viewModel.saveMealMenuSync(date, MealType.LUNCH, "Thali")
    viewModel.saveMealMenuSync(date, MealType.DINNER, "Khichdi")

    viewModel.saveCurrentDayAsTemplateSync("Standard Feast")
    val templates = viewModel.templates.first { it.isNotEmpty() }
    assertEquals(1, templates.size)
    assertEquals("Standard Feast", templates[0].templateName)

    viewModel.setDate("2026-09-15")
    viewModel.applyTemplateSync(templates[0].id)

    val appliedState = viewModel.dailyMenuState.first { it.breakfast != null }
    assertEquals("Poha", appliedState.breakfast?.description)
    assertEquals("Thali", appliedState.lunch?.description)
    assertEquals("Khichdi", appliedState.dinner?.description)
  }
}
