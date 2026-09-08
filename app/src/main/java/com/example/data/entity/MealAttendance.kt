package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MealType

@Entity(
  tableName = "meal_attendances",
  foreignKeys = [
    ForeignKey(
      entity = Employee::class,
      parentColumns = ["id"],
      childColumns = ["employeeId"],
      onDelete = ForeignKey.RESTRICT
    )
  ],
  indices = [
    Index(value = ["employeeId", "date", "mealType"], unique = true),
    Index(value = ["employeeId"]),
    Index(value = ["date", "mealType"])
  ]
)
data class MealAttendance(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val employeeId: Long,
  val date: String,
  val mealType: MealType,
  val present: Boolean,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
