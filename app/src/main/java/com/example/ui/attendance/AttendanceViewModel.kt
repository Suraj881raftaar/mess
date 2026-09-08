package com.example.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.Employee
import com.example.data.entity.MealAttendance
import com.example.data.model.MealType
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.EmployeeRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceViewModel(
  private val attendanceRepository: AttendanceRepository,
  private val employeeRepository: EmployeeRepository
) : ViewModel() {

  companion object {
    val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val DISPLAY_DATE_FORMAT = SimpleDateFormat("EEE, dd MMM yyyy", Locale.US)

    fun getTodayString(): String = DATE_FORMAT.format(Date())
  }

  private val _selectedDate = MutableStateFlow(getTodayString())
  val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

  private val _selectedMeal = MutableStateFlow(MealType.LUNCH)
  val selectedMeal: StateFlow<MealType> = _selectedMeal.asStateFlow()

  // Map of employeeId -> Boolean (present) for in-memory edits before or during save
  private val _draftAttendance = MutableStateFlow<Map<Long, Boolean>?>(null)
  val draftAttendance: StateFlow<Map<Long, Boolean>?> = _draftAttendance.asStateFlow()

  private val _isSaving = MutableStateFlow(false)
  val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  @OptIn(ExperimentalCoroutinesApi::class)
  val rosterState: StateFlow<AttendanceRosterState> = combine(
    _selectedDate,
    _selectedMeal
  ) { date, meal -> Pair(date, meal) }
    .flatMapLatest { (date, meal) ->
      combine(
        employeeRepository.allEmployees,
        attendanceRepository.getAttendanceForDateAndMeal(date, meal),
        _draftAttendance,
        _isSaving
      ) { allEmployees, savedRecords, drafts, saving ->
        buildRoster(allEmployees, savedRecords, drafts, saving)
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AttendanceRosterState.Loading
    )

  private fun buildRoster(
    allEmployees: List<Employee>,
    savedRecords: List<MealAttendance>,
    drafts: Map<Long, Boolean>?,
    isSaving: Boolean
  ): AttendanceRosterState {
    if (allEmployees.isEmpty()) {
      return AttendanceRosterState.Empty("No employees in directory. Please add employees first.")
    }

    val savedMap = savedRecords.associateBy { it.employeeId }

    // Roster inclusion rule:
    // 1. All active employees must appear.
    // 2. Any inactive employee who HAS an existing attendance record for this date & meal must also appear (historical preservation).
    val activeEmployees = allEmployees.filter { it.isActive }
    val inactiveWithRecord = allEmployees.filter { !it.isActive && savedMap.containsKey(it.id) }

    val employeesToShow = (activeEmployees + inactiveWithRecord).sortedBy { it.name }

    if (employeesToShow.isEmpty()) {
      return AttendanceRosterState.Empty("No active employees found for attendance.")
    }

    var hasUnsavedChanges = false
    val items = employeesToShow.map { emp ->
      val savedRecord = savedMap[emp.id]
      val defaultPresent = savedRecord?.present ?: false
      val effectivePresent = if (drafts != null && drafts.containsKey(emp.id)) {
        val draftVal = drafts[emp.id]!!
        if (draftVal != defaultPresent) {
          hasUnsavedChanges = true
        }
        draftVal
      } else {
        defaultPresent
      }

      AttendanceRowItem(
        employee = emp,
        isPresent = effectivePresent,
        attendanceRecord = savedRecord,
        isHistoricalOnly = !emp.isActive
      )
    }

    // If drafts is not null and has keys that differ from saved, hasUnsavedChanges is true
    if (drafts != null) {
      hasUnsavedChanges = items.any { item ->
        val saved = savedMap[item.employee.id]?.present ?: false
        item.isPresent != saved
      }
    }

    val presentCount = items.count { it.isPresent }
    return AttendanceRosterState.Success(
      items = items,
      presentCount = presentCount,
      totalCount = items.size,
      hasUnsavedChanges = hasUnsavedChanges,
      isSaving = isSaving
    )
  }

  fun setDate(date: String) {
    if (_selectedDate.value != date) {
      _selectedDate.value = date
      _draftAttendance.value = null // reset draft for new date
    }
  }

  fun selectNextDay() {
    shiftDate(1)
  }

  fun selectPreviousDay() {
    shiftDate(-1)
  }

  fun selectToday() {
    setDate(getTodayString())
  }

  private fun shiftDate(days: Int) {
    try {
      val parsed = DATE_FORMAT.parse(_selectedDate.value) ?: Date()
      val cal = Calendar.getInstance().apply {
        time = parsed
        add(Calendar.DAY_OF_YEAR, days)
      }
      setDate(DATE_FORMAT.format(cal.time))
    } catch (_: Exception) {
      setDate(getTodayString())
    }
  }

  fun setMealType(mealType: MealType) {
    if (_selectedMeal.value != mealType) {
      _selectedMeal.value = mealType
      _draftAttendance.value = null // reset draft for new meal
    }
  }

  /**
   * Toggle attendance for a specific employee.
   * If autoSave is false (default in UI batch), updates draft.
   */
  fun toggleAttendance(employeeId: Long) {
    val currentDraft = _draftAttendance.value?.toMutableMap() ?: mutableMapOf()
    val currentState = if (currentDraft.containsKey(employeeId)) {
      currentDraft[employeeId]!!
    } else {
      val currentRoster = rosterState.value as? AttendanceRosterState.Success
      currentRoster?.items?.find { it.employee.id == employeeId }?.isPresent ?: false
    }
    currentDraft[employeeId] = !currentState
    _draftAttendance.value = currentDraft
  }

  fun setEmployeePresence(employeeId: Long, isPresent: Boolean) {
    val currentDraft = _draftAttendance.value?.toMutableMap() ?: mutableMapOf()
    currentDraft[employeeId] = isPresent
    _draftAttendance.value = currentDraft
  }

  /**
   * One-tap instant toggle and save (used for direct row interactions if desired).
   */
  suspend fun toggleAndSaveDirectly(employeeId: Long): Boolean {
    val currentDraft = _draftAttendance.value?.toMutableMap() ?: mutableMapOf()
    val currentRoster = rosterState.value as? AttendanceRosterState.Success
    val item = currentRoster?.items?.find { it.employee.id == employeeId }
    val currentState = currentDraft[employeeId] ?: item?.isPresent ?: false
    val nextState = !currentState

    val date = _selectedDate.value
    val meal = _selectedMeal.value
    attendanceRepository.setAttendance(employeeId, date, meal, nextState)

    // Update draft to keep in sync
    currentDraft[employeeId] = nextState
    _draftAttendance.value = currentDraft
    return true
  }

  fun markAllPresent() {
    val currentRoster = rosterState.value as? AttendanceRosterState.Success
    if (currentRoster != null) {
      val newDraft = mutableMapOf<Long, Boolean>()
      currentRoster.items.forEach { item ->
        newDraft[item.employee.id] = true
      }
      _draftAttendance.value = newDraft
    } else {
      viewModelScope.launch {
        val allEmployees = employeeRepository.allEmployees.first()
        val newDraft = allEmployees.filter { it.isActive }.associate { it.id to true }
        _draftAttendance.value = newDraft
      }
    }
  }

  fun markAllAbsent() {
    val currentRoster = rosterState.value as? AttendanceRosterState.Success
    if (currentRoster != null) {
      val newDraft = mutableMapOf<Long, Boolean>()
      currentRoster.items.forEach { item ->
        newDraft[item.employee.id] = false
      }
      _draftAttendance.value = newDraft
    } else {
      viewModelScope.launch {
        val allEmployees = employeeRepository.allEmployees.first()
        val newDraft = allEmployees.filter { it.isActive }.associate { it.id to false }
        _draftAttendance.value = newDraft
      }
    }
  }

  suspend fun saveAttendanceSync(): Boolean {
    val date = _selectedDate.value
    val meal = _selectedMeal.value

    _isSaving.value = true
    try {
      val allEmployees = employeeRepository.allEmployees.first()
      if (allEmployees.isEmpty()) {
        return false
      }
      val savedRecords = attendanceRepository.getAttendanceForDateAndMeal(date, meal).first()
      val savedMap = savedRecords.associateBy { it.employeeId }
      val drafts = _draftAttendance.value

      val activeEmployees = allEmployees.filter { it.isActive }
      val inactiveWithRecord = allEmployees.filter { !it.isActive && savedMap.containsKey(it.id) }
      val employeesToShow = (activeEmployees + inactiveWithRecord)

      if (employeesToShow.isEmpty()) {
        return false
      }

      val recordsToSave = employeesToShow.map { emp ->
        val defaultPresent = savedMap[emp.id]?.present ?: false
        val effectivePresent = if (drafts != null && drafts.containsKey(emp.id)) {
          drafts[emp.id]!!
        } else {
          defaultPresent
        }
        Pair(emp.id, effectivePresent)
      }

      attendanceRepository.setAttendanceBatch(recordsToSave, date, meal)
      _draftAttendance.value = null // cleared because database now matches draft
      _userMessage.value = "Attendance for ${meal.displayName} saved successfully."
      return true
    } catch (e: Exception) {
      _userMessage.value = "Error saving attendance: ${e.message}"
      return false
    } finally {
      _isSaving.value = false
    }
  }

  fun saveAttendance() {
    viewModelScope.launch {
      saveAttendanceSync()
    }
  }

  fun discardChanges() {
    _draftAttendance.value = null
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  class Factory(
    private val attendanceRepository: AttendanceRepository,
    private val employeeRepository: EmployeeRepository
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
        return AttendanceViewModel(attendanceRepository, employeeRepository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
    }
  }
}
