package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.dto.WorkoutSetWithDate
import com.example.gymappfrontendui.db.relationships.WorkoutWithWorkoutExerciseAndSets

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

    @Query("SELECT * FROM sets WHERE workout_exercise_id = :workoutExerciseId ORDER BY position ASC") // Added ORDER BY
    fun getSetByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<Set>>

    @Query("""
        SELECT s.*, w.date as workoutDate
        FROM sets s
        INNER JOIN workout_exercises we ON s.workout_exercise_id = we.workout_exercise_id
        INNER JOIN workouts w ON we.workout_id = w.workout_id
        WHERE we.exercise_id = :exerciseId AND w.user_id = :userId
        ORDER BY w.date ASC, s.position ASC
    """)
    fun getWorkoutSetsWithDateForExerciseAndUser(exerciseId: Int, userId: Int): Flow<List<WorkoutSetWithDate>>

    @Transaction
    @Query("SELECT * FROM workouts WHERE user_id = :userId")
    fun getWorkoutsWithExercisesAndSetsForUser(userId: Int): Flow<List<WorkoutWithWorkoutExerciseAndSets>>

}