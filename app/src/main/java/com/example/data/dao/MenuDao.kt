package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.data.entity.Menu
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {
  @Query("SELECT * FROM menus WHERE date = :date")
  fun getMenuForDate(date: String): Flow<List<Menu>>

  @Query("SELECT * FROM menus WHERE date = :date AND mealType = :mealType LIMIT 1")
  fun getMenuForDateAndMeal(date: String, mealType: MealType): Flow<Menu?>

  @Query("SELECT * FROM menus WHERE date = :date AND mealType = :mealType LIMIT 1")
  suspend fun getMenuForDateAndMealOnce(date: String, mealType: MealType): Menu?

  @Query("SELECT * FROM menus WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
  fun getMenuForDateRange(startDate: String, endDate: String): Flow<List<Menu>>

  @Query("SELECT * FROM menus ORDER BY date DESC")
  fun getAllMenus(): Flow<List<Menu>>

  @Upsert
  suspend fun upsert(menu: Menu): Long

  @Upsert
  suspend fun upsertAll(menus: List<Menu>)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertStrict(menu: Menu): Long

  @Query("DELETE FROM menus WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM menus WHERE date = :date")
  suspend fun deleteForDate(date: String)

  @Query("DELETE FROM menus")
  suspend fun deleteAll()
}
