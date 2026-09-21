package com.luna.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.luna.app.data.local.converters.LunaTypeConverters
import com.luna.app.data.local.dao.GoalDao
import com.luna.app.data.local.dao.HabitDao
import com.luna.app.data.local.dao.MissedReasonDao
import com.luna.app.data.local.dao.ProjectDao
import com.luna.app.data.local.dao.RoutineDao
import com.luna.app.data.local.dao.TaskActivityDao
import com.luna.app.data.local.dao.TaskDao
import com.luna.app.data.local.entity.GoalEntity
import com.luna.app.data.local.entity.HabitEntity
import com.luna.app.data.local.entity.MissedReasonEntity
import com.luna.app.data.local.entity.ProjectEntity
import com.luna.app.data.local.entity.RoutineEntity
import com.luna.app.data.local.entity.SubtaskEntity
import com.luna.app.data.local.entity.TaskActivityEntity
import com.luna.app.data.local.entity.TaskEntity
import com.luna.app.data.local.entity.TaskTemplateEntity

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
        MissedReasonEntity::class
    ],
    version = 7,
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

        fun getInstance(context: Context): LunaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LunaDatabase::class.java,
                    "luna_database.db"
                )
                    .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
