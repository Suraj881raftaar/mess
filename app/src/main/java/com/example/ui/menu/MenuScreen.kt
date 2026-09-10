package com.example.ui.menu

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.draw.clip
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
import com.example.data.entity.MenuTemplate
import com.example.data.model.MealType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.feedback.MealFeedbackDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
  viewModel: MenuViewModel,
  modifier: Modifier = Modifier
) {
  val menuMode by viewModel.menuMode.collectAsStateWithLifecycle()
  val dailyState by viewModel.dailyMenuState.collectAsStateWithLifecycle()
  val weeklyState by viewModel.weeklyMenuState.collectAsStateWithLifecycle()
  val templates by viewModel.templates.collectAsStateWithLifecycle()
  val feedbacks by viewModel.feedbacksForSelectedDate.collectAsStateWithLifecycle()
  val editMealDialogState by viewModel.editMealDialogState.collectAsStateWithLifecycle()
  val copyDayDialogState by viewModel.copyDayDialogState.collectAsStateWithLifecycle()
  val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }

  var showTemplatesDialog by remember { mutableStateOf(false) }
  var showSaveTemplateDialog by remember { mutableStateOf(false) }
  var showCustomTemplateDialog by remember { mutableStateOf(false) }
  var templateToEdit by remember { mutableStateOf<MenuTemplate?>(null) }
  var templateToDelete by remember { mutableStateOf<MenuTemplate?>(null) }
  var showRestoreTemplatesConfirm by remember { mutableStateOf(false) }
  var showCopyWeekDialog by remember { mutableStateOf(false) }
  var feedbackMealTarget by remember { mutableStateOf<Pair<MealType, String?>?>(null) }

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
          // Templates button
          IconButton(
            onClick = { showTemplatesDialog = true },
            modifier = Modifier.testTag("btn_menu_templates")
          ) {
            Icon(Icons.Default.Bookmark, contentDescription = "Menu Templates")
          }

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
          } else if (menuMode == MenuMode.WEEKLY) {
            FilledTonalButton(
              onClick = { showCopyWeekDialog = true },
              modifier = Modifier.testTag("btn_copy_week_menu")
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copy Week")
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
            feedbacks = feedbacks,
            onPreviousDay = { viewModel.selectPreviousDay() },
            onNextDay = { viewModel.selectNextDay() },
            onToday = { viewModel.selectToday() },
            onSelectDate = { viewModel.setDate(it) },
            onSaveAsTemplate = { showSaveTemplateDialog = true },
            onRateMeal = { mealType, dish -> feedbackMealTarget = Pair(mealType, dish) },
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

  // Meal Rating & Feedback Dialog
  feedbackMealTarget?.let { (mealType, dish) ->
    MealFeedbackDialog(
      mealType = mealType,
      date = dailyState.date,
      dishName = dish,
      onDismiss = { feedbackMealTarget = null },
      onSubmit = { rating, comment, employeeName ->
        viewModel.submitMealRating(mealType, rating, comment, employeeName)
        feedbackMealTarget = null
      }
    )
  }

  // Edit Meal Dialog
  editMealDialogState?.let { dialogState ->
    EditMealDialog(
      mealType = dialogState.mealType,
      date = dialogState.date,
      initialDescription = dialogState.currentDescription,
      onDismiss = { viewModel.closeEditMealDialog() },
      onSave = { newDesc ->
        viewModel.saveMealMenu(dialogState.date, dialogState.mealType, newDesc)
      }
    )
  }

  // Copy Day Dialog
  copyDayDialogState?.let { dialogState ->
    CopyDayDialog(
      state = dialogState,
      onTargetDateChange = { viewModel.setCopyTargetDate(it) },
      onConfirm = { viewModel.confirmCopyMenu() },
      onDismiss = { viewModel.closeCopyDayDialog() }
    )
  }

  // Save Today as Template Dialog
  if (showSaveTemplateDialog) {
    var templateName by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showSaveTemplateDialog = false },
      title = { Text("Save Menu as Template", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (!dailyState.hasAnyMeal) {
            Card(
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                  Text(
                    "No meals planned for ${dailyState.formattedDate}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Bold
                  )
                }
                Text(
                  "You cannot save an empty day as a template. Please add dishes to this day first, or create a custom template directly.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
              }
            }
          } else {
            Text(
              "Save meals from ${dailyState.formattedDate} as a reusable template:",
              style = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
              value = templateName,
              onValueChange = { templateName = it },
              label = { Text("Template Name (e.g. North Indian Feast)") },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_template_name")
            )

            // Preview of meals being saved
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Template Contents Preview:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(
                  "☕ Breakfast: ${dailyState.breakfast?.description ?: "—"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  "🍲 Lunch: ${dailyState.lunch?.description ?: "—"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  "🌙 Dinner: ${dailyState.dinner?.description ?: "—"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      },
      confirmButton = {
        if (dailyState.hasAnyMeal) {
          Button(
            onClick = {
              if (templateName.isNotBlank()) {
                viewModel.saveCurrentDayAsTemplate(templateName)
                showSaveTemplateDialog = false
              }
            },
            enabled = templateName.isNotBlank(),
            modifier = Modifier.testTag("confirm_save_template_button")
          ) {
            Text("Save Template")
          }
        } else {
          Button(
            onClick = {
              showSaveTemplateDialog = false
              showCustomTemplateDialog = true
            }
          ) {
            Text("Create Custom Template")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showSaveTemplateDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Create Custom Template Dialog
  if (showCustomTemplateDialog) {
    var name by remember { mutableStateOf("") }
    var breakfast by remember { mutableStateOf("") }
    var lunch by remember { mutableStateOf("") }
    var dinner by remember { mutableStateOf("") }

    val isValid = name.isNotBlank() && (breakfast.isNotBlank() || lunch.isNotBlank() || dinner.isNotBlank())

    AlertDialog(
      onDismissRequest = { showCustomTemplateDialog = false },
      title = { Text("Create Menu Template", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Template Name *") },
            placeholder = { Text("e.g. South Indian Special") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = breakfast,
            onValueChange = { breakfast = it },
            label = { Text("Breakfast Menu") },
            placeholder = { Text("e.g. Idli, Vada, Sambar & Tea") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = lunch,
            onValueChange = { lunch = it },
            label = { Text("Lunch Menu") },
            placeholder = { Text("e.g. Dal Tadka, Rice, Roti & Salad") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = dinner,
            onValueChange = { dinner = it },
            label = { Text("Dinner Menu") },
            placeholder = { Text("e.g. Paneer Bhurji, Phulka & Rice") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            "* Specify at least one meal to save this template.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (isValid) {
              viewModel.createCustomTemplate(name, breakfast, lunch, dinner)
              showCustomTemplateDialog = false
            }
          },
          enabled = isValid
        ) {
          Text("Create Template")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCustomTemplateDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Edit Template Dialog
  templateToEdit?.let { target ->
    var name by remember(target.id) { mutableStateOf(target.templateName) }
    var breakfast by remember(target.id) { mutableStateOf(target.breakfast ?: "") }
    var lunch by remember(target.id) { mutableStateOf(target.lunch ?: "") }
    var dinner by remember(target.id) { mutableStateOf(target.dinner ?: "") }

    val isValid = name.isNotBlank() && (breakfast.isNotBlank() || lunch.isNotBlank() || dinner.isNotBlank())

    AlertDialog(
      onDismissRequest = { templateToEdit = null },
      title = { Text("Edit Menu Template", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Template Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = breakfast,
            onValueChange = { breakfast = it },
            label = { Text("Breakfast Menu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = lunch,
            onValueChange = { lunch = it },
            label = { Text("Lunch Menu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = dinner,
            onValueChange = { dinner = it },
            label = { Text("Dinner Menu") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            "* Specify at least one meal to save changes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (isValid) {
              viewModel.updateTemplate(target.id, name, breakfast, lunch, dinner)
              templateToEdit = null
            }
          },
          enabled = isValid
        ) {
          Text("Save Changes")
        }
      },
      dismissButton = {
        TextButton(onClick = { templateToEdit = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Template Confirmation Dialog
  templateToDelete?.let { target ->
    AlertDialog(
      onDismissRequest = { templateToDelete = null },
      title = { Text("Delete Template?", fontWeight = FontWeight.Bold) },
      text = {
        Text("Are you sure you want to delete template '${target.templateName}'?")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteTemplate(target)
            templateToDelete = null
          },
          colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          )
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { templateToDelete = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Restore Default Templates Confirmation Dialog
  if (showRestoreTemplatesConfirm) {
    AlertDialog(
      onDismissRequest = { showRestoreTemplatesConfirm = false },
      title = { Text("Restore Default Presets?", fontWeight = FontWeight.Bold) },
      text = {
        Text("This will load 7 curated standard meal presets (North Indian, South Indian, Weekend Feast, Light & Healthy, Punjabi Dhaba, Chinese, Quick Working Day) into your templates collection.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.restoreDefaultTemplates()
            showRestoreTemplatesConfirm = false
          }
        ) {
          Text("Restore Presets")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRestoreTemplatesConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Templates Management Dialog
  if (showTemplatesDialog) {
    AlertDialog(
      onDismissRequest = { showTemplatesDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Menu Templates (${templates.size})", fontWeight = FontWeight.Bold)
          Row {
            IconButton(
              onClick = {
                showCustomTemplateDialog = true
              }
            ) {
              Icon(Icons.Default.Add, contentDescription = "New Custom Template")
            }
            IconButton(
              onClick = {
                showRestoreTemplatesConfirm = true
              }
            ) {
              Icon(Icons.Default.RestartAlt, contentDescription = "Restore Default Presets")
            }
          }
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Quick actions row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            OutlinedButton(
              onClick = { showCustomTemplateDialog = true },
              modifier = Modifier.weight(1f),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = {
                showSaveTemplateDialog = true
              },
              modifier = Modifier.weight(1.3f),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save Current", style = MaterialTheme.typography.labelMedium)
            }
            OutlinedButton(
              onClick = { showRestoreTemplatesConfirm = true },
              modifier = Modifier.weight(1.3f),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Presets", style = MaterialTheme.typography.labelMedium)
            }
          }

          if (templates.isEmpty()) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  Icons.Default.Bookmark,
                  contentDescription = null,
                  modifier = Modifier.size(36.dp),
                  tint = MaterialTheme.colorScheme.primary
                )
                Text(
                  "No templates saved yet",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  "Load standard mess presets to quickly plan meals with one tap.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                  onClick = { viewModel.restoreDefaultTemplates() }
                ) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Load Standard Presets")
                }
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(templates, key = { it.id }) { tpl ->
                val hasAny = !tpl.breakfast.isNullOrBlank() || !tpl.lunch.isNullOrBlank() || !tpl.dinner.isNullOrBlank()

                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = tpl.templateName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                      )
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                          onClick = { templateToEdit = tpl },
                          modifier = Modifier.size(28.dp)
                        ) {
                          Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Template",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                        IconButton(
                          onClick = { templateToDelete = tpl },
                          modifier = Modifier.size(28.dp)
                        ) {
                          Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Template",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                          )
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!hasAny) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                      ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Text(
                          "Empty template — tap Edit to add meals",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.error
                        )
                      }
                    } else {
                      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                          text = "☕ Breakfast: ${tpl.breakfast ?: "—"}",
                          style = MaterialTheme.typography.bodySmall,
                          color = if (tpl.breakfast.isNullOrBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                          text = "🍲 Lunch: ${tpl.lunch ?: "—"}",
                          style = MaterialTheme.typography.bodySmall,
                          color = if (tpl.lunch.isNullOrBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                          text = "🌙 Dinner: ${tpl.dinner ?: "—"}",
                          style = MaterialTheme.typography.bodySmall,
                          color = if (tpl.dinner.isNullOrBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                      onClick = {
                        viewModel.applyTemplate(tpl.id)
                        showTemplatesDialog = false
                      },
                      enabled = hasAny,
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Text("Apply to ${dailyState.formattedDate}")
                    }
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showTemplatesDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // Copy Week Dialog
  if (showCopyWeekDialog) {
    var targetMonday by remember { mutableStateOf(MenuViewModel.addDays(weeklyState.startMonday, 7)) }
    AlertDialog(
      onDismissRequest = { showCopyWeekDialog = false },
      title = { Text("Copy Week Menu Plan", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Copy entire week's menu (${weeklyState.formattedWeekRange}) to the following week starting on Monday:")
          OutlinedTextField(
            value = targetMonday,
            onValueChange = { targetMonday = it },
            label = { Text("Target Monday (yyyy-MM-dd)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.copyWeekMenu(targetMonday)
            showCopyWeekDialog = false
          }
        ) {
          Text("Copy Entire Week")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCopyWeekDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
private fun DailyMenuView(
  state: DailyMenuState,
  feedbacks: List<com.example.data.entity.MealFeedback> = emptyList(),
  onPreviousDay: () -> Unit,
  onNextDay: () -> Unit,
  onToday: () -> Unit,
  onSelectDate: (String) -> Unit,
  onSaveAsTemplate: () -> Unit = {},
  onRateMeal: (MealType, String?) -> Unit = { _, _ -> },
  onEditMeal: (MealType, String) -> Unit
) {
  var showDatePicker by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Date Navigator Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("menu_date_navigator"),
      shape = RoundedCornerShape(16.dp),
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
          modifier = Modifier.testTag("btn_menu_prev_day")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable { showDatePicker = true }
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = state.formattedDate,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag("menu_selected_date_label")
            )
          }

          if (!state.isToday) {
            Spacer(modifier = Modifier.height(2.dp))
            OutlinedButton(
              onClick = onToday,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier
                .height(24.dp)
                .testTag("btn_menu_today")
            ) {
              Text("Go to Today", style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        IconButton(
          onClick = onNextDay,
          modifier = Modifier.testTag("btn_menu_next_day")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (state.hasAnyMeal) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(
          onClick = onSaveAsTemplate,
          modifier = Modifier.testTag("btn_save_as_template")
        ) {
          Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save as Template", style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

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
          .testTag("daily_meals_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          val mealFeedbacks = feedbacks.filter { it.mealType == MealType.BREAKFAST }
          MealCard(
            mealType = MealType.BREAKFAST,
            menu = state.breakfast,
            feedbacks = mealFeedbacks,
            icon = Icons.Default.FreeBreakfast,
            onRate = { onRateMeal(MealType.BREAKFAST, state.breakfast?.description) },
            onEdit = { onEditMeal(MealType.BREAKFAST, state.breakfast?.description ?: "") }
          )
        }
        item {
          val mealFeedbacks = feedbacks.filter { it.mealType == MealType.LUNCH }
          MealCard(
            mealType = MealType.LUNCH,
            menu = state.lunch,
            feedbacks = mealFeedbacks,
            icon = Icons.Default.LunchDining,
            onRate = { onRateMeal(MealType.LUNCH, state.lunch?.description) },
            onEdit = { onEditMeal(MealType.LUNCH, state.lunch?.description ?: "") }
          )
        }
        item {
          val mealFeedbacks = feedbacks.filter { it.mealType == MealType.DINNER }
          MealCard(
            mealType = MealType.DINNER,
            menu = state.dinner,
            feedbacks = mealFeedbacks,
            icon = Icons.Default.DinnerDining,
            onRate = { onRateMeal(MealType.DINNER, state.dinner?.description) },
            onEdit = { onEditMeal(MealType.DINNER, state.dinner?.description ?: "") }
          )
        }
      }
    }
  }

  if (showDatePicker) {
    DatePickerModal(
      initialDateString = state.date,
      onDateSelected = {
        onSelectDate(it)
        showDatePicker = false
      },
      onDismiss = { showDatePicker = false }
    )
  }
}

@Composable
private fun MealCard(
  mealType: MealType,
  menu: Menu?,
  feedbacks: List<com.example.data.entity.MealFeedback> = emptyList(),
  icon: ImageVector,
  onRate: () -> Unit = {},
  onEdit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasMenu = menu != null && menu.description.isNotBlank()
  val avgRating = if (feedbacks.isNotEmpty()) feedbacks.map { it.rating }.average() else null

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("meal_card_${mealType.name.lowercase()}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (hasMenu) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
      else MaterialTheme.colorScheme.surface
    ),
    border = BorderStroke(
      width = 1.dp,
      color = if (hasMenu) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
      else MaterialTheme.colorScheme.outlineVariant
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
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = mealType.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          if (avgRating != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(androidx.compose.ui.graphics.Color(0xFFFFF8E1))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                "★ ${String.format(java.util.Locale.US, "%.1f", avgRating)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color(0xFFF57F17)
              )
              Text(
                " (${feedbacks.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (hasMenu) {
            TextButton(
              onClick = onRate,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = androidx.compose.ui.graphics.Color(0xFFFFB300))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Rate", style = MaterialTheme.typography.labelMedium)
            }
          }

          IconButton(
            onClick = onEdit,
            modifier = Modifier.testTag("btn_edit_${mealType.name.lowercase()}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit ${mealType.displayName}",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (hasMenu) {
        Text(
          text = menu.description,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.testTag("menu_desc_${mealType.name.lowercase()}")
        )

        // Show recent review comments if available
        if (feedbacks.any { !it.comment.isNullOrBlank() }) {
          Spacer(modifier = Modifier.height(8.dp))
          val latestComment = feedbacks.firstOrNull { !it.comment.isNullOrBlank() }
          latestComment?.let { fb ->
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("💬 \"${fb.comment}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                fb.employeeName?.let { name ->
                  Text(" — $name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }
        }
      } else {
        Text(
          text = "No menu planned yet. Tap edit to enter items.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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
  onSelectDay: (String) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    // Week Navigator Header
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("weekly_date_navigator"),
      shape = RoundedCornerShape(16.dp),
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
            fontWeight = FontWeight.Bold,
            color = if (dayItem.isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = dayItem.formattedDate,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (dayItem.isToday) {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = "TODAY",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (!dayItem.hasContent) {
        Text(
          text = "No meals planned",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
      } else {
        if (dayItem.breakfast.isNotBlank()) {
          WeeklyMealRow(meal = "B", description = dayItem.breakfast)
        }
        if (dayItem.lunch.isNotBlank()) {
          WeeklyMealRow(meal = "L", description = dayItem.lunch)
        }
        if (dayItem.dinner.isNotBlank()) {
          WeeklyMealRow(meal = "D", description = dayItem.dinner)
        }
      }
    }
  }
}

@Composable
private fun WeeklyMealRow(meal: String, description: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
    verticalAlignment = Alignment.Top
  ) {
    Text(
      text = "$meal: ",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary
    )
    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun EditMealDialog(
  mealType: MealType,
  date: String,
  initialDescription: String,
  onDismiss: () -> Unit,
  onSave: (String) -> Unit
) {
  var description by remember { mutableStateOf(initialDescription) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Plan ${mealType.displayName}",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column {
        Text(
          text = "Date: $date",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Menu items & description") },
          placeholder = { Text("e.g., Dal Makhani, Paneer Butter Masala, Roti, Rice") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_meal_description"),
          minLines = 3,
          maxLines = 6
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(description) },
        modifier = Modifier.testTag("btn_save_meal_dialog")
      ) {
        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
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
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  var showTargetPicker by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Copy Day's Menu", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column {
        Text(
          text = "Source: ${state.sourceDate}",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = state.sourceSummary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Copy to Target Date:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedCard(
          onClick = { showTargetPicker = true },
          modifier = Modifier
            .fillMaxWidth()
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
