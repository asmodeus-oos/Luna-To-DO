package com.luna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luna.app.data.local.entity.TaskActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskActivityDao {

    @Query("SELECT * FROM task_activities WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getActivitiesForTaskFlow(taskId: Long): Flow<List<TaskActivityEntity>>

    @Query("SELECT * FROM task_activities ORDER BY timestamp DESC LIMIT 50")
    fun getRecentActivitiesFlow(): Flow<List<TaskActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: TaskActivityEntity): Long

    @Query("DELETE FROM task_activities WHERE taskId = :taskId")
    suspend fun deleteActivitiesForTask(taskId: Long)
}
