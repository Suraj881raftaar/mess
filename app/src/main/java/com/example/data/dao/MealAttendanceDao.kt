package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.data.entity.MealAttendance
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

data class EmployeeMealCount(
  val employeeId: Long,
  val breakfastCount: Int,
  val lunchCount: Int,
  val dinnerCount: Int,
  val totalMeals: Int
)

data class MonthlyMealBreakdown(
  val breakfastCount: Int,
  val lunchCount: Int,
  val dinnerCount: Int,
  val totalMeals: Int
)

@Dao
interface MealAttendanceDao {
  @Query("SELECT * FROM meal_attendances WHERE date = :date AND mealType = :mealType")
  fun getAttendanceForDateAndMeal(date: String, mealType: MealType): Flow<List<MealAttendance>>

  @Query("SELECT * FROM meal_attendances WHERE date = :date")
  fun getAttendanceForDate(date: String): Flow<List<MealAttendance>>

  @Query("SELECT * FROM meal_attendances WHERE employeeId = :employeeId AND date LIKE :monthPrefix || '%' ORDER BY date DESC")
  fun getAttendanceForEmployeeAndMonth(employeeId: Long, monthPrefix: String): Flow<List<MealAttendance>>

  @Query("SELECT * FROM meal_attendances WHERE employeeId = :employeeId ORDER BY date DESC")
  fun getAllAttendanceForEmployee(employeeId: Long): Flow<List<MealAttendance>>

  @Query("SELECT * FROM meal_attendances WHERE employeeId = :employeeId AND date = :date AND mealType = :mealType LIMIT 1")
  suspend fun getRecord(employeeId: Long, date: String, mealType: MealType): MealAttendance?

  @Query("SELECT COUNT(*) FROM meal_attendances WHERE date = :date AND mealType = :mealType AND present = 1")
  fun getPresentCountForDateAndMeal(date: String, mealType: MealType): Flow<Int>

  @Query("SELECT COUNT(*) FROM meal_attendances WHERE date = :date AND mealType = :mealType AND present = 1")
  suspend fun getPresentCountForDateAndMealOnce(date: String, mealType: MealType): Int

  @Query("SELECT COUNT(*) FROM meal_attendances WHERE date LIKE :monthPrefix || '%' AND present = 1")
  fun getTotalMealsForMonth(monthPrefix: String): Flow<Int>

  @Query("SELECT COUNT(*) FROM meal_attendances WHERE employeeId = :employeeId AND date LIKE :monthPrefix || '%' AND present = 1")
  fun getPresentCountForEmployeeAndMonth(employeeId: Long, monthPrefix: String): Flow<Int>

  @Query(
    """
    SELECT 
      COALESCE(SUM(CASE WHEN mealType = 'BREAKFAST' AND present = 1 THEN 1 ELSE 0 END), 0) AS breakfastCount,
      COALESCE(SUM(CASE WHEN mealType = 'LUNCH' AND present = 1 THEN 1 ELSE 0 END), 0) AS lunchCount,
      COALESCE(SUM(CASE WHEN mealType = 'DINNER' AND present = 1 THEN 1 ELSE 0 END), 0) AS dinnerCount,
      COALESCE(SUM(CASE WHEN present = 1 THEN 1 ELSE 0 END), 0) AS totalMeals
    FROM meal_attendances
    WHERE date LIKE :monthPrefix || '%'
    """
  )
  fun getMonthlyMealBreakdown(monthPrefix: String): Flow<MonthlyMealBreakdown>

  @Query(
    """
    SELECT 
      employeeId,
      COALESCE(SUM(CASE WHEN mealType = 'BREAKFAST' AND present = 1 THEN 1 ELSE 0 END), 0) AS breakfastCount,
      COALESCE(SUM(CASE WHEN mealType = 'LUNCH' AND present = 1 THEN 1 ELSE 0 END), 0) AS lunchCount,
      COALESCE(SUM(CASE WHEN mealType = 'DINNER' AND present = 1 THEN 1 ELSE 0 END), 0) AS dinnerCount,
      COALESCE(SUM(CASE WHEN present = 1 THEN 1 ELSE 0 END), 0) AS totalMeals
    FROM meal_attendances
    WHERE date LIKE :monthPrefix || '%'
    GROUP BY employeeId
    """
  )
  fun getEmployeeMealCountsForMonth(monthPrefix: String): Flow<List<EmployeeMealCount>>

  @Upsert
  suspend fun upsert(attendance: MealAttendance): Long

  @Upsert
  suspend fun upsertAll(attendances: List<MealAttendance>)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertStrict(attendance: MealAttendance): Long

  @Update
  suspend fun update(attendance: MealAttendance)

  @Query("DELETE FROM meal_attendances WHERE date = :date AND mealType = :mealType")
  suspend fun deleteForDateAndMeal(date: String, mealType: MealType)

  @Query("DELETE FROM meal_attendances")
  suspend fun deleteAll()
}
