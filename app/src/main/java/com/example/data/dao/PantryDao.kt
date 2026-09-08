package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PantryItem
import com.example.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: PantryItem): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<PantryItem>)

  @Update
  suspend fun update(item: PantryItem)

  @Delete
  suspend fun delete(item: PantryItem)

  @Query("DELETE FROM pantry_items WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("SELECT * FROM pantry_items ORDER BY itemName ASC")
  fun getAllPantryItems(): Flow<List<PantryItem>>

  @Query("SELECT * FROM pantry_items WHERE currentQuantity <= minThreshold OR isNeededOnShoppingList = 1 ORDER BY itemName ASC")
  fun getShoppingList(): Flow<List<PantryItem>>

  @Query("SELECT * FROM pantry_items WHERE category = :category ORDER BY itemName ASC")
  fun getPantryItemsByCategory(category: ExpenseCategory): Flow<List<PantryItem>>

  @Query("SELECT COUNT(*) FROM pantry_items WHERE currentQuantity <= minThreshold OR isNeededOnShoppingList = 1")
  fun getLowStockCount(): Flow<Int>

  @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
  suspend fun getItemByIdOnce(id: Long): PantryItem?

  @Query("UPDATE pantry_items SET currentQuantity = :newQuantity, updatedAt = :timestamp WHERE id = :id")
  suspend fun updateQuantity(id: Long, newQuantity: Double, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE pantry_items SET isNeededOnShoppingList = :needed, updatedAt = :timestamp WHERE id = :id")
  suspend fun setShoppingListFlag(id: Long, needed: Boolean, timestamp: Long = System.currentTimeMillis())
}
