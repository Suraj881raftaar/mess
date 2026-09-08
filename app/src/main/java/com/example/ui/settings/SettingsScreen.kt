package com.example.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: SettingsViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current

  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }
  var showDemoConfirmDialog by remember { mutableStateOf(false) }

  // Activity launchers for backup export and restore
  val exportBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    uri?.let { viewModel.exportBackup(it) }
  }

  val restoreBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let { viewModel.prepareRestore(it) }
  }

  LaunchedEffect(uiState.userMessage) {
    uiState.userMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings & Management", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("settings_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Mess Profile Card
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Default.Business,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  "Mess Profile",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
              IconButton(
                onClick = { showEditProfileDialog = true },
                modifier = Modifier.testTag("edit_mess_profile_button")
              ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = uiState.messName,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Manager: ${uiState.managerName}",
              style = MaterialTheme.typography.bodyMedium
            )
            if (uiState.managerPhone.isNotBlank()) {
              Text(
                text = "Contact: ${uiState.managerPhone}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // 2. Offline & Security Guarantee
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                "100% Offline & Private",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                "All staff records, meal logs, menus, and accounts are stored securely in local database on your device.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      }

      // 3. Backup & Restore Card
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Backup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                "Backup & Restore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              "Export all data to a single offline JSON file for safe-keeping or transfer to another device.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState.lastBackupTimestamp > 0) {
              val formattedDate = remember(uiState.lastBackupTimestamp) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(uiState.lastBackupTimestamp))
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "Last backup: $formattedDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
              )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Button(
                onClick = {
                  val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                  exportBackupLauncher.launch("Mess_Backup_$dateStr.json")
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("export_backup_button")
              ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Backup")
              }

              OutlinedButton(
                onClick = {
                  restoreBackupLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("restore_backup_button")
              ) {
                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Restore Data")
              }
            }
          }
        }
      }

      // 4. Notifications & Reminders
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                "Daily Reminders & Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Attendance Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Alert on dashboard when meals attendance is unrecorded", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = uiState.remindAttendance,
                onCheckedChange = { viewModel.toggleRemindAttendance(it) },
                modifier = Modifier.testTag("switch_remind_attendance")
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Menu Planner Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Alert when upcoming meal menu has not been planned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = uiState.remindMenu,
                onCheckedChange = { viewModel.toggleRemindMenu(it) },
                modifier = Modifier.testTag("switch_remind_menu")
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Payment Collection Alert", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Alert on dashboard when staff have unpaid balances", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = uiState.remindPayment,
                onCheckedChange = { viewModel.toggleRemindPayment(it) },
                modifier = Modifier.testTag("switch_remind_payment")
              )
            }
          }
        }
      }

      // 5. Data Tools & Testing
      item {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                "Data Management & Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
              onClick = { showDemoConfirmDialog = true },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("load_demo_data_button")
            ) {
              Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Load Realistic Demo Data")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
              onClick = { showResetConfirmDialog = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_database_button")
            ) {
              Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Clear All Data (Factory Reset)")
            }
          }
        }
      }
    }
  }

  // Dialog: Edit Profile
  if (showEditProfileDialog) {
    var name by remember { mutableStateOf(uiState.messName) }
    var manager by remember { mutableStateOf(uiState.managerName) }
    var phone by remember { mutableStateOf(uiState.managerPhone) }

    AlertDialog(
      onDismissRequest = { showEditProfileDialog = false },
      title = { Text("Edit Mess Profile", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Mess / Office Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_mess_name")
          )
          OutlinedTextField(
            value = manager,
            onValueChange = { manager = it },
            label = { Text("Manager Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_manager_name")
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Manager Phone (Optional)") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_manager_phone")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              viewModel.updateMessProfile(name, manager, phone)
              showEditProfileDialog = false
            }
          },
          modifier = Modifier.testTag("save_mess_profile_button")
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showEditProfileDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Dialog: Confirm Demo Data
  if (showDemoConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showDemoConfirmDialog = false },
      title = { Text("Load Sample Data?") },
      text = {
        Text("This will populate 12 staff members, 10 days of attendance and menus, monthly expenses, and payments for instant testing.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDemoConfirmDialog = false
            viewModel.loadDemoData()
          },
          modifier = Modifier.testTag("confirm_demo_data_button")
        ) {
          Text("Load Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDemoConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Dialog: Confirm Factory Reset
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = { Text("Reset Database?") },
      text = {
        Text("Are you sure you want to delete ALL employees, attendance history, menus, expenses, and payments? This action cannot be undone.")
      },
      confirmButton = {
        Button(
          onClick = {
            showResetConfirmDialog = false
            viewModel.resetDatabase()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          ),
          modifier = Modifier.testTag("confirm_reset_database_button")
        ) {
          Text("Delete Everything")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Dialog: Confirm Restore Validation
  uiState.pendingRestoreValidation?.let { validation ->
    AlertDialog(
      onDismissRequest = { viewModel.cancelRestore() },
      title = { Text("Confirm Backup Restore") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Backup file verified successfully! Found:")
          Text("• ${validation.employeeCount} Staff members")
          Text("• ${validation.attendanceCount} Attendance entries")
          Text("• ${validation.menuCount} Menus")
          Text("• ${validation.expenseCount} Expenses")
          Text("• ${validation.paymentCount} Payment records")
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Warning: Restoring will overwrite existing data.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { viewModel.confirmRestore() },
          modifier = Modifier.testTag("confirm_restore_button")
        ) {
          Text("Restore Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.cancelRestore() }) {
          Text("Cancel")
        }
      }
    )
  }
}
