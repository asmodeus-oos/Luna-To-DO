package com.luna.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.luna.app.data.local.converters.LunaTypeConverters
import com.luna.app.data.local.dao.AccountDao
import com.luna.app.data.local.dao.BudgetDao
import com.luna.app.data.local.dao.FinancialGoalDao
import com.luna.app.data.local.dao.GoalDao
import com.luna.app.data.local.dao.HabitDao
import com.luna.app.data.local.dao.MissedReasonDao
import com.luna.app.data.local.dao.ProjectDao
import com.luna.app.data.local.dao.RoutineDao
import com.luna.app.data.local.dao.TaskActivityDao
import com.luna.app.data.local.dao.TaskDao
import com.luna.app.data.local.dao.TransactionDao
import com.luna.app.data.local.entity.AccountEntity
import com.luna.app.data.local.entity.BudgetEntity
import com.luna.app.data.local.entity.FinancialGoalEntity
import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.entity.MissedReasonEntity
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskActivityEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity
import com.luna.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        TaskEntity::class,
        SubtaskEntity::class,
        TaskTemplateEntity::class,
        ProjectEntity::class,
        HabitEntity::class,
        GoalEntity::class,
        RoutineEntity::class,
        TaskActivityEntity::class,
        MissedReasonEntity::class,
        TransactionEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        FinancialGoalEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(LunaTypeConverters::class)
abstract class LunaDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun routineDao(): RoutineDao
    abstract fun taskActivityDao(): TaskActivityDao
    abstract fun missedReasonDao(): MissedReasonDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun financialGoalDao(): FinancialGoalDao

    companion object {
        @Volatile
        private var INSTANCE: LunaDatabase? = null

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN targetPerDay INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE habits ADD COLUMN imageUri TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN alarmOnStart INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN alarmOnFinish INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN weeklyDay TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE tasks ADD COLUMN weeklyTime TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN place TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        amount REAL NOT NULL,
                        type TEXT NOT NULL DEFAULT 'EXPENSE',
                        timestamp INTEGER NOT NULL,
                        category TEXT NOT NULL DEFAULT 'General',
                        tags TEXT NOT NULL,
                        accountId INTEGER NOT NULL DEFAULT 1,
                        currency TEXT NOT NULL DEFAULT 'USD',
                        receiptUri TEXT DEFAULT NULL,
                        recurrenceRule TEXT NOT NULL DEFAULT 'NONE',
                        linkedTaskId INTEGER DEFAULT NULL,
                        linkedProjectId INTEGER DEFAULT NULL,
                        linkedGoalId INTEGER DEFAULT NULL,
                        notes TEXT DEFAULT NULL,
                        isTaxDeductible INTEGER NOT NULL DEFAULT 0,
                        isExcludedFromBudget INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        accountType TEXT NOT NULL DEFAULT 'CASH',
                        balance REAL NOT NULL DEFAULT 0.0,
                        currencyCode TEXT NOT NULL DEFAULT 'USD',
                        colorHex TEXT NOT NULL DEFAULT '#3B82F6',
                        icon TEXT NOT NULL DEFAULT 'Wallet',
                        isArchived INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        categoryName TEXT NOT NULL,
                        limitAmount REAL NOT NULL,
                        period TEXT NOT NULL DEFAULT 'MONTHLY',
                        alertThresholdPercent REAL NOT NULL DEFAULT 80.0,
                        currencyCode TEXT NOT NULL DEFAULT 'USD'
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS financial_goals (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        targetAmount REAL NOT NULL,
                        currentAmount REAL NOT NULL DEFAULT 0.0,
                        targetDate INTEGER DEFAULT NULL,
                        colorHex TEXT NOT NULL DEFAULT '#10B981',
                        icon TEXT NOT NULL DEFAULT 'Savings'
                    )
                """.trimIndent())

                db.execSQL("INSERT OR IGNORE INTO accounts (id, name, accountType, balance, currencyCode, colorHex, icon, isArchived) VALUES (1, 'Main Wallet', 'CASH', 0.0, 'USD', '#3B82F6', 'Wallet', 0)")
            }
        }

        fun getInstance(context: Context): LunaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LunaDatabase::class.java,
                    "luna_database.db"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT OR IGNORE INTO accounts (id, name, accountType, balance, currencyCode, colorHex, icon, isArchived) VALUES (1, 'Main Wallet', 'CASH', 0.0, 'USD', '#3B82F6', 'Wallet', 0)")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
