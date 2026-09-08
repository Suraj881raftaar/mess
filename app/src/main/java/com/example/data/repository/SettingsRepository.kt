package com.example.data.repository

import com.example.data.dao.MessSettingDao
import com.example.data.entity.MessSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val messSettingDao: MessSettingDao) {

  companion object {
    const val KEY_MESS_NAME = "mess_name"
    const val KEY_MANAGER_NAME = "manager_name"
    const val KEY_MANAGER_PHONE = "manager_phone"
    const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    const val KEY_THEME_MODE = "theme_mode" // "SYSTEM", "LIGHT", "DARK"
    const val KEY_REMIND_ATTENDANCE = "remind_attendance"
    const val KEY_REMIND_MENU = "remind_menu"
    const val KEY_REMIND_PAYMENT = "remind_payment"
    const val KEY_LAST_BACKUP_TIMESTAMP = "last_backup_timestamp"

    const val DEFAULT_MESS_NAME = "Office Mess"
    const val DEFAULT_MANAGER_NAME = "Mess Manager"
  }

  fun getMessName(): Flow<String> =
    messSettingDao.getSetting(KEY_MESS_NAME).map { it ?: DEFAULT_MESS_NAME }

  suspend fun getMessNameOnce(): String =
    messSettingDao.getSettingOnce(KEY_MESS_NAME) ?: DEFAULT_MESS_NAME

  suspend fun setMessName(name: String) =
    messSettingDao.upsertSetting(MessSetting(KEY_MESS_NAME, name.trim()))

  fun getManagerName(): Flow<String> =
    messSettingDao.getSetting(KEY_MANAGER_NAME).map { it ?: DEFAULT_MANAGER_NAME }

  suspend fun setManagerName(name: String) =
    messSettingDao.upsertSetting(MessSetting(KEY_MANAGER_NAME, name.trim()))

  fun getManagerPhone(): Flow<String> =
    messSettingDao.getSetting(KEY_MANAGER_PHONE).map { it ?: "" }

  suspend fun setManagerPhone(phone: String) =
    messSettingDao.upsertSetting(MessSetting(KEY_MANAGER_PHONE, phone.trim()))

  fun getThemeMode(): Flow<String> =
    messSettingDao.getSetting(KEY_THEME_MODE).map { it ?: "SYSTEM" }

  suspend fun setThemeMode(mode: String) =
    messSettingDao.upsertSetting(MessSetting(KEY_THEME_MODE, mode))

  fun getRemindAttendance(): Flow<Boolean> =
    messSettingDao.getSetting(KEY_REMIND_ATTENDANCE).map { it != "false" }

  suspend fun setRemindAttendance(enabled: Boolean) =
    messSettingDao.upsertSetting(MessSetting(KEY_REMIND_ATTENDANCE, enabled.toString()))

  fun getRemindMenu(): Flow<Boolean> =
    messSettingDao.getSetting(KEY_REMIND_MENU).map { it != "false" }

  suspend fun setRemindMenu(enabled: Boolean) =
    messSettingDao.upsertSetting(MessSetting(KEY_REMIND_MENU, enabled.toString()))

  fun getRemindPayment(): Flow<Boolean> =
    messSettingDao.getSetting(KEY_REMIND_PAYMENT).map { it != "false" }

  suspend fun setRemindPayment(enabled: Boolean) =
    messSettingDao.upsertSetting(MessSetting(KEY_REMIND_PAYMENT, enabled.toString()))

  fun getLastBackupTimestamp(): Flow<Long> =
    messSettingDao.getSetting(KEY_LAST_BACKUP_TIMESTAMP).map { it?.toLongOrNull() ?: 0L }

  suspend fun setLastBackupTimestamp(timestamp: Long) =
    messSettingDao.upsertSetting(MessSetting(KEY_LAST_BACKUP_TIMESTAMP, timestamp.toString()))
}
