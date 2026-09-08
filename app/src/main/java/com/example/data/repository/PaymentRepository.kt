package com.example.data.repository

import com.example.data.dao.EmployeePaymentSummary
import com.example.data.dao.PaymentDao
import com.example.data.entity.Payment
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val paymentDao: PaymentDao) {

  fun getPaymentsForMonth(month: String): Flow<List<Payment>> =
    paymentDao.getPaymentsForMonth(month)

  fun getPaymentsForEmployee(employeeId: Long): Flow<List<Payment>> =
    paymentDao.getPaymentsForEmployee(employeeId)

  fun getPaymentsForEmployeeAndMonth(employeeId: Long, month: String): Flow<List<Payment>> =
    paymentDao.getPaymentsForEmployeeAndMonth(employeeId, month)

  fun getTotalPaidForMonth(month: String): Flow<Long> =
    paymentDao.getTotalPaidForMonth(month)

  suspend fun getTotalPaidForMonthOnce(month: String): Long =
    paymentDao.getTotalPaidForMonthOnce(month)

  fun getTotalPaidForEmployeeAndMonth(employeeId: Long, month: String): Flow<Long> =
    paymentDao.getTotalPaidForEmployeeAndMonth(employeeId, month)

  suspend fun getTotalPaidForEmployeeAndMonthOnce(employeeId: Long, month: String): Long =
    paymentDao.getTotalPaidForEmployeeAndMonthOnce(employeeId, month)

  fun getEmployeePaymentSummariesForMonth(month: String): Flow<List<EmployeePaymentSummary>> =
    paymentDao.getEmployeePaymentSummariesForMonth(month)

  fun getAllPayments(): Flow<List<Payment>> =
    paymentDao.getAllPayments()

  suspend fun recordPayment(
    employeeId: Long,
    month: String,
    amountPaise: Long,
    paymentDate: String,
    paymentMethod: String = "Cash",
    notes: String? = null
  ): Result<Long> {
    if (employeeId <= 0) return Result.failure(IllegalArgumentException("Invalid employee ID"))
    if (month.isBlank()) return Result.failure(IllegalArgumentException("Month cannot be blank"))
    if (amountPaise <= 0L) return Result.failure(IllegalArgumentException("Payment amount must be greater than zero"))
    if (paymentDate.isBlank()) return Result.failure(IllegalArgumentException("Payment date cannot be blank"))

    val payment = Payment(
      employeeId = employeeId,
      month = month,
      amountPaise = amountPaise,
      paymentDate = paymentDate,
      paymentMethod = paymentMethod,
      notes = notes?.trim()?.ifBlank { null }
    )
    return try {
      val id = paymentDao.insert(payment)
      Result.success(id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updatePayment(payment: Payment): Result<Unit> {
    if (payment.amountPaise <= 0L) {
      return Result.failure(IllegalArgumentException("Payment amount must be greater than zero"))
    }
    return try {
      paymentDao.update(payment)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deletePayment(payment: Payment) = paymentDao.delete(payment)

  suspend fun deletePaymentById(id: Long) = paymentDao.deleteById(id)
}
