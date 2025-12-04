package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.SyncQueue
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import com.example.gymappfrontendui.db.relationships.WorkoutExerciseWithSets
import com.example.gymappfrontendui.db.relationships.WorkoutWithWorkoutExercises
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest


class WorkoutRepository(context: Context) {

    private val db = AppDb.getInstance(context)
    private val workoutDao = db.workoutDao()
    private val syncQueueDao = db.syncQueueDao()
    private val workoutExerciseDao = db.workoutExerciseDao()
    private val userDao = db.userDao()

    suspend fun insertWorkout(workout: Workout): Long = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val newId = workoutDao.insertWorkout(workout.copy(userId = userId))

        val q = SyncQueue(
            tableName = "workouts",
            localId = newId.toInt(),
            userId = userId
        )
        syncQueueDao.insertSyncQueue(q)
        newId
    }


    suspend fun updateWorkout(workout: Workout) = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        workoutDao.updateWorkout(workout.copy(userId = userId))
        if(syncQueueDao.getSyncQueueByTableName(workout.workoutId,"workouts") == null){

            val q = SyncQueue(
                tableName = "workouts",
                localId = workout.workoutId,
                globalId = workout.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
    }

    suspend fun deleteWorkout(workout: Workout) = withContext(Dispatchers.IO){
        if(workout.globalId != null){
            val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
            val q = SyncQueue(
                tableName = "workouts",
                localId = workout.workoutId,
                globalId = workout.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }else{
            val existing = syncQueueDao.getSyncQueueByTableName(workout.workoutId,"workouts")
            if(existing != null){
                syncQueueDao.deleteSyncQueue(existing)
            }
        }
        workoutDao.deleteWorkout(workout)
    }

    fun getWorkoutById(id: Int): Flow<Workout> {
        return workoutDao.getWorkoutById(id)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableWorkouts(): Flow<List<Workout>>{
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            workoutDao.getAvailableWorkouts(userId)
        }
    }

    fun getWorkoutWithExercisesById(id: Int): Flow<WorkoutWithWorkoutExercises> {
        return workoutDao.getWorkoutWithExercisesById(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableWorkoutsWithExercises(): Flow<List<WorkoutWithWorkoutExercises>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            workoutDao.getAvailableWorkoutsWithExercises(userId)
        }
    }

    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExercise): Long {
        return workoutExerciseDao.insertWorkoutExercise(workoutExercise)
    }

    suspend fun insertAllWorkoutExercises(workoutExercises: List<WorkoutExercise>): List<Long> {
        return workoutExerciseDao.insertAllWorkoutExercises(workoutExercises)
    }

    suspend fun updateWorkoutExercise(workoutExercise: WorkoutExercise) {
        workoutExerciseDao.insertWorkoutExercise(workoutExercise)
    }

    suspend fun deleteWorkoutExercise(workoutExercise: WorkoutExercise) {
        workoutExerciseDao.deleteWorkoutExercise(workoutExercise)
    }

    fun getWorkoutExerciseById(id: Int): Flow<WorkoutExercise> {
        return workoutExerciseDao.getWorkoutExerciseById(id)
    }

    fun getAllWorkoutExercises(): Flow<List<WorkoutExercise>> {
        return workoutExerciseDao.getAllWorkoutExercises()
    }

    fun getWorkoutExerciseWithSets(id: Int): Flow<WorkoutExerciseWithSets> {
        return workoutExerciseDao.getWorkoutExerciseWithSets(id)
    }

    fun getAllWorkoutExercisesWithSets(): Flow<List<WorkoutExerciseWithSets>> {
        return workoutExerciseDao.getAllWorkoutExercisesWithSets()
    }


}
