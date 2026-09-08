package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.EmployeeDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MealAttendanceDao
import com.example.data.dao.MenuDao
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.entity.Menu

@Database(
  entities = [
    Employee::class,
    MealAttendance::class,
    Menu::class,
    Expense::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MessDatabase : RoomDatabase() {
  abstract fun employeeDao(): EmployeeDao
  abstract fun mealAttendanceDao(): MealAttendanceDao
  abstract fun menuDao(): MenuDao
  abstract fun expenseDao(): ExpenseDao

  companion object {
    @Volatile
    private var INSTANCE: MessDatabase? = null

    const val DATABASE_NAME = "office_mess_manager.db"

    fun getDatabase(context: Context): MessDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MessDatabase::class.java,
          DATABASE_NAME
        )
          // Add migration strategy here when version bumps occur
          .build()
        INSTANCE = instance
        instance
      }
    }

    /**
     * For testing purposes: build an in-memory database
     */
    fun createInMemoryDatabase(context: Context): MessDatabase {
      return Room.inMemoryDatabaseBuilder(
        context.applicationContext,
        MessDatabase::class.java
      )
        .allowMainThreadQueries()
        .build()
    }
  }
}
