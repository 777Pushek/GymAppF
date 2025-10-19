package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.Set

@Dao
interface SetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: Set): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<Set>): List<Long>

    @Update
    suspend fun updateSet(set: Set)

    @Delete
    suspend fun deleteSet(set: Set)


    @Query("SELECT * FROM sets WHERE set_id = :setId")
    fun getSetById(setId: Int): Flow<Set>

    @Query("SELECT * FROM sets WHERE workout_exercise_id = :workoutExerciseId")
    fun getSetByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<Set>>


}