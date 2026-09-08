package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ExpenseCategory

@Entity(
  tableName = "expenses",
  indices = [
    Index(value = ["date"])
  ]
)
data class Expense(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val date: String,
  val description: String,
  val category: ExpenseCategory,
  val quantity: Double? = null,
  val unit: String? = null,
  val amountPaise: Long,
  val vendor: String? = null,
  val notes: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) {
  val amountRupees: Double
    get() = amountPaise / 100.0
}
