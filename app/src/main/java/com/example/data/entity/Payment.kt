package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "payments",
  foreignKeys = [
    ForeignKey(
      entity = Employee::class,
      parentColumns = ["id"],
      childColumns = ["employeeId"],
      onDelete = ForeignKey.RESTRICT
    )
  ],
  indices = [
    Index(value = ["employeeId"]),
    Index(value = ["month"]),
    Index(value = ["paymentDate"])
  ]
)
data class Payment(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val employeeId: Long,
  val month: String, // Format: "YYYY-MM"
  val amountPaise: Long,
  val paymentDate: String, // Format: "YYYY-MM-DD"
  val paymentMethod: String = "Cash", // "Cash", "UPI", "Bank Transfer", "Other"
  val notes: String? = null,
  val createdAt: Long = System.currentTimeMillis()
) {
  val amountRupees: Double
    get() = amountPaise / 100.0
}
