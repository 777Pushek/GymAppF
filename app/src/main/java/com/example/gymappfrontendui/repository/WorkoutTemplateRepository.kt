package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.SyncQueue
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithExercises
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithWorkouts
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest



class WorkoutTemplateRepository(context: Context) {
    private val db = AppDb.getInstance(context)
    private val workoutTemplateDao = db.workoutTemplateDao()
    private val syncQueueDao = db.syncQueueDao()
    private val workoutTemplateExerciseDao = db.workoutTemplateExerciseDao()
    private val userDao = db.userDao()

    suspend fun insertWorkoutTemplate(workoutTemplate: WorkoutTemplate): Long = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val newId = workoutTemplateDao.insertWorkoutTemplate(workoutTemplate.copy(userId = userId))

        val q = SyncQueue(
            tableName = "workoutTemplates",
            localId = newId.toInt(),
            userId = userId
        )
        syncQueueDao.insertSyncQueue(q)
        newId
    }

    suspend fun insertAllWorkoutTemplates(workoutTemplates: List<WorkoutTemplate>): List<Long> = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val newId = workoutTemplateDao.insertAllWorkoutTemplates(workoutTemplates.map { it.copy(userId = userId) })

        for(i in newId){
            val q = SyncQueue(
                tableName = "workout_templates",
                localId = i.toInt(),
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
        newId
    }

    suspend fun updateWorkoutTemplate(workoutTemplate: WorkoutTemplate) = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        workoutTemplateDao.updateWorkoutTemplate(workoutTemplate.copy(userId = userId))
        if(syncQueueDao.getSyncQueueByTableName(workoutTemplate.workoutTemplateId,"workout_templates") == null){

            val q = SyncQueue(
                tableName = "workout_templates",
                localId = workoutTemplate.workoutTemplateId,
                globalId = workoutTemplate.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
    }

    suspend fun deleteWorkoutTemplate(workoutTemplate: WorkoutTemplate) = withContext(Dispatchers.IO){
        if(workoutTemplate.globalId != null){
            val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()

            val q = SyncQueue(
                tableName = "workout_templates",
                localId = workoutTemplate.workoutTemplateId,
                globalId = workoutTemplate.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }else{
            val existing = syncQueueDao.getSyncQueueByTableName(workoutTemplate.workoutTemplateId,"workout_templates")
            if(existing != null){
                syncQueueDao.deleteSyncQueue(existing)
            }
        }
        workoutTemplateDao.deleteWorkoutTemplate(workoutTemplate)
    }

    suspend fun deleteWorkoutTemplateByGlobalId(id: Int) {
        workoutTemplateDao.deleteWorkoutTemplateByGlobalId(id)
    }


    fun getWorkoutTemplateById(id: Int): Flow<WorkoutTemplate> {
        return workoutTemplateDao.getWorkoutTemplateById(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableWorkoutTemplates(): Flow<List<WorkoutTemplate>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
            workoutTemplateDao.getAvailableWorkoutTemplates(userId)
        }
    }

    fun getTemplateWithExercises(id: Int): Flow<WorkoutTemplateWithExercises> {
        return workoutTemplateDao.getTemplateWithExercises(id)
    }

    fun getTemplateWithWorkouts(id: Int): Flow<WorkoutTemplateWithWorkouts> {
        return workoutTemplateDao.getTemplateWithWorkouts(id)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableTemplatesWithExercises(): Flow<List<WorkoutTemplateWithExercises>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            workoutTemplateDao.getAvailableTemplatesWithExercises(userId)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableTemplatesWithWorkouts(): Flow<List<WorkoutTemplateWithWorkouts>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
            workoutTemplateDao.getAvailableTemplatesWithWorkouts(userId)
        }

    }

    suspend fun insertWorkoutTemplateExercise(workoutTemplateExercise: WorkoutTemplateExercise) = withContext(
        Dispatchers.IO){
        workoutTemplateExerciseDao.insertWorkoutTemplateExercise(workoutTemplateExercise)
    }

    suspend fun insertAllWorkoutTemplateExercises(workoutTemplateExercises: List<WorkoutTemplateExercise>) = withContext(Dispatchers.IO){
        workoutTemplateExerciseDao.insertAllWorkoutTemplateExercises(workoutTemplateExercises)
    }
}