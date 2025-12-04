package com.example.gymappfrontendui.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.dto.WorkoutSetWithDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SetRepository(context: Context) {
    private val db = AppDb.getInstance(context)
    private val setDao = db.setDao()
    private val userDao = db.userDao()

    suspend fun insertSet(set: Set): Long = withContext(Dispatchers.IO){
        val newId = setDao.insertSet(set)
        newId
    }

    suspend fun updateSet(set: Set) = withContext(Dispatchers.IO){
        setDao.updateSet(set)
    }

    suspend fun deleteSet(set: Set) = withContext(Dispatchers.IO){
        setDao.deleteSet(set)
    }

    fun getSetById(setId: Int): Flow<Set> {
        return setDao.getSetById(setId)
    }

    fun getSetByWorkoutExerciseId(workoutExerciseId: Int): Flow<List<Set>> {
        return setDao.getSetByWorkoutExerciseId(workoutExerciseId)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getWorkoutSetsWithDateForExercise(exerciseId: Int): Flow<List<WorkoutSetWithDate>> {
        return userDao.getLoggedInUserIdFlow().flatMapLatest { loggedInUserId: Int? ->
            val userId: Int? = loggedInUserId ?: userDao.getGuestUserId()

            if (userId == null) {
                flowOf(emptyList())
            } else {
                getSetsAndWorkoutDateForExerciseAndUser(exerciseId, userId)
            }
        }
    }
    fun getSetsAndWorkoutDateForExerciseAndUser(exerciseId: Int, userId: Int): Flow<List<WorkoutSetWithDate>> {
        return setDao.getWorkoutsWithExercisesAndSetsForUser(userId)
            .map { workoutsWithRelations ->
                workoutsWithRelations.flatMap { workoutWithRelations ->
                    val workoutDate = workoutWithRelations.workout.date

                    workoutWithRelations.exercisesWithSets
                        .filter { it.workoutExercise.exerciseId == exerciseId }
                        .flatMap { weWithSets ->
                            weWithSets.sets.map { set ->
                                WorkoutSetWithDate(
                                    workoutSet = set,
                                    workoutDate = workoutDate
                                )
                            }

                        }
                }
            }
    }
}


