package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mess_settings")
data class MessSetting(
  @PrimaryKey
  val key: String,
  val value: String
)
