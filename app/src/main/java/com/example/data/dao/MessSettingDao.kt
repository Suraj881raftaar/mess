package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.entity.MessSetting
import kotlinx.coroutines.flow.Flow

@Dao
interface MessSettingDao {
  @Query("SELECT value FROM mess_settings WHERE `key` = :key LIMIT 1")
  fun getSetting(key: String): Flow<String?>

  @Query("SELECT value FROM mess_settings WHERE `key` = :key LIMIT 1")
  suspend fun getSettingOnce(key: String): String?

  @Query("SELECT * FROM mess_settings")
  fun getAllSettings(): Flow<List<MessSetting>>

  @Query("SELECT * FROM mess_settings")
  suspend fun getAllSettingsOnce(): List<MessSetting>

  @Upsert
  suspend fun upsertSetting(setting: MessSetting)

  @Upsert
  suspend fun upsertSettings(settings: List<MessSetting>)

  @Query("DELETE FROM mess_settings WHERE `key` = :key")
  suspend fun deleteSetting(key: String)

  @Query("DELETE FROM mess_settings")
  suspend fun deleteAll()
}
