package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.database.MessDatabase
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.entity.Menu
import com.example.data.entity.MenuTemplate
import com.example.data.entity.MessSetting
import com.example.data.entity.Payment
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupValidationResult(
  val isValid: Boolean,
  val employeeCount: Int = 0,
  val attendanceCount: Int = 0,
  val menuCount: Int = 0,
  val expenseCount: Int = 0,
  val paymentCount: Int = 0,
  val templateCount: Int = 0,
  val errorMessage: String? = null
)

class BackupRepository(
  private val context: Context,
  private val db: MessDatabase
) {

  suspend fun createBackupJsonString(): String = withContext(Dispatchers.IO) {
    val employees = db.employeeDao().getAllEmployees().first()
    val attendances = db.mealAttendanceDao().getAllAttendances().first()
    val menus = db.menuDao().getAllMenus().first()
    val expenses = db.expenseDao().getAllExpenses().first()
    val payments = db.paymentDao().getAllPayments().first()
    val templates = db.menuTemplateDao().getAllTemplates().first()
    val settings = db.messSettingDao().getAllSettingsOnce()

    val root = JSONObject()
    root.put("version", 2)
    root.put("appName", "OfficeMessManager")
    root.put("exportedAt", System.currentTimeMillis())

    // Employees
    val empArray = JSONArray()
    for (emp in employees) {
      val obj = JSONObject()
      obj.put("id", emp.id)
      obj.put("employeeCode", emp.employeeCode)
      obj.put("name", emp.name)
      obj.put("department", emp.department)
      obj.put("phone", emp.phone ?: "")
      obj.put("isActive", emp.isActive)
      obj.put("createdAt", emp.createdAt)
      empArray.put(obj)
    }
    root.put("employees", empArray)

    // Attendances
    val attArray = JSONArray()
    for (att in attendances) {
      val obj = JSONObject()
      obj.put("id", att.id)
      obj.put("employeeId", att.employeeId)
      obj.put("date", att.date)
      obj.put("mealType", att.mealType.name)
      obj.put("present", att.present)
      obj.put("createdAt", att.createdAt)
      obj.put("updatedAt", att.updatedAt)
      attArray.put(obj)
    }
    root.put("attendances", attArray)

    // Menus
    val menuArray = JSONArray()
    for (m in menus) {
      val obj = JSONObject()
      obj.put("id", m.id)
      obj.put("date", m.date)
      obj.put("mealType", m.mealType.name)
      obj.put("description", m.description)
      obj.put("createdAt", m.createdAt)
      menuArray.put(obj)
    }
    root.put("menus", menuArray)

    // Expenses
    val expArray = JSONArray()
    for (e in expenses) {
      val obj = JSONObject()
      obj.put("id", e.id)
      obj.put("date", e.date)
      obj.put("description", e.description)
      obj.put("category", e.category.name)
      obj.put("quantity", e.quantity ?: -1.0)
      obj.put("unit", e.unit ?: "")
      obj.put("amountPaise", e.amountPaise)
      obj.put("vendor", e.vendor ?: "")
      obj.put("notes", e.notes ?: "")
      obj.put("receiptPath", e.receiptPath ?: "")
      obj.put("createdAt", e.createdAt)
      obj.put("updatedAt", e.updatedAt)
      expArray.put(obj)
    }
    root.put("expenses", expArray)

    // Payments
    val payArray = JSONArray()
    for (p in payments) {
      val obj = JSONObject()
      obj.put("id", p.id)
      obj.put("employeeId", p.employeeId)
      obj.put("month", p.month)
      obj.put("amountPaise", p.amountPaise)
      obj.put("paymentDate", p.paymentDate)
      obj.put("paymentMethod", p.paymentMethod)
      obj.put("notes", p.notes ?: "")
      obj.put("createdAt", p.createdAt)
      payArray.put(obj)
    }
    root.put("payments", payArray)

    // Menu Templates
    val tplArray = JSONArray()
    for (t in templates) {
      val obj = JSONObject()
      obj.put("id", t.id)
      obj.put("templateName", t.templateName)
      obj.put("breakfast", t.breakfast ?: "")
      obj.put("lunch", t.lunch ?: "")
      obj.put("dinner", t.dinner ?: "")
      obj.put("createdAt", t.createdAt)
      tplArray.put(obj)
    }
    root.put("menuTemplates", tplArray)

    // Settings
    val setArray = JSONArray()
    for (s in settings) {
      val obj = JSONObject()
      obj.put("key", s.key)
      obj.put("value", s.value)
      setArray.put(obj)
    }
    root.put("settings", setArray)

    root.toString(2)
  }

  suspend fun writeBackupToUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val json = createBackupJsonString()
      context.contentResolver.openOutputStream(uri)?.use { os ->
        OutputStreamWriter(os).use { writer ->
          writer.write(json)
          writer.flush()
        }
      } ?: return@withContext Result.failure(Exception("Could not open output stream"))
      Result.success(json.length)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun validateBackupFromUri(uri: Uri): BackupValidationResult = withContext(Dispatchers.IO) {
    try {
      val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).readText()
      } ?: return@withContext BackupValidationResult(false, errorMessage = "Cannot read backup file")

      val json = JSONObject(content)
      val empCount = json.optJSONArray("employees")?.length() ?: 0
      val attCount = json.optJSONArray("attendances")?.length() ?: 0
      val menuCount = json.optJSONArray("menus")?.length() ?: 0
      val expCount = json.optJSONArray("expenses")?.length() ?: 0
      val payCount = json.optJSONArray("payments")?.length() ?: 0
      val tplCount = json.optJSONArray("menuTemplates")?.length() ?: 0

      BackupValidationResult(
        isValid = true,
        employeeCount = empCount,
        attendanceCount = attCount,
        menuCount = menuCount,
        expenseCount = expCount,
        paymentCount = payCount,
        templateCount = tplCount
      )
    } catch (e: Exception) {
      BackupValidationResult(false, errorMessage = e.localizedMessage ?: "Invalid JSON format")
    }
  }

  suspend fun restoreBackupFromUri(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
    try {
      val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).readText()
      } ?: return@withContext Result.failure(Exception("Cannot read backup file"))

      val json = JSONObject(content)

      // Transactional wipe and restore
      db.paymentDao().deleteAll()
      db.mealAttendanceDao().deleteAll()
      db.menuDao().deleteAll()
      db.expenseDao().deleteAll()
      db.employeeDao().deleteAll()
      db.menuTemplateDao().deleteAll()

      // 1. Restore Employees
      val empArray = json.optJSONArray("employees") ?: JSONArray()
      val employeeMap = mutableMapOf<Long, Long>() // oldId -> newId
      for (i in 0 until empArray.length()) {
        val obj = empArray.getJSONObject(i)
        val oldId = obj.optLong("id", 0L)
        val emp = Employee(
          employeeCode = obj.getString("employeeCode"),
          name = obj.getString("name"),
          department = obj.getString("department"),
          phone = obj.optString("phone").ifBlank { null },
          isActive = obj.optBoolean("isActive", true),
          createdAt = obj.optLong("createdAt", System.currentTimeMillis())
        )
        val newId = db.employeeDao().insert(emp)
        if (oldId > 0) {
          employeeMap[oldId] = newId
        }
      }

      // 2. Restore Attendances
      val attArray = json.optJSONArray("attendances") ?: JSONArray()
      val attendances = mutableListOf<MealAttendance>()
      for (i in 0 until attArray.length()) {
        val obj = attArray.getJSONObject(i)
        val oldEmpId = obj.getLong("employeeId")
        val newEmpId = employeeMap[oldEmpId] ?: oldEmpId
        val mealTypeName = obj.getString("mealType")
        val mealType = try { MealType.valueOf(mealTypeName) } catch (e: Exception) { MealType.LUNCH }
        attendances.add(
          MealAttendance(
            employeeId = newEmpId,
            date = obj.getString("date"),
            mealType = mealType,
            present = obj.getBoolean("present"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
          )
        )
      }
      db.mealAttendanceDao().upsertAll(attendances)

      // 3. Restore Menus
      val menuArray = json.optJSONArray("menus") ?: JSONArray()
      val menus = mutableListOf<Menu>()
      for (i in 0 until menuArray.length()) {
        val obj = menuArray.getJSONObject(i)
        val mealTypeName = obj.getString("mealType")
        val mealType = try { MealType.valueOf(mealTypeName) } catch (e: Exception) { MealType.LUNCH }
        menus.add(
          Menu(
            date = obj.getString("date"),
            mealType = mealType,
            description = obj.getString("description"),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      db.menuDao().upsertAll(menus)

      // 4. Restore Expenses
      val expArray = json.optJSONArray("expenses") ?: JSONArray()
      val expenses = mutableListOf<Expense>()
      for (i in 0 until expArray.length()) {
        val obj = expArray.getJSONObject(i)
        val catName = obj.getString("category")
        val category = try { ExpenseCategory.valueOf(catName) } catch (e: Exception) { ExpenseCategory.OTHER }
        val qty = obj.optDouble("quantity", -1.0)
        expenses.add(
          Expense(
            date = obj.getString("date"),
            description = obj.getString("description"),
            category = category,
            quantity = if (qty > 0) qty else null,
            unit = obj.optString("unit").ifBlank { null },
            amountPaise = obj.getLong("amountPaise"),
            vendor = obj.optString("vendor").ifBlank { null },
            notes = obj.optString("notes").ifBlank { null },
            receiptPath = obj.optString("receiptPath").ifBlank { null },
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
          )
        )
      }
      db.expenseDao().insertAll(expenses)

      // 5. Restore Payments
      val payArray = json.optJSONArray("payments") ?: JSONArray()
      val payments = mutableListOf<Payment>()
      for (i in 0 until payArray.length()) {
        val obj = payArray.getJSONObject(i)
        val oldEmpId = obj.getLong("employeeId")
        val newEmpId = employeeMap[oldEmpId] ?: oldEmpId
        payments.add(
          Payment(
            employeeId = newEmpId,
            month = obj.getString("month"),
            amountPaise = obj.getLong("amountPaise"),
            paymentDate = obj.getString("paymentDate"),
            paymentMethod = obj.optString("paymentMethod", "Cash"),
            notes = obj.optString("notes").ifBlank { null },
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      db.paymentDao().insertAll(payments)

      // 6. Restore Menu Templates
      val tplArray = json.optJSONArray("menuTemplates") ?: JSONArray()
      val templates = mutableListOf<MenuTemplate>()
      for (i in 0 until tplArray.length()) {
        val obj = tplArray.getJSONObject(i)
        templates.add(
          MenuTemplate(
            templateName = obj.getString("templateName"),
            breakfast = obj.optString("breakfast").ifBlank { null },
            lunch = obj.optString("lunch").ifBlank { null },
            dinner = obj.optString("dinner").ifBlank { null },
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }
      db.menuTemplateDao().insertAll(templates)

      // 7. Restore Settings
      val setArray = json.optJSONArray("settings") ?: JSONArray()
      val settings = mutableListOf<MessSetting>()
      for (i in 0 until setArray.length()) {
        val obj = setArray.getJSONObject(i)
        settings.add(
          MessSetting(
            key = obj.getString("key"),
            value = obj.getString("value")
          )
        )
      }
      db.messSettingDao().upsertSettings(settings)

      Result.success("Restored ${empArray.length()} staff, ${attArray.length()} attendance entries, ${expArray.length()} expenses successfully!")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
