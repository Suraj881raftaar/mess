package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
  @Query("SELECT * FROM employees ORDER BY name ASC")
  fun getAllEmployees(): Flow<List<Employee>>

  @Query("SELECT * FROM employees WHERE isActive = 1 ORDER BY name ASC")
  fun getActiveEmployees(): Flow<List<Employee>>

  @Query("SELECT * FROM employees WHERE id = :id")
  fun getEmployeeById(id: Long): Flow<Employee?>

  @Query("SELECT * FROM employees WHERE id = :id")
  suspend fun getEmployeeByIdOnce(id: Long): Employee?

  @Query("SELECT * FROM employees WHERE employeeCode = :code LIMIT 1")
  suspend fun getEmployeeByCode(code: String): Employee?

  @Query(
    """
    SELECT * FROM employees 
    WHERE name LIKE '%' || :query || '%' 
       OR employeeCode LIKE '%' || :query || '%' 
       OR department LIKE '%' || :query || '%' 
    ORDER BY name ASC
    """
  )
  fun searchEmployees(query: String): Flow<List<Employee>>

  @Query("SELECT COUNT(*) FROM employees WHERE isActive = 1")
  fun getActiveCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(employee: Employee): Long

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertAll(employees: List<Employee>)

  @Update
  suspend fun update(employee: Employee)

  @Query("UPDATE employees SET isActive = :isActive WHERE id = :id")
  suspend fun setEmployeeActiveStatus(id: Long, isActive: Boolean)

  @Query("DELETE FROM employees")
  suspend fun deleteAll()
}
