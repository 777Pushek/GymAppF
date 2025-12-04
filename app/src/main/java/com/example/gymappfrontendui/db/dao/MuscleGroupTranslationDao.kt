package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gymappfrontendui.db.entity.MuscleGroupTranslation

@Dao
interface MuscleGroupTranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscleGroupTranslation(muscleGroupTranslation: MuscleGroupTranslation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMuscleGroupTranslations(muscleGroupTranslations: List<MuscleGroupTranslation>): List<Long>

    @Query("SELECT * FROM muscle_groups_translations WHERE muscle_group_id = :muscleGroupId AND language_id = :languageId LIMIT 1")
    suspend fun getMuscleGroupTranslation(muscleGroupId: Int, languageId: Int): MuscleGroupTranslation

}