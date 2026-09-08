package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MealType

@Entity(
  tableName = "menus",
  indices = [
    Index(value = ["date", "mealType"], unique = true)
  ]
)
data class Menu(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val date: String,
  val mealType: MealType,
  val description: String,
  val createdAt: Long = System.currentTimeMillis()
)
