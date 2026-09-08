package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ExpenseCategory

enum class PantryUnit(val symbol: String) {
  KG("kg"),
  GRAM("g"),
  LITRE("L"),
  ML("ml"),
  PACKET("pkts"),
  PIECES("pcs"),
  DOZEN("dz"),
  BAG("bag"),
  CYLINDER("cyl")
}

@Entity(
  tableName = "pantry_items",
  indices = [
    Index(value = ["itemName"]),
    Index(value = ["category"])
  ]
)
data class PantryItem(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val itemName: String,
  val category: ExpenseCategory = ExpenseCategory.GROCERIES,
  val currentQuantity: Double = 0.0,
  val unit: PantryUnit = PantryUnit.KG,
  val minThreshold: Double = 2.0, // Quantity below which alert triggers
  val estimatedPricePaise: Long = 0L, // Estimated replacement cost in paise
  val isNeededOnShoppingList: Boolean = false,
  val notes: String? = null,
  val lastPurchasedDate: String? = null,
  val updatedAt: Long = System.currentTimeMillis()
)
