package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.MealFeedback
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

data class MealRatingSummary(
  val mealType: MealType,
  val averageRating: Double,
  val totalReviews: Int
)

@Dao
interface MealFeedbackDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(feedback: MealFeedback): Long

  @Query("SELECT * FROM meal_feedbacks WHERE date = :date ORDER BY createdAt DESC")
  fun getFeedbacksForDate(date: String): Flow<List<MealFeedback>>

  @Query("SELECT * FROM meal_feedbacks WHERE date = :date AND mealType = :mealType ORDER BY createdAt DESC")
  fun getFeedbacksForDateAndMeal(date: String, mealType: MealType): Flow<List<MealFeedback>>

  @Query("SELECT AVG(rating) FROM meal_feedbacks WHERE date = :date")
  fun getAverageRatingForDate(date: String): Flow<Double?>

  @Query("SELECT AVG(rating) FROM meal_feedbacks WHERE date = :date AND mealType = :mealType")
  fun getAverageRatingForDateAndMeal(date: String, mealType: MealType): Flow<Double?>

  @Query("SELECT * FROM meal_feedbacks ORDER BY createdAt DESC LIMIT :limit")
  fun getRecentFeedbacks(limit: Int = 20): Flow<List<MealFeedback>>

  @Query("SELECT AVG(rating) FROM meal_feedbacks")
  fun getOverallAverageRating(): Flow<Double?>

  @Query("SELECT COUNT(*) FROM meal_feedbacks")
  fun getTotalFeedbackCount(): Flow<Int>

  @Query("DELETE FROM meal_feedbacks WHERE id = :id")
  suspend fun deleteById(id: Long)
}
