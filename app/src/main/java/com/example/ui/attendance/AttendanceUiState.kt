package com.example.ui.attendance

import com.example.data.entity.Employee
import com.example.data.entity.MealAttendance

/**
 * Model representing a single row in the attendance roster.
 */
data class AttendanceRowItem(
  val employee: Employee,
  val isPresent: Boolean,
  val attendanceRecord: MealAttendance? = null,
  val isHistoricalOnly: Boolean = false // True if employee is inactive but was marked in this historic record
)

sealed interface AttendanceRosterState {
  object Loading : AttendanceRosterState
  data class Empty(val message: String) : AttendanceRosterState
  data class Success(
    val items: List<AttendanceRowItem>,
    val presentCount: Int,
    val totalCount: Int,
    val hasUnsavedChanges: Boolean = false,
    val isSaving: Boolean = false
  ) : AttendanceRosterState
  data class Error(val message: String) : AttendanceRosterState
}
