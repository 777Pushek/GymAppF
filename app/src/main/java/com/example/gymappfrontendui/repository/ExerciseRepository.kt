package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import com.example.gymappfrontendui.db.entity.SyncQueue
import com.example.gymappfrontendui.db.relationships.ExerciseWithMuscleGroups
import com.example.gymappfrontendui.db.relationships.ExerciseWithWorkoutExercise
import com.example.gymappfrontendui.db.relationships.ExerciseWithWorkoutTemplates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest


class ExerciseRepository(context: Context) {

    private val db = AppDb.getInstance(context)
    private val exerciseDao = db.exerciseDao()
    private val syncQueueDao = db.syncQueueDao()
    private val exerciseMuscleGroupDao = db.exerciseMuscleGroupDao()
    private val userDao = db.userDao()


    suspend fun insertExercise(exercise: Exercise,muscleGroupIds: List<Int>): Long = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val newId = exerciseDao.insertExercise(exercise.copy(userId = userId))

        val q =SyncQueue(
            tableName = "exercises",
            localId = newId.toInt(),
            userId = userId
        )
        syncQueueDao.insertSyncQueue(q)
        muscleGroupIds.forEach { mgId ->
            exerciseMuscleGroupDao.insertExerciseMuscleGroup(ExerciseMuscleGroup(muscleGroupId = mgId, exerciseId = newId.toInt()))
        }
        newId
    }
    suspend fun insertExercisesWithGroups(exerciseToMuscleMap: Map<Exercise, List<Int>>): List<Long> = withContext(Dispatchers.IO) {
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        val insertedIds = mutableListOf<Long>()

        exerciseToMuscleMap.forEach { (exercise, muscleIds) ->
            val newId = exerciseDao.insertExercise(exercise.copy(userId = userId))
            insertedIds.add(newId)

            val q = SyncQueue(
                tableName = "exercises",
                localId = newId.toInt(),
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)

            muscleIds.forEach { mgId ->
                exerciseMuscleGroupDao.insertExerciseMuscleGroup(
                    ExerciseMuscleGroup(
                        exerciseId = newId.toInt(),
                        muscleGroupId = mgId
                    )
                )
            }
        }

        insertedIds
    }


    suspend fun updateExercise(exercise: Exercise) = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()
        exerciseDao.updateExercise(exercise.copy(userId = userId))

        if(syncQueueDao.getSyncQueueByTableName(exercise.exerciseId,"exercises") == null){
            val q = SyncQueue(
                tableName = "exercises",
                localId = exercise.exerciseId,
                globalId = exercise.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }
    }

    suspend fun deleteExercise(exercise: Exercise) = withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId() ?: userDao.getGuestUserId()

        if(exercise.globalId != null){
            val q = SyncQueue(
                tableName = "exercises",
                localId = exercise.exerciseId,
                globalId = exercise.globalId,
                userId = userId
            )
            syncQueueDao.insertSyncQueue(q)
        }else{
            val existing = syncQueueDao.getSyncQueueByTableName(exercise.exerciseId,"exercises")
            if(existing != null){
                syncQueueDao.deleteSyncQueue(existing)
            }
        }
        exerciseDao.deleteExercise(exercise)
    }

    suspend fun deleteExerciseByGlobalId(id: Int) {
        exerciseDao.deleteExerciseByGlobalId(id)
    }

    fun getExerciseById(id: Int): Flow<Exercise> {
        return exerciseDao.getExerciseById(id)
    }



    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableExercises(): Flow<List<Exercise>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            exerciseDao.getAvailableExercises(userId)
        }
    }



    fun getExerciseWithMuscleGroups(id: Int): Flow<ExerciseWithMuscleGroups> {
        return exerciseDao.getExerciseWithMuscleGroups(id)
    }

    fun getExerciseWithWorkoutExercises(id: Int): Flow<ExerciseWithWorkoutExercise> {
        return exerciseDao.getExerciseWithWorkoutExercises(id)
    }

    fun getExerciseWithWorkoutTemplates(id: Int): Flow<ExerciseWithWorkoutTemplates> {
        return exerciseDao.getExerciseWithWorkoutTemplates(id)
    }



    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableExercisesWithMuscleGroups(): Flow<List<ExerciseWithMuscleGroups>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            exerciseDao.getAvailableExercisesWithMuscleGroups(userId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableExercisesWithWorkoutExercises(): Flow<List<ExerciseWithWorkoutExercise>>{
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            exerciseDao.getAvailableExercisesWithWorkoutExercises(userId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAvailableExercisesWithWorkoutTemplates(): Flow<List<ExerciseWithWorkoutTemplates>>{
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId ->
            val userId = loggedInUserId ?: userDao.getGuestUserId()
            exerciseDao.getAvailableExercisesWithWorkoutTemplates(userId)

        }

    }
    suspend fun insertExerciseMuscleGroup(exerciseMuscleGroup: ExerciseMuscleGroup) {
        exerciseMuscleGroupDao.insertExerciseMuscleGroup(exerciseMuscleGroup)
    }

    suspend fun insertAllExercisesMuscleGroups(exerciseMuscleGroups: List<ExerciseMuscleGroup>) {
        exerciseMuscleGroupDao.insertAllExercisesMuscleGroups(exerciseMuscleGroups)
    }

    suspend fun getAllExercisesMap(): Map<Int, Exercise> = withContext(Dispatchers.IO) {
        val loggedInUserId = userDao.getLoggedInUserIdFlow().firstOrNull()
        val userId = loggedInUserId ?: userDao.getGuestUserId()
        val exercises = exerciseDao.getAvailableExercises(userId).first()
        exercises.associateBy { it.exerciseId }
    }

}
