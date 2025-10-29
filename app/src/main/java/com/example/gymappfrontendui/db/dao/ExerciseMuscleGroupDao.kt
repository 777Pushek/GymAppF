package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup

@Dao
interface ExerciseMuscleGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseMuscleGroup(exerciseMuscleGroup: ExerciseMuscleGroup)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercisesMuscleGroups(exerciseMuscleGroups: List<ExerciseMuscleGroup>)
}