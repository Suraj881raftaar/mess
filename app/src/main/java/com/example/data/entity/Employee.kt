package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.DietaryPreference

@Entity(
  tableName = "employees",
  indices = [
    Index(value = ["employeeCode"], unique = true)
  ]
)
data class Employee(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val employeeCode: String,
  val name: String,
  val department: String,
  val phone: String? = null,
  val dietaryPreference: DietaryPreference = DietaryPreference.REGULAR_VEG,
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)
