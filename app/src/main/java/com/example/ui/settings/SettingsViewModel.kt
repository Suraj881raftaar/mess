package com.example.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.BackupRepository
import com.example.data.repository.DataManagementRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
  private val settingsRepository: SettingsRepository,
  private val backupRepository: BackupRepository,
  private val dataManagementRepository: DataManagementRepository
) : ViewModel() {

  private val _isLoading = MutableStateFlow(false)
  private val _userMessage = MutableStateFlow<String?>(null)
  private val _pendingRestoreValidation = MutableStateFlow<com.example.data.repository.BackupValidationResult?>(null)
  private var pendingRestoreUri: Uri? = null

  val uiState: StateFlow<SettingsUiState> = combine(
    settingsRepository.getMessName(),
    settingsRepository.getManagerName(),
    settingsRepository.getManagerPhone(),
    settingsRepository.getThemeMode(),
    settingsRepository.getRemindAttendance(),
    settingsRepository.getRemindMenu(),
    settingsRepository.getRemindPayment(),
    settingsRepository.getLastBackupTimestamp(),
    _pendingRestoreValidation,
    _isLoading,
    _userMessage
  ) { args ->
    val messName = args[0] as String
    val managerName = args[1] as String
    val managerPhone = args[2] as String
    val themeMode = args[3] as String
    val remindAttendance = args[4] as Boolean
    val remindMenu = args[5] as Boolean
    val remindPayment = args[6] as Boolean
    val lastBackupTimestamp = args[7] as Long
    val pendingValidation = args[8] as com.example.data.repository.BackupValidationResult?
    val loading = args[9] as Boolean
    val message = args[10] as String?

    SettingsUiState(
      messName = messName,
      managerName = managerName,
      managerPhone = managerPhone,
      themeMode = themeMode,
      remindAttendance = remindAttendance,
      remindMenu = remindMenu,
      remindPayment = remindPayment,
      lastBackupTimestamp = lastBackupTimestamp,
      pendingRestoreValidation = pendingValidation,
      isLoading = loading,
      userMessage = message
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SettingsUiState(isLoading = true)
  )

  suspend fun updateMessProfileSync(name: String, manager: String, phone: String) {
    settingsRepository.setMessName(name)
    settingsRepository.setManagerName(manager)
    settingsRepository.setManagerPhone(phone)
    _userMessage.value = "Mess profile updated successfully"
  }

  fun updateMessProfile(name: String, manager: String, phone: String) {
    viewModelScope.launch {
      updateMessProfileSync(name, manager, phone)
    }
  }

  suspend fun updateThemeModeSync(mode: String) {
    settingsRepository.setThemeMode(mode)
  }

  fun updateThemeMode(mode: String) {
    viewModelScope.launch {
      updateThemeModeSync(mode)
    }
  }

  suspend fun toggleRemindAttendanceSync(enabled: Boolean) {
    settingsRepository.setRemindAttendance(enabled)
  }

  fun toggleRemindAttendance(enabled: Boolean) {
    viewModelScope.launch {
      toggleRemindAttendanceSync(enabled)
    }
  }

  suspend fun toggleRemindMenuSync(enabled: Boolean) {
    settingsRepository.setRemindMenu(enabled)
  }

  fun toggleRemindMenu(enabled: Boolean) {
    viewModelScope.launch {
      toggleRemindMenuSync(enabled)
    }
  }

  suspend fun toggleRemindPaymentSync(enabled: Boolean) {
    settingsRepository.setRemindPayment(enabled)
  }

  fun toggleRemindPayment(enabled: Boolean) {
    viewModelScope.launch {
      toggleRemindPaymentSync(enabled)
    }
  }

  fun exportBackup(uri: Uri) {
    viewModelScope.launch {
      _isLoading.value = true
      val result = backupRepository.writeBackupToUri(uri)
      _isLoading.value = false
      if (result.isSuccess) {
        settingsRepository.setLastBackupTimestamp(System.currentTimeMillis())
        _userMessage.value = "Backup successfully exported!"
      } else {
        _userMessage.value = "Failed to export backup: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun prepareRestore(uri: Uri) {
    viewModelScope.launch {
      _isLoading.value = true
      val validation = backupRepository.validateBackupFromUri(uri)
      _isLoading.value = false
      if (validation.isValid) {
        pendingRestoreUri = uri
        _pendingRestoreValidation.value = validation
      } else {
        _userMessage.value = "Invalid backup file: ${validation.errorMessage}"
      }
    }
  }

  fun confirmRestore() {
    val uri = pendingRestoreUri ?: return
    viewModelScope.launch {
      _isLoading.value = true
      val result = backupRepository.restoreBackupFromUri(uri)
      _isLoading.value = false
      _pendingRestoreValidation.value = null
      pendingRestoreUri = null
      if (result.isSuccess) {
        _userMessage.value = result.getOrNull() ?: "Database restored successfully!"
      } else {
        _userMessage.value = "Restore failed: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun cancelRestore() {
    _pendingRestoreValidation.value = null
    pendingRestoreUri = null
  }

  fun loadDemoData() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        dataManagementRepository.populateRealisticDemoData()
        _userMessage.value = "Sample data loaded successfully (12 Staff, 10 Days Attendance & Menus, Expenses, Payments)"
      } catch (e: Exception) {
        _userMessage.value = "Error loading sample data: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun resetDatabase() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        dataManagementRepository.resetDatabase()
        _userMessage.value = "Database has been completely cleared"
      } catch (e: Exception) {
        _userMessage.value = "Error resetting database: ${e.message}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  class Factory(
    private val settingsRepository: SettingsRepository,
    private val backupRepository: BackupRepository,
    private val dataManagementRepository: DataManagementRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
        return SettingsViewModel(
          settingsRepository,
          backupRepository,
          dataManagementRepository
        ) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
  }
}
