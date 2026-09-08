package com.example.ui.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Employee
import com.example.data.repository.EmployeeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EmployeeFilter {
  ALL,
  ACTIVE_ONLY,
  INACTIVE_ONLY
}

data class EmployeeFormState(
  val employeeCode: String = "",
  val name: String = "",
  val department: String = "",
  val phone: String = "",
  val codeError: String? = null,
  val nameError: String? = null,
  val departmentError: String? = null,
  val generalError: String? = null,
  val isSubmitting: Boolean = false
)

class EmployeesViewModel(
  private val employeeRepository: EmployeeRepository
) : ViewModel() {

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedFilter = MutableStateFlow(EmployeeFilter.ALL)
  val selectedFilter: StateFlow<EmployeeFilter> = _selectedFilter.asStateFlow()

  private val _selectedEmployee = MutableStateFlow<Employee?>(null)
  val selectedEmployee: StateFlow<Employee?> = _selectedEmployee.asStateFlow()

  private val _isAddDialogOpen = MutableStateFlow(false)
  val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

  private val _employeeToEdit = MutableStateFlow<Employee?>(null)
  val employeeToEdit: StateFlow<Employee?> = _employeeToEdit.asStateFlow()

  private val _employeeToConfirmDeactivate = MutableStateFlow<Employee?>(null)
  val employeeToConfirmDeactivate: StateFlow<Employee?> = _employeeToConfirmDeactivate.asStateFlow()

  private val _formState = MutableStateFlow(EmployeeFormState())
  val formState: StateFlow<EmployeeFormState> = _formState.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val employees: StateFlow<List<Employee>> = _searchQuery
    .flatMapLatest { query ->
      if (query.isBlank()) {
        employeeRepository.allEmployees
      } else {
        employeeRepository.searchEmployees(query)
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val activeCount: StateFlow<Int> = employeeRepository.activeEmployeeCount
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0
    )

  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  fun onFilterChange(filter: EmployeeFilter) {
    _selectedFilter.value = filter
  }

  fun selectEmployee(employee: Employee?) {
    _selectedEmployee.value = employee
  }

  fun openAddDialog() {
    _formState.value = EmployeeFormState()
    _employeeToEdit.value = null
    _isAddDialogOpen.value = true
  }

  fun openEditDialog(employee: Employee) {
    _employeeToEdit.value = employee
    _formState.value = EmployeeFormState(
      employeeCode = employee.employeeCode,
      name = employee.name,
      department = employee.department,
      phone = employee.phone ?: ""
    )
    _isAddDialogOpen.value = true
  }

  fun closeDialog() {
    _isAddDialogOpen.value = false
    _employeeToEdit.value = null
    _formState.value = EmployeeFormState()
  }

  fun onCodeChange(code: String) {
    _formState.value = _formState.value.copy(employeeCode = code, codeError = null, generalError = null)
  }

  fun onNameChange(name: String) {
    _formState.value = _formState.value.copy(name = name, nameError = null, generalError = null)
  }

  fun onDepartmentChange(department: String) {
    _formState.value = _formState.value.copy(department = department, departmentError = null, generalError = null)
  }

  fun onPhoneChange(phone: String) {
    _formState.value = _formState.value.copy(phone = phone)
  }

  suspend fun submitFormSync(): Boolean {
    val current = _formState.value
    val code = current.employeeCode.trim()
    val name = current.name.trim()
    val dept = current.department.trim()
    val phone = current.phone.trim().ifBlank { null }

    var hasError = false
    var codeErr: String? = null
    var nameErr: String? = null
    var deptErr: String? = null

    if (code.isBlank()) {
      codeErr = "Employee ID cannot be blank"
      hasError = true
    }
    if (name.isBlank()) {
      nameErr = "Employee name cannot be blank"
      hasError = true
    }
    if (dept.isBlank()) {
      deptErr = "Department cannot be blank"
      hasError = true
    }

    if (hasError) {
      _formState.value = current.copy(
        codeError = codeErr,
        nameError = nameErr,
        departmentError = deptErr
      )
      return false
    }

    _formState.value = current.copy(isSubmitting = true, generalError = null)

    val editTarget = _employeeToEdit.value
    return if (editTarget == null) {
      // Add new
      val result = employeeRepository.addEmployee(code, name, dept, phone)
      if (result.isSuccess) {
        _isAddDialogOpen.value = false
        _formState.value = EmployeeFormState()
        _userMessage.value = "Employee $name ($code) added successfully"
        true
      } else {
        _formState.value = _formState.value.copy(
          isSubmitting = false,
          generalError = result.exceptionOrNull()?.message ?: "Failed to add employee"
        )
        false
      }
    } else {
      // Edit existing
      val updated = editTarget.copy(
        employeeCode = code,
        name = name,
        department = dept,
        phone = phone
      )
      val result = employeeRepository.updateEmployee(updated)
      if (result.isSuccess) {
        _isAddDialogOpen.value = false
        _employeeToEdit.value = null
        _formState.value = EmployeeFormState()
        _userMessage.value = "Employee $name updated successfully"
        if (_selectedEmployee.value?.id == updated.id) {
          _selectedEmployee.value = updated
        }
        true
      } else {
        _formState.value = _formState.value.copy(
          isSubmitting = false,
          generalError = result.exceptionOrNull()?.message ?: "Failed to update employee"
        )
        false
      }
    }
  }

  fun submitForm() {
    viewModelScope.launch {
      submitFormSync()
    }
  }

  fun requestDeactivate(employee: Employee) {
    _employeeToConfirmDeactivate.value = employee
  }

  fun dismissDeactivateDialog() {
    _employeeToConfirmDeactivate.value = null
  }

  suspend fun confirmDeactivateSync() {
    val target = _employeeToConfirmDeactivate.value ?: return
    employeeRepository.deactivateEmployee(target.id)
    _employeeToConfirmDeactivate.value = null
    _userMessage.value = "${target.name} has been deactivated"
    if (_selectedEmployee.value?.id == target.id) {
      _selectedEmployee.value = _selectedEmployee.value?.copy(isActive = false)
    }
  }

  fun confirmDeactivate() {
    viewModelScope.launch {
      confirmDeactivateSync()
    }
  }

  suspend fun reactivateEmployeeSync(employee: Employee) {
    employeeRepository.activateEmployee(employee.id)
    _userMessage.value = "${employee.name} has been reactivated"
    if (_selectedEmployee.value?.id == employee.id) {
      _selectedEmployee.value = _selectedEmployee.value?.copy(isActive = true)
    }
  }

  fun reactivateEmployee(employee: Employee) {
    viewModelScope.launch {
      reactivateEmployeeSync(employee)
    }
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  class Factory(private val repository: EmployeeRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(EmployeesViewModel::class.java)) {
        return EmployeesViewModel(repository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
