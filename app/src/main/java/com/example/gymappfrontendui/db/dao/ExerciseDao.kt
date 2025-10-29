package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.relationships.ExerciseWithMuscleGroups
import com.example.gymappfrontendui.db.relationships.ExerciseWithWorkoutExercise
import com.example.gymappfrontendui.db.relationships.ExerciseWithWorkoutTemplates

@Dao
interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<Exercise>): List<Long>

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Query("DELETE FROM exercises WHERE global_id = :id")
    suspend fun deleteExerciseByGlobalId(id: Int)

    @Query("SELECT * FROM exercises WHERE exercise_id = :id")
    fun getExerciseById(id: Int): Flow<Exercise>

    @Query("SELECT global_id FROM exercises WHERE exercise_id = :id LIMIT 1")
    fun getExerciseGlobalIdById(id: Int) : Int?

    @Query("SELECT * FROM exercises WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableExercises(userId: Int?): Flow<List<Exercise>>

    @Query("SELECT muscle_group_id FROM exercises_muscle_groups WHERE exercise_id = :id")
    suspend fun getMuscleGroupIdsForExercise(id: Int): List<Int>


    @Transaction
    @Query("SELECT * FROM exercises WHERE exercise_id = :id")
    fun getExerciseWithMuscleGroups(id: Int): Flow<ExerciseWithMuscleGroups>

    @Transaction
    @Query("SELECT * FROM exercises WHERE exercise_id = :id")
    fun getExerciseWithWorkoutExercises(id: Int): Flow<ExerciseWithWorkoutExercise>

    @Transaction
    @Query("SELECT * FROM exercises WHERE exercise_id = :id")
    fun getExerciseWithWorkoutTemplates(id: Int): Flow<ExerciseWithWorkoutTemplates>

    @Transaction
    @Query("SELECT * FROM exercises WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableExercisesWithMuscleGroups(userId: Int?): Flow<List<ExerciseWithMuscleGroups>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableExercisesWithWorkoutExercises(userId: Int?): Flow<List<ExerciseWithWorkoutExercise>>

    @Transaction
    @Query("SELECT * FROM exercises WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableExercisesWithWorkoutTemplates(userId: Int?): Flow<List<ExerciseWithWorkoutTemplates>>

    @Query("SELECT COUNT(*) > 0 FROM exercises WHERE user_id = :userId")
    suspend fun hasExercisesForUser(userId: Int): Boolean

}