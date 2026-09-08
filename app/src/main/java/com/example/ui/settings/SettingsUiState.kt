package com.example.ui.settings

import com.example.data.repository.BackupValidationResult

data class SettingsUiState(
  val messName: String = "Office Mess",
  val managerName: String = "Mess Manager",
  val managerPhone: String = "",
  val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
  val remindAttendance: Boolean = true,
  val remindMenu: Boolean = true,
  val remindPayment: Boolean = true,
  val lastBackupTimestamp: Long = 0L,
  val pendingRestoreValidation: BackupValidationResult? = null,
  val isLoading: Boolean = false,
  val userMessage: String? = null
)
