package com.example.data.repository

import com.example.data.database.MessDatabase
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.entity.Menu
import com.example.data.entity.MenuTemplate
import com.example.data.entity.Payment
import com.example.data.model.ExpenseCategory
import com.example.data.model.MealType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DataManagementRepository(private val db: MessDatabase) {

  suspend fun resetDatabase() {
    db.paymentDao().deleteAll()
    db.mealAttendanceDao().deleteAll()
    db.menuDao().deleteAll()
    db.expenseDao().deleteAll()
    db.employeeDao().deleteAll()
    db.menuTemplateDao().deleteAll()
  }

  suspend fun populateRealisticDemoData() {
    resetDatabase()

    // 1. Insert Employees across various departments
    val sampleEmployees = listOf(
      Employee(employeeCode = "EMP001", name = "Aarav Sharma", department = "Engineering", phone = "9876543201"),
      Employee(employeeCode = "EMP002", name = "Diya Patel", department = "Marketing", phone = "9876543202"),
      Employee(employeeCode = "EMP003", name = "Rohan Verma", department = "Sales", phone = "9876543203"),
      Employee(employeeCode = "EMP004", name = "Ananya Iyer", department = "Human Resources", phone = "9876543204"),
      Employee(employeeCode = "EMP005", name = "Vikram Malhotra", department = "Finance", phone = "9876543205"),
      Employee(employeeCode = "EMP006", name = "Neha Reddy", department = "Engineering", phone = "9876543206"),
      Employee(employeeCode = "EMP007", name = "Kabir Singh", department = "Operations", phone = "9876543207"),
      Employee(employeeCode = "EMP008", name = "Pooja Hegde", department = "Design", phone = "9876543208"),
      Employee(employeeCode = "EMP009", name = "Siddharth Rao", department = "Engineering", phone = "9876543209"),
      Employee(employeeCode = "EMP010", name = "Kavita Nair", department = "Support", phone = "9876543210"),
      Employee(employeeCode = "EMP011", name = "Arjun Kapoor", department = "Product", phone = "9876543211"),
      Employee(employeeCode = "EMP012", name = "Meera Joshi", department = "Administration", phone = "9876543212")
    )

    val insertedEmployeeIds = mutableListOf<Long>()
    for (emp in sampleEmployees) {
      val id = db.employeeDao().insert(emp)
      insertedEmployeeIds.add(id)
    }

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    // 2. Insert Menus for past 7 days, today, and upcoming 3 days
    val sampleMenuPlans = listOf(
      Triple("Poha & Masala Chai", "Dal Makhani, Jeera Rice, Paneer Butter Masala & Rotis", "Egg Curry / Malai Kofta, Steamed Rice & Phulkas"),
      Triple("Idli, Vada & Sambar", "Rajma Chawal, Aloo Gobhi & Parathas", "Mixed Veg Biryani, Mirchi Ka Salan & Boondi Raita"),
      Triple("Aloo Paratha & Fresh Curd", "Chole Bhature, Steamed Rice & Green Salad", "Paneer Bhurji, Dal Tadka, Rice & Phulkas"),
      Triple("Upma & Coconut Chutney", "Kadai Paneer, Dal Fry, Jeera Rice & Butter Naan", "Veg Pulav, Curd Rice & Roasted Papad"),
      Triple("Masala Dosa & Filter Coffee", "Chicken Curry / Shahi Paneer, Pulao & Tandoori Roti", "Sev Tamatar Sabzi, Khichdi & Kadhi"),
      Triple("Poori Bhaji & Halwa", "Kadhi Pakora, Steamed Basmati Rice & Bhindi Masala", "Mushroom Matar, Dal Palak, Rice & Chapati"),
      Triple("Bread Omelette / Veg Sandwich", "Veg Thali (Dal, Paneer, 2 Sabzi, Rice, Sweet)", "South Indian Meals (Sambar, Rasam, Poriyal, Curd)")
    )

    for (dayOffset in -10..3) {
      val date = today.plusDays(dayOffset.toLong()).format(formatter)
      val menuIndex = Math.abs(date.hashCode()) % sampleMenuPlans.size
      val plan = sampleMenuPlans[menuIndex]

      db.menuDao().upsert(Menu(date = date, mealType = MealType.BREAKFAST, description = plan.first))
      db.menuDao().upsert(Menu(date = date, mealType = MealType.LUNCH, description = plan.second))
      db.menuDao().upsert(Menu(date = date, mealType = MealType.DINNER, description = plan.third))
    }

    // 3. Insert Attendance for past 10 days up to today
    val attendances = mutableListOf<MealAttendance>()
    for (dayOffset in -10..0) {
      val date = today.plusDays(dayOffset.toLong()).format(formatter)
      for ((idx, empId) in insertedEmployeeIds.withIndex()) {
        val hash = (date + empId).hashCode()
        // Most employees attend lunch regularly, breakfast and dinner moderately
        val bPresent = (hash % 10) in 0..7
        val lPresent = (hash % 10) in 0..8
        val dPresent = if (dayOffset == 0) false else (hash % 10) in 0..5

        attendances.add(MealAttendance(employeeId = empId, date = date, mealType = MealType.BREAKFAST, present = bPresent))
        attendances.add(MealAttendance(employeeId = empId, date = date, mealType = MealType.LUNCH, present = lPresent))
        attendances.add(MealAttendance(employeeId = empId, date = date, mealType = MealType.DINNER, present = dPresent))
      }
    }
    db.mealAttendanceDao().upsertAll(attendances)

    // 4. Insert Expenses for current and previous month
    val currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"))
    val prevMonth = today.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"))

    val sampleExpenses = listOf(
      Expense(date = "$currentMonth-01", description = "Monthly Grocery Stock (Rice, Atta, Dals, Spices)", category = ExpenseCategory.GROCERIES, amountPaise = 1850000L, vendor = "Metro Wholesale", quantity = 150.0, unit = "kg"),
      Expense(date = "$currentMonth-02", description = "Fresh Vegetables & Herbs (Week 1)", category = ExpenseCategory.VEGETABLES, amountPaise = 420000L, vendor = "Local Sabzi Mandi"),
      Expense(date = "$currentMonth-03", description = "Milk & Paneer Daily Supply (Week 1)", category = ExpenseCategory.DAIRY, amountPaise = 315000L, vendor = "Amul Dairy Parlour"),
      Expense(date = "$currentMonth-04", description = "Commercial LPG Cylinders (2 Refills)", category = ExpenseCategory.GAS_FUEL, amountPaise = 360000L, vendor = "Bharat Gas Agency"),
      Expense(date = "$currentMonth-05", description = "Cook & Helper Advance Salary", category = ExpenseCategory.SALARY_LABOUR, amountPaise = 1200000L, vendor = "Mess Staff"),
      Expense(date = "$currentMonth-06", description = "Fresh Vegetables & Tomatoes (Week 2)", category = ExpenseCategory.VEGETABLES, amountPaise = 385000L, vendor = "Local Sabzi Mandi"),
      Expense(date = "$currentMonth-07", description = "Milk, Curd & Butter (Week 2)", category = ExpenseCategory.DAIRY, amountPaise = 290000L, vendor = "Amul Dairy Parlour"),
      Expense(date = "$currentMonth-08", description = "Dishwashing liquid, foil & cleaning supplies", category = ExpenseCategory.UTILITIES, amountPaise = 145000L, vendor = "Kirana Mart"),
      // Previous month expenses
      Expense(date = "$prevMonth-02", description = "Monthly Staples (Rice, Oil, Flour)", category = ExpenseCategory.GROCERIES, amountPaise = 1720000L, vendor = "Metro Wholesale"),
      Expense(date = "$prevMonth-05", description = "Vegetables & Greens", category = ExpenseCategory.VEGETABLES, amountPaise = 780000L, vendor = "Local Sabzi Mandi"),
      Expense(date = "$prevMonth-08", description = "Dairy Products Supply", category = ExpenseCategory.DAIRY, amountPaise = 620000L, vendor = "Amul"),
      Expense(date = "$prevMonth-10", description = "Commercial Gas", category = ExpenseCategory.GAS_FUEL, amountPaise = 360000L, vendor = "Bharat Gas"),
      Expense(date = "$prevMonth-15", description = "Kitchen Staff Salary", category = ExpenseCategory.SALARY_LABOUR, amountPaise = 1200000L, vendor = "Mess Staff")
    )
    db.expenseDao().insertAll(sampleExpenses)

    // 5. Insert Sample Payments for previous month & current month
    val payments = listOf(
      Payment(employeeId = insertedEmployeeIds[0], month = prevMonth, amountPaise = 245000L, paymentDate = "$prevMonth-28", paymentMethod = "UPI", notes = "GPay Ref #98124"),
      Payment(employeeId = insertedEmployeeIds[1], month = prevMonth, amountPaise = 230000L, paymentDate = "$prevMonth-29", paymentMethod = "Cash", notes = "Paid in full"),
      Payment(employeeId = insertedEmployeeIds[2], month = prevMonth, amountPaise = 210000L, paymentDate = "$prevMonth-30", paymentMethod = "UPI", notes = "PhonePe #4411"),
      Payment(employeeId = insertedEmployeeIds[3], month = currentMonth, amountPaise = 100000L, paymentDate = "$currentMonth-05", paymentMethod = "UPI", notes = "Advance mess payment"),
      Payment(employeeId = insertedEmployeeIds[4], month = currentMonth, amountPaise = 150000L, paymentDate = "$currentMonth-06", paymentMethod = "Cash", notes = "Advance token")
    )
    db.paymentDao().insertAll(payments)

    // 6. Insert Menu Templates
    val templates = MenuRepository.getDefaultTemplates()
    db.menuTemplateDao().insertAll(templates)
  }
}
