package com.example.ui.employees

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.MessDatabase
import com.example.data.repository.EmployeeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmployeesViewModelTest {

  private lateinit var db: MessDatabase
  private lateinit var repository: EmployeeRepository
  private lateinit var viewModel: EmployeesViewModel

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = MessDatabase.createInMemoryDatabase(context)
    repository = EmployeeRepository(db.employeeDao())
    viewModel = EmployeesViewModel(repository)
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun testAddValidEmployee() = runBlocking {
    viewModel.openAddDialog()
    viewModel.onCodeChange("EMP001")
    viewModel.onNameChange("Rahul Sharma")
    viewModel.onDepartmentChange("IT")
    viewModel.onPhoneChange("9876543210")

    val success = viewModel.submitFormSync()
    assertTrue(success)

    val employees = repository.allEmployees.first()
    assertEquals(1, employees.size)
    val emp = employees[0]
    assertEquals("EMP001", emp.employeeCode)
    assertEquals("Rahul Sharma", emp.name)
    assertEquals("IT", emp.department)
    assertEquals("9876543210", emp.phone)
    assertTrue(emp.isActive)
    assertFalse(viewModel.isAddDialogOpen.value)
  }

  @Test
  fun testRequiredFieldValidation() = runBlocking {
    viewModel.openAddDialog()
    // Blank submit
    val success = viewModel.submitFormSync()
    assertFalse(success)

    val formState = viewModel.formState.value
    assertEquals("Employee ID cannot be blank", formState.codeError)
    assertEquals("Employee name cannot be blank", formState.nameError)
    assertEquals("Department cannot be blank", formState.departmentError)
    assertTrue(viewModel.isAddDialogOpen.value)

    val employees = repository.allEmployees.first()
    assertEquals(0, employees.size)
  }

  @Test
  fun testDuplicateEmployeeCodeRejection() = runBlocking {
    // Add first employee directly to repository
    repository.addEmployee("EMP001", "Rahul Sharma", "IT")

    // Try adding duplicate via ViewModel
    viewModel.openAddDialog()
    viewModel.onCodeChange("EMP001")
    viewModel.onNameChange("Another Person")
    viewModel.onDepartmentChange("Finance")
    val success = viewModel.submitFormSync()
    assertFalse(success)

    val formState = viewModel.formState.value
    assertNotNull(formState.generalError)
    assertTrue(formState.generalError!!.contains("already exists"))
    assertTrue(viewModel.isAddDialogOpen.value)

    val employees = repository.allEmployees.first()
    assertEquals(1, employees.size)
  }

  @Test
  fun testEditEmployee() = runBlocking {
    val addResult = repository.addEmployee("EMP002", "Amit Patil", "Accounts", "9999999999")
    val empId = addResult.getOrThrow()
    val initialEmp = repository.getEmployeeByIdOnce(empId)!!

    viewModel.openEditDialog(initialEmp)
    assertEquals("EMP002", viewModel.formState.value.employeeCode)
    assertEquals("Amit Patil", viewModel.formState.value.name)

    // Change name and department
    viewModel.onNameChange("Amit S. Patil")
    viewModel.onDepartmentChange("Finance")
    val success = viewModel.submitFormSync()
    assertTrue(success)

    val updatedEmp = repository.getEmployeeByIdOnce(empId)!!
    assertEquals("Amit S. Patil", updatedEmp.name)
    assertEquals("Finance", updatedEmp.department)
    assertEquals("EMP002", updatedEmp.employeeCode)
    assertFalse(viewModel.isAddDialogOpen.value)
  }

  @Test
  fun testDeactivateAndReactivateEmployee() = runBlocking {
    val addResult = repository.addEmployee("EMP003", "Priya Joshi", "HR")
    val empId = addResult.getOrThrow()
    val emp = repository.getEmployeeByIdOnce(empId)!!

    // Request deactivate
    viewModel.requestDeactivate(emp)
    assertEquals(emp, viewModel.employeeToConfirmDeactivate.value)

    // Confirm deactivate
    viewModel.confirmDeactivateSync()
    assertNull(viewModel.employeeToConfirmDeactivate.value)

    val empAfterDeactivate = repository.getEmployeeByIdOnce(empId)!!
    assertFalse(empAfterDeactivate.isActive)

    // Reactivate
    viewModel.reactivateEmployeeSync(empAfterDeactivate)

    val empAfterReactivate = repository.getEmployeeByIdOnce(empId)!!
    assertTrue(empAfterReactivate.isActive)
  }

  @Test
  fun testSearchByNameAndEmployeeCode() = runBlocking {
    repository.addEmployee("EMP001", "Rahul Sharma", "IT")
    repository.addEmployee("EMP002", "Amit Patil", "Accounts")
    repository.addEmployee("EMP003", "Priya Joshi", "HR")

    // Search by name
    val searchByName = repository.searchEmployees("Rahul").first()
    assertEquals(1, searchByName.size)
    assertEquals("Rahul Sharma", searchByName[0].name)

    // Search by code
    val searchByCode = repository.searchEmployees("EMP003").first()
    assertEquals(1, searchByCode.size)
    assertEquals("Priya Joshi", searchByCode[0].name)

    // Search by department
    val searchByDept = repository.searchEmployees("Accounts").first()
    assertEquals(1, searchByDept.size)
    assertEquals("Amit Patil", searchByDept[0].name)
  }

  @Test
  fun testActiveFilterAndCounts() = runBlocking {
    val id1 = repository.addEmployee("EMP001", "Rahul Sharma", "IT").getOrThrow()
    repository.addEmployee("EMP002", "Amit Patil", "Accounts")

    assertEquals(2, repository.activeEmployeeCount.first())

    // Deactivate Rahul
    repository.deactivateEmployee(id1)

    assertEquals(1, repository.activeEmployeeCount.first())
    val activeList = repository.activeEmployees.first()
    assertEquals(1, activeList.size)
    assertEquals("EMP002", activeList[0].employeeCode)

    // All employees still contains both
    val allList = repository.allEmployees.first()
    assertEquals(2, allList.size)
  }
}
