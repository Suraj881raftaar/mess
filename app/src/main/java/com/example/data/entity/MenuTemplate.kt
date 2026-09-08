package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_templates")
data class MenuTemplate(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val templateName: String,
  val breakfast: String? = null,
  val lunch: String? = null,
  val dinner: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)
