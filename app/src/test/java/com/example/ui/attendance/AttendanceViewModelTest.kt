package com.example.ui.attendance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AttendanceViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var db: MessDatabase
  private lateinit var employeeRepository: EmployeeRepository
  private lateinit var attendanceRepository: AttendanceRepository
  private lateinit var viewModel: AttendanceViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    employeeRepository = EmployeeRepository(db.employeeDao())
    attendanceRepository = AttendanceRepository(db.mealAttendanceDao())
    viewModel = AttendanceViewModel(attendanceRepository, employeeRepository)
  }

  @After
  fun teardown() {
    db.close()
    try {
      Dispatchers.resetMain()
    } catch (_: Throwable) {}
  }

  @Test
  fun testEmptyEmployeesShowEmptyState() = runBlocking {
    val state = viewModel.rosterState.first { it !is AttendanceRosterState.Loading }
    assertTrue(state is AttendanceRosterState.Empty)
  }

  @Test
  fun testLoadActiveEmployeesInRoster() = runBlocking {
    employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT")
    employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts")

    val state = viewModel.rosterState.first { it is AttendanceRosterState.Success } as AttendanceRosterState.Success
    assertEquals(2, state.totalCount)
    assertEquals(0, state.presentCount)
    assertEquals("Amit Patil", state.items[0].employee.name)
    assertEquals("Rahul Sharma", state.items[1].employee.name)
  }

  @Test
  fun testMarkEmployeePresentAndSave() = runBlocking {
    val id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    viewModel.setDate("2026-09-07")
    viewModel.setMealType(MealType.LUNCH)

    // Initially 0 present
    var state = viewModel.rosterState.first { it is AttendanceRosterState.Success } as AttendanceRosterState.Success
    assertEquals(0, state.presentCount)

    // Mark Rahul present
    viewModel.setEmployeePresence(id, true)
    state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.presentCount == 1 } as AttendanceRosterState.Success
    assertTrue(state.hasUnsavedChanges)
    assertTrue(state.items[0].isPresent)

    // Save
    val saved = viewModel.saveAttendanceSync()
    assertTrue(saved)

    // Verify DB
    val dbRecords = attendanceRepository.getAttendanceForDateAndMeal("2026-09-07", MealType.LUNCH).first()
    assertEquals(1, dbRecords.size)
    assertEquals(id, dbRecords[0].employeeId)
    assertTrue(dbRecords[0].present)
  }

  @Test
  fun testMarkEmployeeAbsent() = runBlocking {
    val id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    viewModel.setDate("2026-09-07")
    viewModel.setMealType(MealType.LUNCH)

    // Save as present first
    attendanceRepository.setAttendance(id, "2026-09-07", MealType.LUNCH, present = true)

    // Load in viewModel
    var state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.presentCount == 1 } as AttendanceRosterState.Success
    assertTrue(state.items[0].isPresent)

    // Toggle to absent
    viewModel.setEmployeePresence(id, false)
    state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.presentCount == 0 } as AttendanceRosterState.Success
    assertFalse(state.items[0].isPresent)

    // Save
    viewModel.saveAttendanceSync()
    val dbRecords = attendanceRepository.getAttendanceForDateAndMeal("2026-09-07", MealType.LUNCH).first()
    assertEquals(1, dbRecords.size)
    assertFalse(dbRecords[0].present)
  }

  @Test
  fun testMarkAllPresentAndMarkAllAbsent() = runBlocking {
    val id1 = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val id2 = employeeRepository.addEmployee("EMP002", "Amit Patil", "Accounts").getOrThrow()
    val id3 = employeeRepository.addEmployee("EMP003", "Priya Joshi", "HR").getOrThrow()

    viewModel.setDate("2026-09-07")
    viewModel.setMealType(MealType.LUNCH)

    // Mark all present
    viewModel.markAllPresent()
    var state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.presentCount == 3 } as AttendanceRosterState.Success
    assertEquals(3, state.presentCount)
    assertTrue(state.hasUnsavedChanges)

    // Save
    viewModel.saveAttendanceSync()
    assertEquals(3, attendanceRepository.getPresentCountForDateAndMealOnce("2026-09-07", MealType.LUNCH))

    // Mark all absent
    viewModel.markAllAbsent()
    state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.presentCount == 0 } as AttendanceRosterState.Success
    assertEquals(0, state.presentCount)

    // Save
    viewModel.saveAttendanceSync()
    assertEquals(0, attendanceRepository.getPresentCountForDateAndMealOnce("2026-09-07", MealType.LUNCH))
  }

  @Test
  fun testMealTypeSeparation() = runBlocking {
    val id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val date = "2026-09-07"

    // Mark Breakfast = Present
    viewModel.setDate(date)
    viewModel.setMealType(MealType.BREAKFAST)
    viewModel.setEmployeePresence(id, true)
    viewModel.saveAttendanceSync()

    // Mark Lunch = Absent
    viewModel.setMealType(MealType.LUNCH)
    viewModel.setEmployeePresence(id, false)
    viewModel.saveAttendanceSync()

    // Mark Dinner = Present
    viewModel.setMealType(MealType.DINNER)
    viewModel.setEmployeePresence(id, true)
    viewModel.saveAttendanceSync()

    // Verify DB counts per meal
    assertEquals(1, attendanceRepository.getPresentCountForDateAndMealOnce(date, MealType.BREAKFAST))
    assertEquals(0, attendanceRepository.getPresentCountForDateAndMealOnce(date, MealType.LUNCH))
    assertEquals(1, attendanceRepository.getPresentCountForDateAndMealOnce(date, MealType.DINNER))
  }

  @Test
  fun testDateSeparation() = runBlocking {
    val id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()

    // Day 1 Lunch Present
    viewModel.setDate("2026-09-07")
    viewModel.setMealType(MealType.LUNCH)
    viewModel.setEmployeePresence(id, true)
    viewModel.saveAttendanceSync()

    // Day 2 Lunch Absent
    viewModel.setDate("2026-09-08")
    viewModel.setMealType(MealType.LUNCH)
    viewModel.setEmployeePresence(id, false)
    viewModel.saveAttendanceSync()

    assertEquals(1, attendanceRepository.getPresentCountForDateAndMealOnce("2026-09-07", MealType.LUNCH))
    assertEquals(0, attendanceRepository.getPresentCountForDateAndMealOnce("2026-09-08", MealType.LUNCH))
  }

  @Test
  fun testDuplicatePreventionOnUpdate() = runBlocking {
    val id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    val date = "2026-09-07"
    val meal = MealType.LUNCH

    // Save attendance multiple times
    viewModel.setDate(date)
    viewModel.setMealType(meal)
    viewModel.setEmployeePresence(id, true)
    viewModel.saveAttendanceSync()

    viewModel.setEmployeePresence(id, false)
    viewModel.saveAttendanceSync()

    viewModel.setEmployeePresence(id, true)
    viewModel.saveAttendanceSync()

    // Check that there is strictly 1 record in DB, not 3
    val records = attendanceRepository.getAttendanceForDateAndMeal(date, meal).first()
    assertEquals(1, records.size)
    assertTrue(records[0].present)
  }

  @Test
  fun testDeactivatedEmployeeExcludedFromNewAttendance() = runBlocking {
    val activeId = employeeRepository.addEmployee("EMP001", "Active Emp", "IT").getOrThrow()
    val inactiveId = employeeRepository.addEmployee("EMP002", "Inactive Emp", "HR").getOrThrow()

    // Deactivate second employee
    employeeRepository.deactivateEmployee(inactiveId)

    viewModel.setDate("2026-09-10")
    viewModel.setMealType(MealType.LUNCH)

    val state = viewModel.rosterState.first { it is AttendanceRosterState.Success } as AttendanceRosterState.Success
    assertEquals(1, state.totalCount)
    assertEquals(activeId, state.items[0].employee.id)
  }

  @Test
  fun testHistoricalAttendancePreservedForDeactivatedEmployee() = runBlocking {
    val activeId = employeeRepository.addEmployee("EMP001", "Active Emp", "IT").getOrThrow()
    val toDeactivateId = employeeRepository.addEmployee("EMP002", "Will Deactivate", "HR").getOrThrow()

    // Mark attendance on Sep 5 when both were active
    val pastDate = "2026-09-05"
    attendanceRepository.setAttendance(activeId, pastDate, MealType.LUNCH, present = true)
    attendanceRepository.setAttendance(toDeactivateId, pastDate, MealType.LUNCH, present = true)

    // Now deactivate EMP002 on Sep 7
    employeeRepository.deactivateEmployee(toDeactivateId)

    // View pastDate Sep 5 in AttendanceViewModel
    viewModel.setDate(pastDate)
    viewModel.setMealType(MealType.LUNCH)

    val state = viewModel.rosterState.first {
      (it as? AttendanceRosterState.Success)?.items?.size == 2
    } as AttendanceRosterState.Success

    assertEquals(2, state.totalCount)
    assertEquals(2, state.presentCount)

    val historicalItem = state.items.find { it.employee.id == toDeactivateId }!!
    assertTrue(historicalItem.isHistoricalOnly)
    assertTrue(historicalItem.isPresent)
    assertFalse(historicalItem.employee.isActive)
  }

  @Test
  fun testDiscardDraftChanges() = runBlocking {
    val id = employeeRepository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    viewModel.setDate("2026-09-07")
    viewModel.setMealType(MealType.LUNCH)

    // Make draft edit
    viewModel.setEmployeePresence(id, true)
    var state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.hasUnsavedChanges == true } as AttendanceRosterState.Success
    assertTrue(state.items[0].isPresent)

    // Discard
    viewModel.discardChanges()
    state = viewModel.rosterState.first { (it as? AttendanceRosterState.Success)?.hasUnsavedChanges == false } as AttendanceRosterState.Success
    assertFalse(state.items[0].isPresent)
  }
}
