package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.model.MealType
import com.example.ui.reports.ReportUiState
import java.io.File
import java.io.FileWriter

object ReportExportUtils {

  /**
   * Generates a clean CSV file representing the monthly mess report
   * including summary breakdown and employee billing items.
   */
  fun generateCsvContent(state: ReportUiState): String {
    val sb = StringBuilder()

    // Title and Metadata
    sb.append("OFFICE MESS MANAGER - MONTHLY REPORT\n")
    sb.append("Month,").append(escapeCsv(state.formattedMonth.ifBlank { state.selectedMonth })).append("\n")
    sb.append("Generated On,").append(escapeCsv(java.time.LocalDate.now().toString())).append("\n\n")

    // Financial & Meal Summary Section
    sb.append("--- SUMMARY ---\n")
    sb.append("Total Active Employees,").append(state.activeEmployeeCount).append("\n")
    sb.append("Total Mess Expenses,").append("INR ").append(String.format(java.util.Locale.US, "%.2f", CurrencyUtils.paiseToRupees(state.totalExpensesPaise))).append("\n")
    sb.append("Breakfast Meals,").append(state.breakfastCount).append("\n")
    sb.append("Lunch Meals,").append(state.lunchCount).append("\n")
    sb.append("Dinner Meals,").append(state.dinnerCount).append("\n")
    sb.append("Total Meals Served,").append(state.totalMeals).append("\n")
    sb.append("Cost Per Meal,").append("INR ").append(String.format(java.util.Locale.US, "%.2f", state.costPerMealRupees)).append("\n")
    sb.append("Total Billed,").append("INR ").append(String.format(java.util.Locale.US, "%.2f", CurrencyUtils.paiseToRupees(state.totalBilledPaise))).append("\n")
    sb.append("Total Collected,").append("INR ").append(String.format(java.util.Locale.US, "%.2f", CurrencyUtils.paiseToRupees(state.totalCollectedPaise))).append("\n")
    sb.append("Total Outstanding,").append("INR ").append(String.format(java.util.Locale.US, "%.2f", CurrencyUtils.paiseToRupees(state.totalPendingPaise))).append("\n\n")

    // Employee Billing Details Table
    sb.append("--- EMPLOYEE BILLING & PAYMENT DETAILS ---\n")
    sb.append("Employee ID,Name,Department,Status,Breakfast,Lunch,Dinner,Total Meals,Cost Per Meal (INR),Payable (INR),Paid (INR),Pending (INR),Payment Status\n")

    for (item in state.employeeBills) {
      val emp = item.employee
      sb.append(escapeCsv(emp.employeeCode)).append(",")
      sb.append(escapeCsv(emp.name)).append(",")
      sb.append(escapeCsv(emp.department)).append(",")
      sb.append(if (emp.isActive) "Active" else "Inactive").append(",")
      sb.append(item.breakfastCount).append(",")
      sb.append(item.lunchCount).append(",")
      sb.append(item.dinnerCount).append(",")
      sb.append(item.totalMeals).append(",")
      sb.append(String.format(java.util.Locale.US, "%.2f", state.costPerMealRupees)).append(",")
      sb.append(String.format(java.util.Locale.US, "%.2f", item.payableRupees)).append(",")
      sb.append(String.format(java.util.Locale.US, "%.2f", CurrencyUtils.paiseToRupees(item.paidPaise))).append(",")
      sb.append(String.format(java.util.Locale.US, "%.2f", CurrencyUtils.paiseToRupees(item.pendingPaise))).append(",")
      sb.append(item.paymentStatus.name).append("\n")
    }

    return sb.toString()
  }

  /**
   * Generates a comprehensive Expense Ledger CSV
   */
  fun generateExpenseLedgerCsv(expenses: List<Expense>, formattedMonth: String): String {
    val sb = StringBuilder()
    sb.append("OFFICE MESS MANAGER - EXPENSE LEDGER ($formattedMonth)\n")
    sb.append("Date,Description,Category,Vendor,Quantity,Unit,Amount (INR),Notes\n")
    for (exp in expenses) {
      sb.append(escapeCsv(exp.date)).append(",")
      sb.append(escapeCsv(exp.description)).append(",")
      sb.append(escapeCsv(exp.category.displayName)).append(",")
      sb.append(escapeCsv(exp.vendor ?: "-")).append(",")
      sb.append(exp.quantity?.toString() ?: "").append(",")
      sb.append(escapeCsv(exp.unit ?: "")).append(",")
      sb.append(String.format(java.util.Locale.US, "%.2f", exp.amountRupees)).append(",")
      sb.append(escapeCsv(exp.notes ?: "")).append("\n")
    }
    return sb.toString()
  }

  /**
   * Generates an Attendance Matrix CSV (Staff x Dates)
   */
  fun generateAttendanceMatrixCsv(
    attendances: List<MealAttendance>,
    employees: List<Employee>,
    formattedMonth: String
  ): String {
    val sb = StringBuilder()
    sb.append("OFFICE MESS MANAGER - ATTENDANCE MATRIX ($formattedMonth)\n")
    sb.append("Employee ID,Name,Department,Date,Meal Type,Status\n")

    val empMap = employees.associateBy { it.id }
    for (att in attendances) {
      val emp = empMap[att.employeeId]
      sb.append(escapeCsv(emp?.employeeCode ?: "EMP-${att.employeeId}")).append(",")
      sb.append(escapeCsv(emp?.name ?: "Unknown")).append(",")
      sb.append(escapeCsv(emp?.department ?: "General")).append(",")
      sb.append(escapeCsv(att.date)).append(",")
      sb.append(att.mealType.displayName).append(",")
      sb.append(if (att.present) "Present" else "Absent").append("\n")
    }
    return sb.toString()
  }

  /**
   * Generates a nicely formatted plain-text summary suitable for sharing via WhatsApp, Email, or Notes.
   */
  fun generateTextSummary(state: ReportUiState): String {
    val sb = StringBuilder()
    val monthTitle = state.formattedMonth.ifBlank { state.selectedMonth }
    sb.append("🍽️ *OFFICE MESS REPORT — $monthTitle*\n")
    sb.append("────────────────────────────\n")
    sb.append("• Total Expenses: ${CurrencyUtils.formatPaise(state.totalExpensesPaise)}\n")
    sb.append("• Total Meals Served: ${state.totalMeals}\n")
    sb.append("  - Breakfast: ${state.breakfastCount}\n")
    sb.append("  - Lunch: ${state.lunchCount}\n")
    sb.append("  - Dinner: ${state.dinnerCount}\n")
    sb.append("• Cost Per Meal: ${CurrencyUtils.formatRupees(state.costPerMealRupees)}\n")
    sb.append("• Total Collected: ${CurrencyUtils.formatPaise(state.totalCollectedPaise)} / ${CurrencyUtils.formatPaise(state.totalBilledPaise)}\n")
    sb.append("• Outstanding Balance: ${CurrencyUtils.formatPaise(state.totalPendingPaise)}\n")
    sb.append("• Active Staff: ${state.activeEmployeeCount}\n\n")

    sb.append("📋 *EMPLOYEE BILLING BREAKDOWN:*\n")
    val billsWithMeals = state.employeeBills.filter { it.totalMeals > 0 }
    if (billsWithMeals.isEmpty()) {
      sb.append("No meals recorded for this month.\n")
    } else {
      for (item in billsWithMeals) {
        val emp = item.employee
        val statusTag = if (!emp.isActive) " (Inactive)" else ""
        val payTag = when (item.paymentStatus) {
          com.example.ui.reports.PaymentStatus.PAID -> " [PAID]"
          com.example.ui.reports.PaymentStatus.PARTIAL -> " [PARTIAL: Due ${CurrencyUtils.formatPaise(item.pendingPaise)}]"
          com.example.ui.reports.PaymentStatus.PENDING -> " [DUE: ${CurrencyUtils.formatPaise(item.payablePaise)}]"
          com.example.ui.reports.PaymentStatus.NO_DUES -> ""
        }
        sb.append("${emp.employeeCode} - ${emp.name}$statusTag$payTag\n")
        sb.append("   ${item.totalMeals} meals (B:${item.breakfastCount}, L:${item.lunchCount}, D:${item.dinnerCount}) = ${CurrencyUtils.formatRupees(item.payableRupees)}\n")
      }
    }
    sb.append("────────────────────────────\n")
    sb.append("Generated by Office Mess Manager")
    return sb.toString()
  }

  /**
   * Writes the CSV content to a cache file and creates a share intent with FileProvider.
   */
  fun shareReportCsv(context: Context, state: ReportUiState) {
    try {
      val reportsDir = File(context.cacheDir, "reports")
      if (!reportsDir.exists()) {
        reportsDir.mkdirs()
      }

      val fileName = "Mess_Report_${state.selectedMonth}.csv"
      val file = File(reportsDir, fileName)
      val writer = FileWriter(file)
      writer.write(generateCsvContent(state))
      writer.flush()
      writer.close()

      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Mess Report - ${state.formattedMonth.ifBlank { state.selectedMonth }}")
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, generateTextSummary(state))
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(intent, "Share Mess Report CSV")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Shares the Expense Ledger CSV
   */
  fun shareExpenseLedgerCsv(context: Context, expenses: List<Expense>, formattedMonth: String) {
    try {
      val reportsDir = File(context.cacheDir, "reports")
      if (!reportsDir.exists()) reportsDir.mkdirs()

      val fileName = "Expense_Ledger_${formattedMonth.replace(" ", "_")}.csv"
      val file = File(reportsDir, fileName)
      val writer = FileWriter(file)
      writer.write(generateExpenseLedgerCsv(expenses, formattedMonth))
      writer.flush()
      writer.close()

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Expense Ledger - $formattedMonth")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(intent, "Share Expense Ledger")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Shares the Attendance Matrix CSV
   */
  fun shareAttendanceMatrixCsv(
    context: Context,
    attendances: List<MealAttendance>,
    employees: List<Employee>,
    formattedMonth: String
  ) {
    try {
      val reportsDir = File(context.cacheDir, "reports")
      if (!reportsDir.exists()) reportsDir.mkdirs()

      val fileName = "Attendance_Matrix_${formattedMonth.replace(" ", "_")}.csv"
      val file = File(reportsDir, fileName)
      val writer = FileWriter(file)
      writer.write(generateAttendanceMatrixCsv(attendances, employees, formattedMonth))
      writer.flush()
      writer.close()

      val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "Attendance Matrix - $formattedMonth")
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
      val chooser = Intent.createChooser(intent, "Share Attendance Matrix")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Shares the plain text summary across messaging apps, email, or clipboard.
   */
  fun shareReportText(context: Context, state: ReportUiState) {
    try {
      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Mess Summary - ${state.formattedMonth.ifBlank { state.selectedMonth }}")
        putExtra(Intent.EXTRA_TEXT, generateTextSummary(state))
      }
      val chooser = Intent.createChooser(intent, "Share Mess Report")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Shares an individual employee's monthly mess slip via WhatsApp, SMS, or Email.
   */
  fun shareEmployeeBillText(
    context: Context,
    item: com.example.ui.reports.EmployeeReportItem,
    formattedMonth: String,
    costPerMealRupees: Double
  ) {
    try {
      val emp = item.employee
      val sb = StringBuilder()
      sb.append("🧾 *MESS BILL SLIP — $formattedMonth*\n")
      sb.append("────────────────────────────\n")
      sb.append("Employee: ${emp.name} (${emp.employeeCode})\n")
      sb.append("Department: ${emp.department}\n")
      sb.append("Breakfast Meals: ${item.breakfastCount}\n")
      sb.append("Lunch Meals: ${item.lunchCount}\n")
      sb.append("Dinner Meals: ${item.dinnerCount}\n")
      sb.append("Total Meals: ${item.totalMeals}\n")
      sb.append("Rate per Meal: ${CurrencyUtils.formatRupees(costPerMealRupees)}\n")
      sb.append("────────────────────────────\n")
      sb.append("Total Billed: ${CurrencyUtils.formatPaise(item.payablePaise)}\n")
      sb.append("Total Paid: ${CurrencyUtils.formatPaise(item.paidPaise)}\n")
      sb.append("*Balance Due: ${CurrencyUtils.formatPaise(item.pendingPaise)}*\n")
      sb.append("Status: ${item.paymentStatus.name}\n")
      sb.append("────────────────────────────\n")
      sb.append("Generated by Office Mess Manager")

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Mess Bill - ${emp.name} ($formattedMonth)")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
      }
      val chooser = Intent.createChooser(intent, "Share Bill Slip")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      "\"" + value.replace("\"", "\"\"") + "\""
    } else {
      value
    }
  }
}
