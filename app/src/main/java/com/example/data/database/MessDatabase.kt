package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.EmployeeDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.MealAttendanceDao
import com.example.data.dao.MenuDao
import com.example.data.dao.MenuTemplateDao
import com.example.data.dao.MessSettingDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.Employee
import com.example.data.entity.Expense
import com.example.data.entity.MealAttendance
import com.example.data.entity.Menu
import com.example.data.entity.MenuTemplate
import com.example.data.entity.MessSetting
import com.example.data.entity.Payment

@Database(
  entities = [
    Employee::class,
    MealAttendance::class,
    Menu::class,
    Expense::class,
    Payment::class,
    MenuTemplate::class,
    MessSetting::class
  ],
  version = 2,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MessDatabase : RoomDatabase() {
  abstract fun employeeDao(): EmployeeDao
  abstract fun mealAttendanceDao(): MealAttendanceDao
  abstract fun menuDao(): MenuDao
  abstract fun expenseDao(): ExpenseDao
  abstract fun paymentDao(): PaymentDao
  abstract fun menuTemplateDao(): MenuTemplateDao
  abstract fun messSettingDao(): MessSettingDao

  companion object {
    @Volatile
    private var INSTANCE: MessDatabase? = null

    const val DATABASE_NAME = "office_mess_manager.db"

    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Add receiptPath to expenses table
        db.execSQL("ALTER TABLE expenses ADD COLUMN receiptPath TEXT")

        // 2. Create payments table
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS payments (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            employeeId INTEGER NOT NULL,
            month TEXT NOT NULL,
            amountPaise INTEGER NOT NULL,
            paymentDate TEXT NOT NULL,
            paymentMethod TEXT NOT NULL,
            notes TEXT,
            createdAt INTEGER NOT NULL,
            FOREIGN KEY(employeeId) REFERENCES employees(id) ON DELETE RESTRICT
          )
          """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_employeeId ON payments(employeeId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_month ON payments(month)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_paymentDate ON payments(paymentDate)")

        // 3. Create menu_templates table
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS menu_templates (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            templateName TEXT NOT NULL,
            breakfast TEXT,
            lunch TEXT,
            dinner TEXT,
            createdAt INTEGER NOT NULL
          )
          """.trimIndent()
        )

        // 4. Create mess_settings table
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS mess_settings (
            `key` TEXT PRIMARY KEY NOT NULL,
            value TEXT NOT NULL
          )
          """.trimIndent()
        )
      }
    }

    fun getDatabase(context: Context): MessDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MessDatabase::class.java,
          DATABASE_NAME
        )
          .addMigrations(MIGRATION_1_2)
          .fallbackToDestructiveMigrationOnDowngrade()
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
