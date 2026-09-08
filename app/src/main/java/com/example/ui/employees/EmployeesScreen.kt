package com.example.ui.employees

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.lazy.LazyRow
import com.example.data.entity.Employee
import com.example.data.model.DietaryPreference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
  viewModel: EmployeesViewModel,
  modifier: Modifier = Modifier
) {
  val employees by viewModel.employees.collectAsStateWithLifecycle()
  val activeCount by viewModel.activeCount.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
  val selectedEmployee by viewModel.selectedEmployee.collectAsStateWithLifecycle()
  val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsStateWithLifecycle()
  val employeeToEdit by viewModel.employeeToEdit.collectAsStateWithLifecycle()
  val employeeToConfirmDeactivate by viewModel.employeeToConfirmDeactivate.collectAsStateWithLifecycle()
  val formState by viewModel.formState.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  // Filtered employees list: active employees sorted first, then inactive
  val filteredEmployees = remember(employees, selectedFilter) {
    val list = when (selectedFilter) {
      EmployeeFilter.ALL -> employees
      EmployeeFilter.ACTIVE_ONLY -> employees.filter { it.isActive }
      EmployeeFilter.INACTIVE_ONLY -> employees.filter { !it.isActive }
    }
    list.sortedWith(compareByDescending<Employee> { it.isActive }.thenBy { it.name.lowercase(Locale.getDefault()) })
  }

  Scaffold(
    modifier = modifier.testTag("employees_screen"),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (selectedEmployee != null) "Employee Details" else "Employees",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            if (selectedEmployee == null) {
              Text(
                text = "$activeCount active • ${employees.size} total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          if (selectedEmployee != null) {
            IconButton(
              onClick = { viewModel.selectEmployee(null) },
              modifier = Modifier.testTag("back_to_list_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to employee list"
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    floatingActionButton = {
      if (selectedEmployee == null) {
        FloatingActionButton(
          onClick = { viewModel.openAddDialog() },
          modifier = Modifier.testTag("add_employee_fab"),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Employee")
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (selectedEmployee != null) {
        EmployeeDetailContent(
          employee = selectedEmployee!!,
          onEdit = { viewModel.openEditDialog(it) },
          onDeactivate = { viewModel.requestDeactivate(it) },
          onReactivate = { viewModel.reactivateEmployee(it) }
        )
      } else {
        EmployeeListContent(
          employees = filteredEmployees,
          searchQuery = searchQuery,
          selectedFilter = selectedFilter,
          onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
          onFilterChange = { viewModel.onFilterChange(it) },
          onSelectEmployee = { viewModel.selectEmployee(it) },
          onEditEmployee = { viewModel.openEditDialog(it) },
          onDeactivateEmployee = { viewModel.requestDeactivate(it) },
          onReactivateEmployee = { viewModel.reactivateEmployee(it) }
        )
      }
    }
  }

  // Add / Edit Dialog
  if (isAddDialogOpen) {
    EmployeeFormDialog(
      formState = formState,
      isEdit = employeeToEdit != null,
      onCodeChange = { viewModel.onCodeChange(it) },
      onNameChange = { viewModel.onNameChange(it) },
      onDepartmentChange = { viewModel.onDepartmentChange(it) },
      onPhoneChange = { viewModel.onPhoneChange(it) },
      onDietaryPreferenceChange = { viewModel.onDietaryPreferenceChange(it) },
      onSubmit = { viewModel.submitForm() },
      onDismiss = { viewModel.closeDialog() }
    )
  }

  // Deactivate Confirmation Dialog
  if (employeeToConfirmDeactivate != null) {
    DeactivateConfirmationDialog(
      employee = employeeToConfirmDeactivate!!,
      onConfirm = { viewModel.confirmDeactivate() },
      onDismiss = { viewModel.dismissDeactivateDialog() }
    )
  }
}

@Composable
private fun EmployeeListContent(
  employees: List<Employee>,
  searchQuery: String,
  selectedFilter: EmployeeFilter,
  onSearchQueryChange: (String) -> Unit,
  onFilterChange: (EmployeeFilter) -> Unit,
  onSelectEmployee: (Employee) -> Unit,
  onEditEmployee: (Employee) -> Unit,
  onDeactivateEmployee: (Employee) -> Unit,
  onReactivateEmployee: (Employee) -> Unit
) {
  val focusManager = LocalFocusManager.current

  Column(modifier = Modifier.fillMaxSize()) {
    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchQueryChange,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .testTag("employee_search_field"),
      placeholder = { Text("Search by name, ID or department...") },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = "Search Icon")
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(
            onClick = { onSearchQueryChange("") },
            modifier = Modifier.testTag("clear_search_button")
          ) {
            Icon(Icons.Default.Clear, contentDescription = "Clear search")
          }
        }
      },
      singleLine = true,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
      shape = RoundedCornerShape(12.dp)
    )

    // Filter Chips
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedFilter == EmployeeFilter.ALL,
        onClick = { onFilterChange(EmployeeFilter.ALL) },
        label = { Text("All") },
        modifier = Modifier.testTag("filter_all")
      )
      FilterChip(
        selected = selectedFilter == EmployeeFilter.ACTIVE_ONLY,
        onClick = { onFilterChange(EmployeeFilter.ACTIVE_ONLY) },
        label = { Text("Active") },
        modifier = Modifier.testTag("filter_active")
      )
      FilterChip(
        selected = selectedFilter == EmployeeFilter.INACTIVE_ONLY,
        onClick = { onFilterChange(EmployeeFilter.INACTIVE_ONLY) },
        label = { Text("Inactive") },
        modifier = Modifier.testTag("filter_inactive")
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // List or Empty State
    if (employees.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(64.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = if (searchQuery.isNotBlank()) "No employees matching \"$searchQuery\"" else "No employees found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (searchQuery.isNotBlank()) "Try another search term" else "Tap + button below to add an employee",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("employee_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp, top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(employees, key = { it.id }) { employee ->
          EmployeeItemCard(
            employee = employee,
            onCardClick = { onSelectEmployee(employee) },
            onEditClick = { onEditEmployee(employee) },
            onDeactivateClick = { onDeactivateEmployee(employee) },
            onReactivateClick = { onReactivateEmployee(employee) }
          )
        }
      }
    }
  }
}

@Composable
private fun EmployeeItemCard(
  employee: Employee,
  onCardClick: () -> Unit,
  onEditClick: () -> Unit,
  onDeactivateClick: () -> Unit,
  onReactivateClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .alpha(if (employee.isActive) 1f else 0.72f)
      .clickable(onClick = onCardClick)
      .testTag("employee_card_${employee.employeeCode}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (employee.isActive) {
        MaterialTheme.colorScheme.surfaceContainerHigh
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      }
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (employee.isActive) 1.5.dp else 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Avatar badge with initials
      Surface(
        shape = CircleShape,
        color = if (employee.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(48.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          val initials = employee.name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifBlank { "E" }
          Text(
            text = initials,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (employee.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Info Column
      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = employee.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (!employee.isActive) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.errorContainer
            ) {
              Text(
                text = "Inactive",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            text = employee.employeeCode,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = employee.department,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
          )
          Text(
            text = employee.dietaryPreference.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
          )
        }
      }

      // Action icon buttons
      IconButton(
        onClick = onEditClick,
        modifier = Modifier
          .size(36.dp)
          .testTag("edit_employee_${employee.employeeCode}")
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = "Edit ${employee.name}",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }

      if (employee.isActive) {
        IconButton(
          onClick = onDeactivateClick,
          modifier = Modifier
            .size(36.dp)
            .testTag("deactivate_employee_${employee.employeeCode}")
        ) {
          Icon(
            imageVector = Icons.Default.Block,
            contentDescription = "Deactivate ${employee.name}",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
          )
        }
      } else {
        IconButton(
          onClick = onReactivateClick,
          modifier = Modifier
            .size(36.dp)
            .testTag("reactivate_employee_${employee.employeeCode}")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reactivate ${employee.name}",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun EmployeeDetailContent(
  employee: Employee,
  onEdit: (Employee) -> Unit,
  onDeactivate: (Employee) -> Unit,
  onReactivate: (Employee) -> Unit
) {
  val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("employee_detail_view")
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Surface(
          shape = CircleShape,
          color = if (employee.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.size(72.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              modifier = Modifier.size(40.dp),
              tint = if (employee.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = employee.name,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (employee.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        ) {
          Text(
            text = if (employee.isActive) "ACTIVE EMPLOYEE" else "DEACTIVATED",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (employee.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Information breakdown card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        DetailRow(icon = Icons.Default.Badge, label = "Employee Code / ID", value = employee.employeeCode)
        DetailRow(icon = Icons.Default.Person, label = "Department", value = employee.department)
        DetailRow(icon = Icons.Default.Restaurant, label = "Dietary Preference", value = employee.dietaryPreference.label)
        DetailRow(icon = Icons.Default.Phone, label = "Phone", value = employee.phone ?: "Not provided")
        DetailRow(
          icon = Icons.Default.CheckCircle,
          label = "Registered Date",
          value = dateFormatter.format(Date(employee.createdAt))
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Note explaining future monthly attendance hook
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Monthly attendance history and billing breakdown will appear here once attendance records are logged.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    // Action buttons at bottom
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedButton(
        onClick = { onEdit(employee) },
        modifier = Modifier
          .weight(1f)
          .testTag("detail_edit_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Edit")
      }

      if (employee.isActive) {
        Button(
          onClick = { onDeactivate(employee) },
          modifier = Modifier
            .weight(1f)
            .testTag("detail_deactivate_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Deactivate")
        }
      } else {
        Button(
          onClick = { onReactivate(employee) },
          modifier = Modifier
            .weight(1f)
            .testTag("detail_reactivate_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Reactivate")
        }
      }
    }
  }
}

@Composable
private fun DetailRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
private fun EmployeeFormDialog(
  formState: EmployeeFormState,
  isEdit: Boolean,
  onCodeChange: (String) -> Unit,
  onNameChange: (String) -> Unit,
  onDepartmentChange: (String) -> Unit,
  onPhoneChange: (String) -> Unit,
  onDietaryPreferenceChange: (DietaryPreference) -> Unit,
  onSubmit: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isEdit) "Edit Employee" else "Add New Employee",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        if (formState.generalError != null) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = formState.generalError,
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(10.dp)
            )
          }
        }

        OutlinedTextField(
          value = formState.employeeCode,
          onValueChange = onCodeChange,
          label = { Text("Employee ID / Code *") },
          placeholder = { Text("e.g. EMP001") },
          singleLine = true,
          isError = formState.codeError != null,
          supportingText = formState.codeError?.let { { Text(it) } },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_form_code_input")
        )

        OutlinedTextField(
          value = formState.name,
          onValueChange = onNameChange,
          label = { Text("Full Name *") },
          placeholder = { Text("e.g. Rahul Sharma") },
          singleLine = true,
          isError = formState.nameError != null,
          supportingText = formState.nameError?.let { { Text(it) } },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_form_name_input")
        )

        OutlinedTextField(
          value = formState.department,
          onValueChange = onDepartmentChange,
          label = { Text("Department *") },
          placeholder = { Text("e.g. IT, Accounts, HR") },
          singleLine = true,
          isError = formState.departmentError != null,
          supportingText = formState.departmentError?.let { { Text(it) } },
          keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_form_dept_input")
        )

        OutlinedTextField(
          value = formState.phone,
          onValueChange = onPhoneChange,
          label = { Text("Phone Number (Optional)") },
          placeholder = { Text("e.g. 9876543210") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
          ),
          keyboardActions = KeyboardActions(onDone = { onSubmit() }),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_form_phone_input")
        )

        // Dietary Preference Selection
        Column {
          Text(
            text = "Dietary Preference",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(4.dp))
          LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(DietaryPreference.values()) { pref ->
              FilterChip(
                selected = formState.dietaryPreference == pref,
                onClick = { onDietaryPreferenceChange(pref) },
                label = { Text(pref.label, style = MaterialTheme.typography.labelSmall) }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onSubmit,
        enabled = !formState.isSubmitting,
        modifier = Modifier.testTag("employee_form_submit_button")
      ) {
        if (formState.isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
        } else {
          Text(if (isEdit) "Save Changes" else "Add Employee")
        }
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("employee_form_cancel_button")
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun DeactivateConfirmationDialog(
  employee: Employee,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Block,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(32.dp)
      )
    },
    title = { Text(text = "Deactivate Employee?") },
    text = {
      Text(
        text = "Are you sure you want to deactivate ${employee.name} (${employee.employeeCode})?\n\n" +
          "They will no longer appear in new daily attendance rosters. All historical attendance and monthly records will be safely preserved."
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = Modifier.testTag("confirm_deactivate_button")
      ) {
        Text("Deactivate")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_deactivate_button")
      ) {
        Text("Cancel")
      }
    }
  )
}
