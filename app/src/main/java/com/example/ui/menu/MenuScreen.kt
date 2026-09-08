package com.example.ui.menu

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.Menu
import com.example.data.model.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
  viewModel: MenuViewModel,
  modifier: Modifier = Modifier
) {
  val menuMode by viewModel.menuMode.collectAsStateWithLifecycle()
  val dailyState by viewModel.dailyMenuState.collectAsStateWithLifecycle()
  val weeklyState by viewModel.weeklyMenuState.collectAsStateWithLifecycle()
  val editMealDialogState by viewModel.editMealDialogState.collectAsStateWithLifecycle()
  val copyDayDialogState by viewModel.copyDayDialogState.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(userMessage) {
    userMessage?.let {
      snackbarHostState.showSnackbar(
        message = it,
        duration = SnackbarDuration.Short
      )
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .testTag("menu_screen"),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.RestaurantMenu,
              contentDescription = null,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Mess Menu",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
          }
        },
        actions = {
          if (menuMode == MenuMode.DAILY && dailyState.hasAnyMeal) {
            FilledTonalButton(
              onClick = { viewModel.openCopyDayDialog(dailyState.date) },
              modifier = Modifier.testTag("btn_copy_day_menu")
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copy Day")
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      PrimaryTabRow(
        selectedTabIndex = menuMode.ordinal,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = menuMode == MenuMode.DAILY,
          onClick = { viewModel.setMenuMode(MenuMode.DAILY) },
          text = { Text("Daily Menu") },
          icon = { Icon(Icons.Default.Today, contentDescription = "Daily Menu") },
          modifier = Modifier.testTag("menu_mode_daily")
        )
        Tab(
          selected = menuMode == MenuMode.WEEKLY,
          onClick = { viewModel.setMenuMode(MenuMode.WEEKLY) },
          text = { Text("Weekly Overview") },
          icon = { Icon(Icons.Default.DateRange, contentDescription = "Weekly Overview") },
          modifier = Modifier.testTag("menu_mode_weekly")
        )
      }

      when (menuMode) {
        MenuMode.DAILY -> {
          DailyMenuView(
            state = dailyState,
            onPreviousDay = { viewModel.selectPreviousDay() },
            onNextDay = { viewModel.selectNextDay() },
            onToday = { viewModel.selectToday() },
            onSelectDate = { viewModel.setDate(it) },
            onEditMeal = { mealType, currentText ->
              viewModel.openEditMealDialog(dailyState.date, mealType, currentText)
            }
          )
        }
        MenuMode.WEEKLY -> {
          WeeklyMenuView(
            state = weeklyState,
            onPreviousWeek = { viewModel.selectPreviousWeek() },
            onNextWeek = { viewModel.selectNextWeek() },
            onThisWeek = { viewModel.selectCurrentWeek() },
            onSelectDay = { date -> viewModel.navigateToDateFromWeekly(date) }
          )
        }
      }
    }
  }

  // Edit Meal Dialog
  editMealDialogState?.let { dialogState ->
    EditMealDialog(
      state = dialogState,
      onDismiss = { viewModel.closeEditMealDialog() },
      onSave = { desc ->
        viewModel.saveMealMenu(dialogState.date, dialogState.mealType, desc)
      }
    )
  }

  // Copy Day Dialog
  copyDayDialogState?.let { dialogState ->
    CopyDayDialog(
      state = dialogState,
      onTargetDateChange = { viewModel.setCopyTargetDate(it) },
      onDismiss = { viewModel.closeCopyDayDialog() },
      onConfirm = { viewModel.confirmCopyMenu() }
    )
  }
}

@Composable
private fun DailyMenuView(
  state: DailyMenuState,
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onToday: () -> Unit,
  onSelectDate: (String) -> Unit,
  onEditMeal: (MealType, String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showDatePicker by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Date Navigation Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onPreviousDay,
          modifier = Modifier.testTag("btn_prev_date")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clickable { showDatePicker = true }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = state.formattedDate.ifBlank { state.date },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Pick date",
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          }
          if (state.isToday) {
            Text(
              text = "Today",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (!state.isToday) {
            OutlinedButton(
              onClick = onToday,
              modifier = Modifier.testTag("btn_today"),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text("Today", style = MaterialTheme.typography.labelMedium)
            }
          }
          IconButton(
            onClick = onNextDay,
            modifier = Modifier.testTag("btn_next_date")
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
          }
        }
      }
    }

    if (state.isLoading) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator()
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
      ) {
        item {
          MealCard(
            mealType = MealType.BREAKFAST,
            icon = Icons.Default.FreeBreakfast,
            menu = state.breakfast,
            onEdit = { onEditMeal(MealType.BREAKFAST, state.breakfast?.description.orEmpty()) },
            testTag = "meal_card_breakfast",
            editTestTag = "btn_edit_breakfast"
          )
        }
        item {
          MealCard(
            mealType = MealType.LUNCH,
            icon = Icons.Default.LunchDining,
            menu = state.lunch,
            onEdit = { onEditMeal(MealType.LUNCH, state.lunch?.description.orEmpty()) },
            testTag = "meal_card_lunch",
            editTestTag = "btn_edit_lunch"
          )
        }
        item {
          MealCard(
            mealType = MealType.DINNER,
            icon = Icons.Default.DinnerDining,
            menu = state.dinner,
            onEdit = { onEditMeal(MealType.DINNER, state.dinner?.description.orEmpty()) },
            testTag = "meal_card_dinner",
            editTestTag = "btn_edit_dinner"
          )
        }
      }
    }
  }

  if (showDatePicker) {
    DatePickerModal(
      initialDateString = state.date,
      onDateSelected = { selectedDateStr ->
        onSelectDate(selectedDateStr)
        showDatePicker = false
      },
      onDismiss = { showDatePicker = false }
    )
  }
}

@Composable
private fun MealCard(
  mealType: MealType,
  icon: ImageVector,
  menu: Menu?,
  onEdit: () -> Unit,
  testTag: String,
  editTestTag: String,
  modifier: Modifier = Modifier
) {
  val hasContent = !menu?.description.isNullOrBlank()

  OutlinedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(
      width = 1.dp,
      color = if (hasContent) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
      else MaterialTheme.colorScheme.outlineVariant
    ),
    colors = CardDefaults.outlinedCardColors(
      containerColor = if (hasContent) MaterialTheme.colorScheme.surface
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = mealType.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        FilledTonalButton(
          onClick = onEdit,
          modifier = Modifier.testTag(editTestTag)
        ) {
          Icon(
            imageVector = if (hasContent) Icons.Default.Edit else Icons.Default.Edit,
            contentDescription = "Edit ${mealType.displayName}",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (hasContent) "Edit" else "Set Menu")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (hasContent) {
        Text(
          text = menu!!.description,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
      } else {
        Text(
          text = "No menu items specified for ${mealType.displayName.lowercase()}.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun WeeklyMenuView(
  state: WeeklyMenuState,
  onPreviousWeek: () -> Unit,
  onNextWeek: () -> Unit,
  onThisWeek: () -> Unit,
  onSelectDay: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // Week navigation header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onPreviousWeek) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Week")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = state.formattedWeekRange,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          OutlinedButton(
            onClick = onThisWeek,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp)
          ) {
            Text("This Week", style = MaterialTheme.typography.labelSmall)
          }
        }

        IconButton(onClick = onNextWeek) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week")
        }
      }
    }

    if (state.isLoading) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator()
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .testTag("weekly_menu_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
      ) {
        items(state.days, key = { it.date }) { dayItem ->
          WeeklyDayCard(
            dayItem = dayItem,
            onClick = { onSelectDay(dayItem.date) }
          )
        }
      }
    }
  }
}

@Composable
private fun WeeklyDayCard(
  dayItem: DayMenuItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("weekly_day_${dayItem.date}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (dayItem.isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
      else MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = if (dayItem.isToday) 2.dp else 1.dp,
      color = if (dayItem.isToday) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.outlineVariant
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = dayItem.dayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = dayItem.formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (dayItem.isToday) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "(Today)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Text(
          text = "View / Edit >",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MealSummaryRow(
          mealName = "Breakfast",
          content = dayItem.breakfast
        )
        MealSummaryRow(
          mealName = "Lunch",
          content = dayItem.lunch
        )
        MealSummaryRow(
          mealName = "Dinner",
          content = dayItem.dinner
        )
      }
    }
  }
}

@Composable
private fun MealSummaryRow(
  mealName: String,
  content: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = "$mealName: ",
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.width(76.dp)
    )
    Text(
      text = content.ifBlank { "—" },
      style = MaterialTheme.typography.bodySmall,
      color = if (content.isNotBlank()) MaterialTheme.colorScheme.onSurface
      else MaterialTheme.colorScheme.outline,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun EditMealDialog(
  state: EditMealDialogState,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var descriptionText by remember { mutableStateOf(state.currentDescription) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit ${state.mealType.displayName} Menu",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Date: ${state.date}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = descriptionText,
          onValueChange = { descriptionText = it },
          label = { Text("Menu description") },
          placeholder = { Text("e.g. Dal, Rice, Sabzi, Roti, Salad") },
          minLines = 3,
          maxLines = 6,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_meal_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(descriptionText) },
        modifier = Modifier.testTag("btn_save_meal_menu")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun CopyDayDialog(
  state: CopyDayDialogState,
  onTargetDateChange: (String) -> Unit,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit
) {
  var showTargetPicker by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Copy Day's Menu",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Copy menu from: ${state.sourceDate}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = state.sourceSummary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Text(
          text = "Target Date:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold
        )
        OutlinedCard(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showTargetPicker = true }
            .padding(vertical = 6.dp)
            .testTag("copy_target_date_selector"),
          shape = RoundedCornerShape(8.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = state.targetDate,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Medium
            )
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = "Pick target date",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Note: Any existing menu items on ${state.targetDate} will be overwritten.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        modifier = Modifier.testTag("btn_confirm_copy_menu")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Confirm Copy")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )

  if (showTargetPicker) {
    DatePickerModal(
      initialDateString = state.targetDate,
      onDateSelected = {
        onTargetDateChange(it)
        showTargetPicker = false
      },
      onDismiss = { showTargetPicker = false }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
  initialDateString: String,
  onDateSelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val initialMillis = remember(initialDateString) {
    try {
      val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(initialDateString)
      date?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
      System.currentTimeMillis()
    }
  }

  val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          datePickerState.selectedDateMillis?.let { millis ->
            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
            onDateSelected(formatted)
          } ?: onDismiss()
        }
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
