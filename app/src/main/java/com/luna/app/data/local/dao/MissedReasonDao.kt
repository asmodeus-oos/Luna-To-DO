package com.luna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luna.app.data.local.entity.MissedReasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissedReasonDao {

    @Query("SELECT * FROM missed_reasons ORDER BY timestamp DESC")
    fun getAllMissedReasonsFlow(): Flow<List<MissedReasonEntity>>

    @Query("SELECT * FROM missed_reasons WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getReasonsForTaskFlow(taskId: Long): Flow<List<MissedReasonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissedReason(entry: MissedReasonEntity): Long

    @Query("DELETE FROM missed_reasons WHERE id = :id")
    suspend fun deleteMissedReason(id: Long)
}
