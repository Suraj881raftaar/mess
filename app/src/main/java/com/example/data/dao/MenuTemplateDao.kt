package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.MenuTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuTemplateDao {
  @Query("SELECT * FROM menu_templates ORDER BY templateName ASC")
  fun getAllTemplates(): Flow<List<MenuTemplate>>

  @Query("SELECT * FROM menu_templates WHERE id = :id")
  fun getTemplateById(id: Long): Flow<MenuTemplate?>

  @Query("SELECT * FROM menu_templates WHERE id = :id")
  suspend fun getTemplateByIdOnce(id: Long): MenuTemplate?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(template: MenuTemplate): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(templates: List<MenuTemplate>)

  @Update
  suspend fun update(template: MenuTemplate)

  @Delete
  suspend fun delete(template: MenuTemplate)

  @Query("DELETE FROM menu_templates WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM menu_templates")
  suspend fun deleteAll()
}
