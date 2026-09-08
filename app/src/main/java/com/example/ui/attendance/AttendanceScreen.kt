package com.example.ui.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveDone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
  viewModel: AttendanceViewModel,
  onNavigateToEmployees: () -> Unit,
  modifier: Modifier = Modifier
) {
  val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
  val selectedMeal by viewModel.selectedMeal.collectAsStateWithLifecycle()
  val rosterState by viewModel.rosterState.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  var showDatePicker by remember { mutableStateOf(false) }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearUserMessage()
    }
  }

  // Format date for display header
  val formattedDate = remember(selectedDate) {
    try {
      val parsed = AttendanceViewModel.DATE_FORMAT.parse(selectedDate)
      if (parsed != null) AttendanceViewModel.DISPLAY_DATE_FORMAT.format(parsed) else selectedDate
    } catch (_: Exception) {
      selectedDate
    }
  }

  val isToday = remember(selectedDate) {
    selectedDate == AttendanceViewModel.getTodayString()
  }

  Scaffold(
    modifier = modifier.fillMaxSize().testTag("attendance_screen"),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Meal Attendance",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = formattedDate + if (isToday) " (Today)" else "",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          // Go to today quick button
          if (!isToday) {
            IconButton(
              onClick = { viewModel.selectToday() },
              modifier = Modifier.testTag("today_button")
            ) {
              Icon(
                imageVector = Icons.Default.Today,
                contentDescription = "Go to Today",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          }
          // Date Picker dialog button
          IconButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.testTag("date_picker_button")
          ) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Select Date"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      // Bottom save / actions bar when there are changes or in success state
      if (rosterState is AttendanceRosterState.Success) {
        val success = rosterState as AttendanceRosterState.Success
        AttendanceBottomBar(
          successState = success,
          onSave = { viewModel.saveAttendance() },
          onDiscard = { viewModel.discardChanges() }
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Date Navigator Strip
      DateNavigator(
        currentDateString = formattedDate,
        isToday = isToday,
        onPreviousDay = { viewModel.selectPreviousDay() },
        onNextDay = { viewModel.selectNextDay() },
        onSelectCalendar = { showDatePicker = true }
      )

      // 2. Meal Type Tabs (Breakfast, Lunch, Dinner)
      MealTypeTabs(
        selectedMeal = selectedMeal,
        onMealSelected = { viewModel.setMealType(it) }
      )

      // 3. Roster Content (Stats, Quick bulk actions, and Employee list)
      when (val state = rosterState) {
        is AttendanceRosterState.Loading -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .testTag("attendance_loading"),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator()
              Spacer(modifier = Modifier.height(16.dp))
              Text("Loading attendance roster...", style = MaterialTheme.typography.bodyMedium)
            }
          }
        }

        is AttendanceRosterState.Empty -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp)
              .testTag("attendance_empty"),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "No Employees Available",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(20.dp))
              Button(
                onClick = onNavigateToEmployees,
                modifier = Modifier.testTag("go_to_employees_button")
              ) {
                Text("Manage Employees")
              }
            }
          }
        }

        is AttendanceRosterState.Error -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp)
              .testTag("attendance_error"),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "Failed to load attendance",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        is AttendanceRosterState.Success -> {
          AttendanceRosterContent(
            state = state,
            onToggleAttendance = { empId -> viewModel.toggleAttendance(empId) },
            onMarkAllPresent = { viewModel.markAllPresent() },
            onMarkAllAbsent = { viewModel.markAllAbsent() },
            onCopyYesterday = { viewModel.copyPreviousDayAttendance() }
          )
        }
      }
    }
  }

  // DatePickerDialog
  if (showDatePicker) {
    AttendanceDatePickerDialog(
      initialDateString = selectedDate,
      onDateSelected = { dateStr ->
        viewModel.setDate(dateStr)
        showDatePicker = false
      },
      onDismiss = { showDatePicker = false }
    )
  }
}

@Composable
private fun DateNavigator(
  currentDateString: String,
  isToday: Boolean,
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onSelectCalendar: () -> Unit
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      IconButton(
        onClick = onPreviousDay,
        modifier = Modifier.testTag("prev_day_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Previous Day"
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onSelectCalendar)
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text(
          text = currentDateString,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
      }

      IconButton(
        onClick = onNextDay,
        modifier = Modifier.testTag("next_day_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Next Day"
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealTypeTabs(
  selectedMeal: MealType,
  onMealSelected: (MealType) -> Unit
) {
  val meals = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
  val selectedIndex = meals.indexOf(selectedMeal)

  SecondaryTabRow(
    selectedTabIndex = selectedIndex,
    modifier = Modifier
      .fillMaxWidth()
      .testTag("meal_type_tabs")
  ) {
    meals.forEachIndexed { index, meal ->
      Tab(
        selected = selectedIndex == index,
        onClick = { onMealSelected(meal) },
        text = {
          Text(
            text = meal.name.lowercase().replaceFirstChar { it.uppercase() },
            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal
          )
        },
        modifier = Modifier.testTag("meal_tab_${meal.name.lowercase()}")
      )
    }
  }
}

@Composable
private fun AttendanceRosterContent(
  state: AttendanceRosterState.Success,
  onToggleAttendance: (Long) -> Unit,
  onMarkAllPresent: () -> Unit,
  onMarkAllAbsent: () -> Unit,
  onCopyYesterday: () -> Unit = {}
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Attendance count summary & bulk actions row
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Stats
          Column {
            Text(
              text = "${state.presentCount} / ${state.totalCount} Present",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "${state.totalCount - state.presentCount} Absent",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Copy Yesterday Button
          OutlinedButton(
            onClick = onCopyYesterday,
            modifier = Modifier.testTag("copy_yesterday_attendance_button"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy Yesterday", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bulk action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onMarkAllPresent,
            modifier = Modifier
              .weight(1f)
              .testTag("mark_all_present_button"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("All Present", fontSize = 12.sp)
          }

          OutlinedButton(
            onClick = onMarkAllAbsent,
            modifier = Modifier
              .weight(1f)
              .testTag("mark_all_absent_button"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.RemoveDone,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("All Absent", fontSize = 12.sp)
          }
        }
      }
    }

    // Employee Roster List
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("roster_list"),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(
        items = state.items,
        key = { it.employee.id }
      ) { item ->
        AttendanceRow(
          item = item,
          onToggle = { onToggleAttendance(item.employee.id) }
        )
      }
    }
  }
}

@Composable
private fun AttendanceRow(
  item: AttendanceRowItem,
  onToggle: () -> Unit
) {
  val employee = item.employee
  val isPresent = item.isPresent

  val backgroundColor by animateColorAsState(
    targetValue = if (isPresent) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
      MaterialTheme.colorScheme.surface
    },
    label = "row_bg"
  )

  val borderColor = if (isPresent) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
  } else {
    MaterialTheme.colorScheme.outlineVariant
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onToggle)
      .testTag("attendance_row_${employee.employeeCode}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Avatar + Details
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
              if (isPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = employee.name.firstOrNull()?.uppercase() ?: "?",
            color = if (isPresent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = employee.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
            if (item.isHistoricalOnly) {
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "Deactivated",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = employee.employeeCode,
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = " • ${employee.department}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Right: Present / Absent Badge Button
      Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(8.dp),
        color = if (isPresent) {
          MaterialTheme.colorScheme.primary
        } else {
          MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.testTag("toggle_presence_${employee.employeeCode}")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = if (isPresent) Icons.Default.Check else Icons.Default.Clear,
            contentDescription = if (isPresent) "Present" else "Absent",
            tint = if (isPresent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isPresent) "Present" else "Absent",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPresent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun AttendanceBottomBar(
  successState: AttendanceRosterState.Success,
  onSave: () -> Unit,
  onDiscard: () -> Unit
) {
  Surface(
    tonalElevation = 8.dp,
    shadowElevation = 8.dp,
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      if (successState.hasUnsavedChanges) {
        TextButton(
          onClick = onDiscard,
          modifier = Modifier.testTag("discard_changes_button")
        ) {
          Text("Discard")
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Saved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Button(
        onClick = onSave,
        enabled = !successState.isSaving,
        modifier = Modifier.testTag("save_attendance_button")
      ) {
        if (successState.isSaving) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onPrimary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Saving...")
        } else {
          Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(if (successState.hasUnsavedChanges) "Save Attendance" else "Update Attendance")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttendanceDatePickerDialog(
  initialDateString: String,
  onDateSelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val initialMillis = remember(initialDateString) {
    try {
      val parsed = AttendanceViewModel.DATE_FORMAT.parse(initialDateString)
      parsed?.time ?: System.currentTimeMillis()
    } catch (_: Exception) {
      System.currentTimeMillis()
    }
  }

  val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = initialMillis
  )

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          val selected = datePickerState.selectedDateMillis
          if (selected != null) {
            // Note: selectedDateMillis is in UTC. Format using UTC calendar
            val cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
              timeInMillis = selected
            }
            val yyyy = cal.get(java.util.Calendar.YEAR)
            val mm = String.format(Locale.US, "%02d", cal.get(java.util.Calendar.MONTH) + 1)
            val dd = String.format(Locale.US, "%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
            onDateSelected("$yyyy-$mm-$dd")
          } else {
            onDismiss()
          }
        },
        modifier = Modifier.testTag("confirm_date_button")
      ) {
        Text("OK")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  ) {
    DatePicker(state = datePickerState)
  }
}
