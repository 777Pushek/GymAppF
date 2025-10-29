package com.example.gymappfrontendui.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.db.relationships.MuscleGroupWithExercises

@Dao
interface MuscleGroupDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscleGroup(muscleGroup: MuscleGroup): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMuscleGroups(muscleGroups: List<MuscleGroup>): List<Long>

    @Update
    suspend fun updateMuscleGroup(muscleGroup: MuscleGroup)

    @Delete
    suspend fun deleteMuscleGroup(muscleGroup: MuscleGroup)

    @Query("SELECT * FROM muscle_groups")
    fun getAllMuscleGroups(): Flow<List<MuscleGroup>>

    @Query("SELECT * FROM muscle_groups WHERE muscle_group_id = :id")
    fun getMuscleGroupById(id: Int): Flow<MuscleGroup>

    @Transaction
    @Query("SELECT * FROM muscle_groups WHERE muscle_group_id = :id")
    fun getMuscleGroupWithExercises(id: Int): Flow<MuscleGroupWithExercises>

    @Transaction
    @Query("SELECT * FROM muscle_groups")
    fun getAllMuscleGroupsWithExercises(): Flow<List<MuscleGroupWithExercises>>


}