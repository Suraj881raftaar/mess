package com.example.data.repository

import com.example.data.dao.EmployeeMealCount
import com.example.data.dao.MealAttendanceDao
import com.example.data.dao.MonthlyMealBreakdown
import com.example.data.entity.MealAttendance
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: MealAttendanceDao) {

  fun getAttendanceForDateAndMeal(date: String, mealType: MealType): Flow<List<MealAttendance>> {
    return attendanceDao.getAttendanceForDateAndMeal(date, mealType)
  }

  fun getAttendanceForDate(date: String): Flow<List<MealAttendance>> {
    return attendanceDao.getAttendanceForDate(date)
  }

  fun getAttendanceForEmployeeAndMonth(employeeId: Long, monthPrefix: String): Flow<List<MealAttendance>> {
    return attendanceDao.getAttendanceForEmployeeAndMonth(employeeId, monthPrefix)
  }

  fun getAllAttendanceForEmployee(employeeId: Long): Flow<List<MealAttendance>> {
    return attendanceDao.getAllAttendanceForEmployee(employeeId)
  }

  fun getPresentCountForDateAndMeal(date: String, mealType: MealType): Flow<Int> {
    return attendanceDao.getPresentCountForDateAndMeal(date, mealType)
  }

  suspend fun getPresentCountForDateAndMealOnce(date: String, mealType: MealType): Int {
    return attendanceDao.getPresentCountForDateAndMealOnce(date, mealType)
  }

  fun getTotalMealsForMonth(monthPrefix: String): Flow<Int> {
    return attendanceDao.getTotalMealsForMonth(monthPrefix)
  }

  fun getMonthlyMealBreakdown(monthPrefix: String): Flow<MonthlyMealBreakdown> {
    return attendanceDao.getMonthlyMealBreakdown(monthPrefix)
  }

  fun getEmployeeMealCountsForMonth(monthPrefix: String): Flow<List<EmployeeMealCount>> {
    return attendanceDao.getEmployeeMealCountsForMonth(monthPrefix)
  }

  fun getAttendanceForMonth(monthPrefix: String): Flow<List<MealAttendance>> {
    return attendanceDao.getAttendanceForMonth(monthPrefix)
  }

  fun getActiveDatesForMonth(monthPrefix: String): Flow<List<String>> {
    return attendanceDao.getActiveDatesForMonth(monthPrefix)
  }

  suspend fun copyAttendance(fromDate: String, toDate: String): Result<Int> {
    val sourceList = attendanceDao.getAttendanceForDateOnce(fromDate)
    if (sourceList.isEmpty()) {
      return Result.failure(IllegalStateException("No attendance found for date $fromDate"))
    }
    val now = System.currentTimeMillis()
    val copied = sourceList.map { src ->
      val existing = attendanceDao.getRecord(src.employeeId, toDate, src.mealType)
      existing?.copy(present = src.present, updatedAt = now) ?: MealAttendance(
        employeeId = src.employeeId,
        date = toDate,
        mealType = src.mealType,
        present = src.present,
        createdAt = now,
        updatedAt = now
      )
    }
    attendanceDao.upsertAll(copied)
    return Result.success(copied.size)
  }

  suspend fun setAllEmployeesAttendance(
    employeeIds: List<Long>,
    date: String,
    mealType: MealType,
    present: Boolean
  ) {
    val batch = employeeIds.map { it to present }
    setAttendanceBatch(batch, date, mealType)
  }

  suspend fun setAttendance(
    employeeId: Long,
    date: String,
    mealType: MealType,
    present: Boolean
  ) {
    val existing = attendanceDao.getRecord(employeeId, date, mealType)
    if (existing != null) {
      attendanceDao.upsert(
        existing.copy(
          present = present,
          updatedAt = System.currentTimeMillis()
        )
      )
    } else {
      attendanceDao.upsert(
        MealAttendance(
          employeeId = employeeId,
          date = date,
          mealType = mealType,
          present = present,
          createdAt = System.currentTimeMillis(),
          updatedAt = System.currentTimeMillis()
        )
      )
    }
  }

  suspend fun setAttendanceBatch(
    records: List<Pair<Long, Boolean>>,
    date: String,
    mealType: MealType
  ) {
    val now = System.currentTimeMillis()
    val attendances = records.map { (employeeId, present) ->
      val existing = attendanceDao.getRecord(employeeId, date, mealType)
      existing?.copy(present = present, updatedAt = now) ?: MealAttendance(
        employeeId = employeeId,
        date = date,
        mealType = mealType,
        present = present,
        createdAt = now,
        updatedAt = now
      )
    }
    attendanceDao.upsertAll(attendances)
  }
}
