package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithExercises
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithWorkouts

@Dao
interface WorkoutTemplateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkoutTemplates(workoutTemplates: List<WorkoutTemplate>): List<Long>

    @Update
    suspend fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate)

    @Delete
    suspend fun deleteWorkoutTemplate(workoutTemplate: WorkoutTemplate)

    @Query("DELETE FROM workout_templates WHERE global_id = :id")
    suspend fun deleteWorkoutTemplateByGlobalId(id: Int)

    @Query("SELECT exercise_id FROM workout_template_exercise WHERE workout_template_id = :id ORDER BY position")

    suspend fun getExerciseIdsForTemplate(id: Int): List<Int>


    @Query("SELECT * FROM workout_templates WHERE workout_template_id = :id")
    fun  getWorkoutTemplateById(id: Int): Flow<WorkoutTemplate>


    @Query("SELECT * FROM workout_templates WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableWorkoutTemplates(userId: Int?): Flow<List<WorkoutTemplate>>


    // ---------- RELATIONS ----------
    @Transaction
    @Query("SELECT * FROM workout_templates WHERE workout_template_id = :id")
    fun getTemplateWithExercises(id: Int): Flow<WorkoutTemplateWithExercises>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE workout_template_id = :id")
    fun getTemplateWithWorkouts(id: Int): Flow<WorkoutTemplateWithWorkouts>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableTemplatesWithExercises(userId: Int?): Flow<List<WorkoutTemplateWithExercises>>

    @Transaction
    @Query("SELECT * FROM workout_templates WHERE user_id = :userId OR user_id IS NULL")
    fun getAvailableTemplatesWithWorkouts(userId: Int?): Flow<List<WorkoutTemplateWithWorkouts>>
    @Query("SELECT COUNT(*) > 0 FROM workout_templates WHERE user_id = :userId")
    suspend fun hasWorkoutTemplatesForUser(userId: Int): Boolean

}