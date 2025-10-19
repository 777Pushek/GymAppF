package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import com.example.gymappfrontendui.db.relationships.WorkoutExerciseWithSets

@Dao
interface WorkoutExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkoutExercises(workoutExercises: List<WorkoutExercise>): List<Long>

    @Update
    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise)

    @Delete
    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExercise)

    @Query("SELECT * FROM workout_exercises WHERE workout_exercise_id = :id")
    fun getWorkoutExerciseById(id: Int): Flow<WorkoutExercise>

    @Query("SELECT * FROM workout_exercises")
    fun getAllWorkoutExercises(): Flow<List<WorkoutExercise>>

    @Transaction
    @Query("SELECT * FROM workout_exercises WHERE workout_exercise_id = :id")
    fun getWorkoutExerciseWithSets(id: Int): Flow<WorkoutExerciseWithSets>

    @Transaction
    @Query("SELECT * FROM workout_exercises")
    fun getAllWorkoutExercisesWithSets(): Flow<List<WorkoutExerciseWithSets>>
}