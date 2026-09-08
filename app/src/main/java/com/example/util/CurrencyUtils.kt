package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

object CurrencyUtils {
  private val formatter = DecimalFormat("₹#,##0.00")

  fun paiseToRupees(paise: Long): BigDecimal {
    return BigDecimal(paise).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
  }

  fun rupeesToPaise(rupees: Double): Long {
    return BigDecimal.valueOf(rupees)
      .multiply(BigDecimal(100))
      .setScale(0, RoundingMode.HALF_UP)
      .toLong()
  }

  fun formatPaise(paise: Long): String {
    val rupees = paiseToRupees(paise)
    return "₹" + String.format(Locale.getDefault(), "%,.2f", rupees.toDouble())
  }

  fun formatRupees(rupees: Double): String {
    return "₹" + String.format(Locale.getDefault(), "%,.2f", rupees)
  }

  fun formatRupees(rupees: BigDecimal): String {
    return "₹" + String.format(Locale.getDefault(), "%,.2f", rupees.toDouble())
  }

  /**
   * Calculates cost per meal in Rupees with precision.
   * Returns 0.0 if totalMeals == 0 (preventing division by zero).
   */
  fun calculateCostPerMealRupees(totalExpensePaise: Long, totalMeals: Int): Double {
    if (totalMeals <= 0 || totalExpensePaise <= 0L) return 0.0
    val totalRupees = paiseToRupees(totalExpensePaise)
    return totalRupees.divide(BigDecimal(totalMeals), 4, RoundingMode.HALF_UP).toDouble()
  }

  /**
   * Calculates employee payable amount in paise and rupees.
   */
  fun calculateEmployeePayable(
    employeeMeals: Int,
    costPerMealRupees: Double
  ): Double {
    if (employeeMeals <= 0 || costPerMealRupees <= 0.0) return 0.0
    val mealsBigDecimal = BigDecimal.valueOf(employeeMeals.toLong())
    val rateBigDecimal = BigDecimal.valueOf(costPerMealRupees)
    return (mealsBigDecimal * rateBigDecimal)
      .setScale(2, RoundingMode.HALF_UP)
      .toDouble()
  }
}
