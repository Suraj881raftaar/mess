package com.example.data.repository

import com.example.data.dao.MealFeedbackDao
import com.example.data.entity.MealFeedback
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

class MealFeedbackRepository(private val feedbackDao: MealFeedbackDao) {

  fun getFeedbacksForDate(date: String): Flow<List<MealFeedback>> =
    feedbackDao.getFeedbacksForDate(date)

  fun getFeedbacksForDateAndMeal(date: String, mealType: MealType): Flow<List<MealFeedback>> =
    feedbackDao.getFeedbacksForDateAndMeal(date, mealType)

  fun getAverageRatingForDate(date: String): Flow<Double?> =
    feedbackDao.getAverageRatingForDate(date)

  fun getAverageRatingForDateAndMeal(date: String, mealType: MealType): Flow<Double?> =
    feedbackDao.getAverageRatingForDateAndMeal(date, mealType)

  fun getRecentFeedbacks(limit: Int = 20): Flow<List<MealFeedback>> =
    feedbackDao.getRecentFeedbacks(limit)

  fun getOverallAverageRating(): Flow<Double?> =
    feedbackDao.getOverallAverageRating()

  fun getTotalFeedbackCount(): Flow<Int> =
    feedbackDao.getTotalFeedbackCount()

  suspend fun submitFeedback(
    date: String,
    mealType: MealType,
    rating: Int,
    comment: String? = null,
    employeeName: String? = null,
    employeeId: Long? = null
  ): Result<Long> {
    if (rating !in 1..5) {
      return Result.failure(IllegalArgumentException("Rating must be between 1 and 5"))
    }
    val feedback = MealFeedback(
      date = date,
      mealType = mealType,
      rating = rating,
      comment = comment?.trim()?.ifBlank { null },
      employeeName = employeeName?.trim()?.ifBlank { null },
      employeeId = employeeId
    )
    val id = feedbackDao.insert(feedback)
    return Result.success(id)
  }

  suspend fun deleteFeedback(id: Long) = feedbackDao.deleteById(id)
}
