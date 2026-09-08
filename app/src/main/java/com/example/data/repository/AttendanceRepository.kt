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
