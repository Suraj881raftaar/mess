package com.example.util

import com.example.data.entity.Employee
import com.example.ui.reports.EmployeeReportItem
import com.example.ui.reports.ReportUiState
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportExportUtilsTest {

  @Test
  fun testGenerateCsvContent_containsHeadersAndEmployeeData() {
    val emp = Employee(
      id = 1L,
      employeeCode = "EMP001",
      name = "Rahul Sharma",
      department = "IT",
      phone = "9876543210",
      isActive = true,
      createdAt = 1000L
    )
    val item = EmployeeReportItem(
      employee = emp,
      breakfastCount = 10,
      lunchCount = 20,
      dinnerCount = 12,
      totalMeals = 42,
      payablePaise = 84000L,
      payableRupees = 840.0
    )
    val state = ReportUiState(
      selectedMonth = "2026-09",
      formattedMonth = "September 2026",
      totalExpensesPaise = 4200000L,
      totalMeals = 2100,
      breakfastCount = 500,
      lunchCount = 1000,
      dinnerCount = 600,
      activeEmployeeCount = 50,
      costPerMealRupees = 20.0,
      employeeBills = listOf(item),
      isLoading = false
    )

    val csv = ReportExportUtils.generateCsvContent(state)
    assertTrue(csv.contains("OFFICE MESS MANAGER - MONTHLY REPORT"))
    assertTrue(csv.contains("September 2026"))
    assertTrue(csv.contains("Total Active Employees,50"))
    assertTrue(csv.contains("Total Meals Served,2100"))
    assertTrue(csv.contains("Cost Per Meal,INR 20.00"))
    assertTrue(csv.contains("EMP001,Rahul Sharma,IT,Active,10,20,12,42,20.00,840.00"))
  }

  @Test
  fun testGenerateTextSummary_containsFormattedSummary() {
    val emp = Employee(
      id = 2L,
      employeeCode = "EMP002",
      name = "Amit Patil",
      department = "Accounts",
      isActive = true
    )
    val item = EmployeeReportItem(
      employee = emp,
      breakfastCount = 5,
      lunchCount = 10,
      dinnerCount = 5,
      totalMeals = 20,
      payablePaise = 40000L,
      payableRupees = 400.0
    )
    val state = ReportUiState(
      selectedMonth = "2026-09",
      formattedMonth = "September 2026",
      totalExpensesPaise = 4200000L,
      totalMeals = 2100,
      breakfastCount = 500,
      lunchCount = 1000,
      dinnerCount = 600,
      activeEmployeeCount = 1,
      costPerMealRupees = 20.0,
      employeeBills = listOf(item),
      isLoading = false
    )

    val text = ReportExportUtils.generateTextSummary(state)
    assertTrue(text.contains("September 2026"))
    assertTrue(text.contains("Total Meals Served: 2100"))
    assertTrue(text.contains("Cost Per Meal: ₹20.00"))
    assertTrue(text.contains("EMP002 - Amit Patil"))
    assertTrue(text.contains("20 meals"))
  }
}
