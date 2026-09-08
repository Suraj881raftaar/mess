package com.example.data.repository

import com.example.data.dao.EmployeeDao
import com.example.data.entity.Employee
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val employeeDao: EmployeeDao) {

  val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()

  val activeEmployees: Flow<List<Employee>> = employeeDao.getActiveEmployees()

  val activeEmployeeCount: Flow<Int> = employeeDao.getActiveCount()

  fun getEmployeeById(id: Long): Flow<Employee?> = employeeDao.getEmployeeById(id)

  suspend fun getEmployeeByIdOnce(id: Long): Employee? = employeeDao.getEmployeeByIdOnce(id)

  suspend fun getEmployeeByCode(code: String): Employee? = employeeDao.getEmployeeByCode(code.trim())

  fun searchEmployees(query: String): Flow<List<Employee>> = employeeDao.searchEmployees(query.trim())

  suspend fun addEmployee(
    code: String,
    name: String,
    department: String,
    phone: String? = null
  ): Result<Long> {
    val trimmedCode = code.trim()
    val trimmedName = name.trim()
    val trimmedDept = department.trim()

    if (trimmedCode.isBlank()) {
      return Result.failure(IllegalArgumentException("Employee ID / Code cannot be blank"))
    }
    if (trimmedName.isBlank()) {
      return Result.failure(IllegalArgumentException("Employee name cannot be blank"))
    }
    if (trimmedDept.isBlank()) {
      return Result.failure(IllegalArgumentException("Department cannot be blank"))
    }

    val existing = employeeDao.getEmployeeByCode(trimmedCode)
    if (existing != null) {
      return Result.failure(IllegalStateException("Employee ID '$trimmedCode' already exists"))
    }

    val employee = Employee(
      employeeCode = trimmedCode,
      name = trimmedName,
      department = trimmedDept,
      phone = phone?.trim()?.ifBlank { null },
      isActive = true
    )
    val id = employeeDao.insert(employee)
    return Result.success(id)
  }

  suspend fun updateEmployee(employee: Employee): Result<Unit> {
    if (employee.employeeCode.isBlank()) {
      return Result.failure(IllegalArgumentException("Employee ID cannot be blank"))
    }
    if (employee.name.isBlank()) {
      return Result.failure(IllegalArgumentException("Employee name cannot be blank"))
    }

    val existing = employeeDao.getEmployeeByCode(employee.employeeCode.trim())
    if (existing != null && existing.id != employee.id) {
      return Result.failure(IllegalStateException("Employee ID '${employee.employeeCode}' is used by another employee"))
    }

    employeeDao.update(
      employee.copy(
        employeeCode = employee.employeeCode.trim(),
        name = employee.name.trim(),
        department = employee.department.trim(),
        phone = employee.phone?.trim()?.ifBlank { null }
      )
    )
    return Result.success(Unit)
  }

  suspend fun deactivateEmployee(id: Long) {
    employeeDao.setEmployeeActiveStatus(id, isActive = false)
  }

  suspend fun activateEmployee(id: Long) {
    employeeDao.setEmployeeActiveStatus(id, isActive = true)
  }
}
