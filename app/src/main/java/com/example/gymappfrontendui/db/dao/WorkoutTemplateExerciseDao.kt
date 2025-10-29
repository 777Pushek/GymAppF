package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise

@Dao
interface WorkoutTemplateExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutTemplateExercise(workoutTemplateExercise: WorkoutTemplateExercise)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorkoutTemplateExercises(workoutTemplateExercises: List<WorkoutTemplateExercise>)

}