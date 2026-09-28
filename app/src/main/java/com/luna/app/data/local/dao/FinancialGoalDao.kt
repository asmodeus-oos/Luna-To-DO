package com.luna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.luna.app.data.local.entity.FinancialGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialGoalDao {
    @Query("SELECT * FROM financial_goals ORDER BY id ASC")
    fun getAllFinancialGoalsFlow(): Flow<List<FinancialGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialGoal(goal: FinancialGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinancialGoals(goals: List<FinancialGoalEntity>)

    @Update
    suspend fun updateFinancialGoal(goal: FinancialGoalEntity)

    @Delete
    suspend fun deleteFinancialGoal(goal: FinancialGoalEntity)

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteFinancialGoalById(id: Long)
}
