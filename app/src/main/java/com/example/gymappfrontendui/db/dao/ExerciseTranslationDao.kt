package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymappfrontendui.db.entity.ExerciseTranslation

@Dao
interface ExerciseTranslationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseTranslation(exerciseTranslation: ExerciseTranslation): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExercisesTranslations(exerciseTranslations: List<ExerciseTranslation>): List<Long>

    @Query("SELECT * FROM exercises_translations WHERE exercise_id = :exerciseId AND language_id = :languageId LIMIT 1")
    suspend fun getExerciseTranslation(exerciseId: Int, languageId: Int): ExerciseTranslation

}