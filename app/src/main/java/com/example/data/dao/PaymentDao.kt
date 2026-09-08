package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.Payment
import kotlinx.coroutines.flow.Flow

data class EmployeePaymentSummary(
  val employeeId: Long,
  val totalPaidPaise: Long,
  val paymentCount: Int
)

@Dao
interface PaymentDao {
  @Query("SELECT * FROM payments WHERE month = :month ORDER BY paymentDate DESC, id DESC")
  fun getPaymentsForMonth(month: String): Flow<List<Payment>>

  @Query("SELECT * FROM payments WHERE employeeId = :employeeId ORDER BY paymentDate DESC, id DESC")
  fun getPaymentsForEmployee(employeeId: Long): Flow<List<Payment>>

  @Query("SELECT * FROM payments WHERE employeeId = :employeeId AND month = :month ORDER BY paymentDate DESC, id DESC")
  fun getPaymentsForEmployeeAndMonth(employeeId: Long, month: String): Flow<List<Payment>>

  @Query("SELECT * FROM payments WHERE id = :id")
  fun getPaymentById(id: Long): Flow<Payment?>

  @Query("SELECT * FROM payments WHERE id = :id")
  suspend fun getPaymentByIdOnce(id: Long): Payment?

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM payments WHERE month = :month")
  fun getTotalPaidForMonth(month: String): Flow<Long>

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM payments WHERE month = :month")
  suspend fun getTotalPaidForMonthOnce(month: String): Long

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM payments WHERE employeeId = :employeeId AND month = :month")
  fun getTotalPaidForEmployeeAndMonth(employeeId: Long, month: String): Flow<Long>

  @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM payments WHERE employeeId = :employeeId AND month = :month")
  suspend fun getTotalPaidForEmployeeAndMonthOnce(employeeId: Long, month: String): Long

  @Query(
    """
    SELECT 
      employeeId, 
      COALESCE(SUM(amountPaise), 0) AS totalPaidPaise, 
      COUNT(*) AS paymentCount 
    FROM payments 
    WHERE month = :month 
    GROUP BY employeeId
    """
  )
  fun getEmployeePaymentSummariesForMonth(month: String): Flow<List<EmployeePaymentSummary>>

  @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
  fun getAllPayments(): Flow<List<Payment>>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insert(payment: Payment): Long

  @Insert(onConflict = OnConflictStrategy.ABORT)
  suspend fun insertAll(payments: List<Payment>)

  @Update
  suspend fun update(payment: Payment)

  @Delete
  suspend fun delete(payment: Payment)

  @Query("DELETE FROM payments WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM payments")
  suspend fun deleteAll()
}
