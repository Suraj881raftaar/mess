package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MealType

@Entity(
  tableName = "meal_feedbacks",
  indices = [
    Index(value = ["date"]),
    Index(value = ["mealType"]),
    Index(value = ["employeeId"])
  ]
)
data class MealFeedback(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val date: String, // YYYY-MM-DD
  val mealType: MealType,
  val rating: Int, // 1 to 5 stars
  val comment: String? = null,
  val employeeName: String? = null,
  val employeeId: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
)
