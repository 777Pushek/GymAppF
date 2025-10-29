package com.example.gymappfrontendui.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import com.example.gymappfrontendui.db.AppDb

import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.entity.SyncQueue
import com.example.gymappfrontendui.db.entity.WeekSchedule
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise
import com.example.gymappfrontendui.network.ApiClient
import com.example.gymappfrontendui.network.dto.reguest.BodyMeasurementRequest
import com.example.gymappfrontendui.network.dto.reguest.ExerciseRequest
import com.example.gymappfrontendui.network.dto.reguest.ScheduledWorkoutsRequest
import com.example.gymappfrontendui.network.dto.reguest.SetRequest
import com.example.gymappfrontendui.network.dto.reguest.WeekScheduleRequest
import com.example.gymappfrontendui.network.dto.reguest.WorkoutExerciseRequest
import com.example.gymappfrontendui.network.dto.reguest.WorkoutRequest
import com.example.gymappfrontendui.network.dto.reguest.WorkoutTemplatesRequest
import retrofit2.HttpException
import java.time.Instant

class SyncQueueRepository(context: Context) {
    private val db = AppDb.getInstance(context)
    private val syncQueueDao = db.syncQueueDao()
    private val userDao = db.userDao()
    private val exerciseDao = db.exerciseDao()
    private val exerciseMuscleGroupDao = db.exerciseMuscleGroupDao()
    private val bodyMeasurementDao = db.bodyMeasurementDao()
    private val workoutDao = db.workoutDao()
    private val workoutExerciseDao = db.workoutExerciseDao()
    private val workoutTemplateDao = db.workoutTemplateDao()
    private val workoutTemplateExerciseDao = db.workoutTemplateExerciseDao()
    private val weekScheduleDao = db.weekScheduleDao()
    private val scheduledWorkoutDao = db.scheduledWorkoutDao()


    private val setDao = db.setDao()
    private val api = ApiClient.create(context)
    companion object {
        private const val TAG = "SyncQueueRepository"
    }


    suspend fun sync()= withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId()
        if (userId == null) {
            Log.w(TAG, "No logged-in user, skipping sync")
            return@withContext
        }
        try {
            val lastSyncResponse = api.getUserLastSync().lastSync
            val lastSyncResponseInstant = Instant.parse(lastSyncResponse)
            val lastSync = userDao.getLastSync()?.let(Instant::parse)

            if (lastSync == null || lastSyncResponseInstant.isAfter(lastSync)) {
                try{downloadFromServer(lastSync?.toString(),lastSyncResponse,userId)}
                catch (e: HttpException) {
                    if (e.code() == 404) {
                        Log.w(TAG, "No data to download (404).")
                    } else {
                        Log.e(TAG, "Download failed with HTTP ${e.code()}: ${e.message()}")
                        throw e
                    }
                }

            }
            uploadToServer(lastSyncResponse)


        }catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}")
            throw e
        }

    }
    suspend fun hasGuestData(): Boolean {
        val guestId = userDao.getGuestUserId()

        val hasExercises = exerciseDao.hasExercisesForUser(guestId)
        val hasWorkouts = workoutDao.hasWorkoutsForUser(guestId)
        val hasBodyMeasurements = bodyMeasurementDao.hasBodyMeasurementsForUser(guestId)
        val hasTemplates = workoutTemplateDao.hasWorkoutTemplatesForUser(guestId)
        val hasWeekSchedules = weekScheduleDao.hasWeekSchedulesForUser(guestId)
        val hasSyncQueue = syncQueueDao.hasSyncQueuesForUser(guestId)

        return hasExercises || hasWorkouts || hasBodyMeasurements ||
                hasTemplates || hasWeekSchedules || hasSyncQueue
    }

    suspend fun assignSyncQueueToUser() {
        val userId = userDao.getLoggedInUserId()
        if (userId == null) {
            Log.w(TAG, "No logged-in user to assign guest queues to")
            return
        }
        val guestId = userDao.getGuestUserId()
        val syncQueue = syncQueueDao.getSyncQueuesByUserId(guestId)

        for(q in syncQueue){
            syncQueueDao.updateSyncQueue(q.copy(userId = userId))
            when (q.tableName) {
                "exercises" -> {
                    q.localId?.let { id ->
                        val entity = exerciseDao.getExerciseById(id).firstOrNull()
                        if (entity != null && entity.userId == guestId) {
                            exerciseDao.updateExercise(entity.copy(userId = userId))
                            Log.d(TAG, "Assigned userId=$userId to Exercise localId=$id")
                        }
                    }
                }

                "body_measurements" -> {
                    q.localId?.let { id ->
                        val entity = bodyMeasurementDao.getBodyMeasurementById(id).firstOrNull()
                        if (entity != null && entity.userId == guestId) {
                            bodyMeasurementDao.updateBodyMeasurement(entity.copy(userId = userId))
                            Log.d(TAG, "Assigned userId=$userId to BodyMeasurement localId=$id")
                        }
                    }
                }

                "workouts" -> {
                    q.localId?.let { id ->
                        val entity = workoutDao.getWorkoutById(id).firstOrNull()
                        if (entity != null && entity.userId == guestId) {
                            workoutDao.updateWorkout(entity.copy(userId = userId))
                            Log.d(TAG, "Assigned userId=$userId to Workout localId=$id")
                        }
                    }
                }

                "workout_templates" -> {
                    q.localId?.let { id ->
                        val entity = workoutTemplateDao.getWorkoutTemplateById(id).firstOrNull()
                        if (entity != null && entity.userId == guestId) {
                            workoutTemplateDao.updateWorkoutTemplate(entity.copy(userId = userId))
                            Log.d(TAG, "Assigned userId=$userId to WorkoutTemplate localId=$id")
                        }
                    }
                }
                "week_schedules" -> {
                    q.localId?.let { id ->
                        val entity = weekScheduleDao.getWeekScheduleById(id).firstOrNull()
                        if (entity != null && entity.userId == guestId) {
                            weekScheduleDao.updateWeekSchedule(entity.copy(userId = userId))
                            Log.d(TAG, "Assigned userId=$userId to WeekSchedule localId=$id")
                        }
                    }
                }

                else -> {
                    Log.w(TAG, "Unknown table in SyncQueue: ${q.tableName}")
                }
            }
        }

        Log.d(TAG, "Assigned userId=$userId to ${syncQueue.size} SyncQueue records and related local entities.")

    }
    suspend fun downloadFromServer(startDate: String?, endDate: String, userId: Int)= withContext(Dispatchers.IO){


        try {
            // Exercises
            try {
                downloadExercises(startDate, endDate, userId)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.w(TAG, "No exercises to download (404).")
                } else {
                    Log.e(TAG, "downloadExercises failed: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "downloadExercises error: ${e.message}")
            }

            // Workouts
            try {
                downloadWorkouts(startDate, endDate, userId)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.w(TAG, "No workouts to download (404).")
                } else {
                    Log.e(TAG, "downloadWorkouts failed: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "downloadWorkouts error: ${e.message}")
            }

            // Workout templates
            try {
                downloadWorkoutTemplates(startDate, endDate, userId)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.w(TAG, "No templates to download (404).")
                } else {
                    Log.e(TAG, "downloadWorkoutTemplates failed: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "downloadWorkoutTemplates error: ${e.message}")
            }

            // Body measurements
            try {
                downloadBodyMeasurements(startDate, endDate, userId)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.w(TAG, "No body measurements to download (404).")
                } else {
                    Log.e(TAG, "downloadBodyMeasurements failed: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "downloadBodyMeasurements error: ${e.message}")
            }

            // Week schedules
            try {
                downloadWeekSchedules(startDate, endDate, userId)
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.w(TAG, "No week schedules to download (404).")
                } else {
                    Log.e(TAG, "downloadWeekSchedules failed: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "downloadWeekSchedules error: ${e.message}")
            }


            userDao.updateLastSync(endDate)

        } catch (e: Exception) {
            Log.e(TAG, "downloadFromServer fatal error: ${e.message}")
            throw e
        }


    }

    private suspend fun downloadWeekSchedules(startDate: String?, endDate: String, userId: Int) {
        var offset = 0
        val limit = 100
        var hasMore: Boolean


        do {
            val response = api.getUserWeekSchedules(offset, limit, startDate, endDate)
            hasMore = response.hasMore

            for (ws in response.data) {
                if (ws.deleted) {

                    weekScheduleDao.deleteWeekScheduleByGlobalId(ws.id)
                } else {
                    val entity = WeekSchedule(
                        globalId = ws.id,
                        name = ws.name,
                        userId = userId,
                        notificationTime = ws.notificationTime,
                        selected = ws.selected
                    )
                    val localId = weekScheduleDao.insertWeekSchedule(entity).toInt()

                    if (ws.selected) {
                        weekScheduleDao.clearSelectedForUser(userId)
                        weekScheduleDao.updateWeekSchedule(entity.copy(weekScheduleId = localId, selected = true))
                    }
                }
            }
            offset += limit
        } while (hasMore)
    }

    private suspend fun downloadExercises(startDate: String?, endDate: String, userId: Int) {
        var offset = 0
        val limit = 20
        var hasMore: Boolean

        do {
            val response = api.getExercises(offset, limit, startDate, endDate)
            hasMore = response.hasMore

            for (exercise in response.data) {
                if (exercise.deleted) {
                    exerciseDao.deleteExerciseByGlobalId(exercise.id)
                } else {
                    val exerciseEntity = Exercise(
                        globalId = exercise.id,
                        name = exercise.name,
                        description = exercise.description,
                        userId = userId
                    )
                    val localExerciseId = exerciseDao.insertExercise(exerciseEntity)

                    exercise.muscleGroups?.forEach { mgId ->
                        exerciseMuscleGroupDao.insertExerciseMuscleGroup(
                            ExerciseMuscleGroup(
                                muscleGroupId = mgId,
                                exerciseId = localExerciseId.toInt(),
                            )
                        )
                    }
                }
            }
            offset += limit
        } while (hasMore)
    }

    private suspend fun downloadWorkouts(startDate: String?, endDate: String, userId: Int) {
        var offset = 0
        val limit = 5
        var hasMore: Boolean

        do {
            val response = api.getUserWorkouts(offset, limit, startDate, endDate)
            hasMore = response.hasMore

            for (workout in response.data) {
                if (workout.deleted) {
                    workoutDao.deleteWorkoutByGlobalId(workout.id)
                } else {
                    val workoutEntity = Workout(
                        globalId = workout.id,
                        duration = workout.duration,
                        date = workout.date,
                        userId = userId,
                        workoutTemplateId = workout.template,
                    )
                    val localWorkoutId = workoutDao.insertWorkout(workoutEntity)

                    for (ex in workout.exercises) {

                        val localExerciseId = exerciseDao.getExerciseGlobalIdById(ex.id)
                        if (localExerciseId == null) {
                            Log.w("SyncQueueRepository", "Exercise with globalId=${ex.id} not found locally, skipping workout exercise")
                            continue
                        }
                        val localWorkoutExerciseId = workoutExerciseDao.insertWorkoutExercise(
                            WorkoutExercise(
                                position = ex.position,
                                exerciseId = localExerciseId,
                                workoutId = localWorkoutId.toInt()

                            )
                        )

                        ex.sets.forEachIndexed { index, set ->
                            setDao.insertSet(
                                Set(
                                    workoutExerciseId = localWorkoutExerciseId.toInt(),
                                    reps = set.reps,
                                    weight = set.weight,
                                    position = index
                                )
                            )
                        }

                    }
                }
            }
            offset += limit
        } while (hasMore)
    }

    private suspend fun downloadBodyMeasurements(startDate: String?, endDate: String, userId: Int) {
        var offset = 0
        val limit = 20
        var hasMore: Boolean

        do {
            val response = api.getUserMeasurements(offset, limit, startDate, endDate)
            hasMore = response.hasMore

            for (bm in response.data) {
                if (bm.deleted) {
                    bodyMeasurementDao.deleteBodyMeasurementByGlobalId(bm.id)
                } else {
                    val entity = BodyMeasurement(
                        globalId = bm.id,
                        weight = bm.weight,
                        arm = bm.arm,
                        forearm = bm.forearm,
                        chest = bm.chest,
                        waist = bm.waist,
                        hips = bm.hips,
                        thigh = bm.thigh,
                        calf = bm.calf,
                        date = bm.date,
                        userId = userId
                    )
                    bodyMeasurementDao.insertBodyMeasurement(entity)
                }
            }
            offset += limit
        } while (hasMore)
    }

    private suspend fun downloadWorkoutTemplates(startDate: String?, endDate: String, userId: Int) {
        var offset = 0
        val limit = 20
        var hasMore: Boolean
        do {
            val response = api.getUserWorkoutTemplates(offset, limit, startDate, endDate)
            hasMore = response.hasMore

            for (template in response.data) {
                if (template.deleted) {
                    workoutTemplateDao.deleteWorkoutTemplateByGlobalId(template.id)
                } else {
                    val workoutTemplate = WorkoutTemplate(
                        globalId = template.id,
                        name = template.name,
                        userId = userId
                    )
                    val localTemplateId = workoutTemplateDao.insertWorkoutTemplate(workoutTemplate)

                    template.exercises.forEachIndexed { index, eGlobalId ->
                        val localExerciseId = exerciseDao.getExerciseGlobalIdById(eGlobalId)
                        if (localExerciseId == null) {
                            Log.w("SyncQueueRepository", "Template exercise globalId=$eGlobalId not found locally, skipping")
                            return@forEachIndexed
                        }
                        workoutTemplateExerciseDao.insertWorkoutTemplateExercise(
                            WorkoutTemplateExercise(
                                workoutTemplateId = localTemplateId.toInt(),
                                exerciseId = localExerciseId,
                                position = index
                            )
                        )
                    }
                }
            }
            offset += limit
        } while (hasMore)
    }



    suspend fun uploadToServer(initialLastSync: String)= withContext(Dispatchers.IO){
        val userId = userDao.getLoggedInUserId()
        if (userId == null) {
            Log.w("SyncQueueRepository", "No logged-in user, skipping upload")
            return@withContext
        }
        val syncQueue = syncQueueDao.getSyncQueuesByUserId(userId)
        var currentLastSync = initialLastSync

        for (q in syncQueue){
            try{
                when(q.tableName){
                    "exercises" -> currentLastSync = uploadExercise(q, currentLastSync)
                    "body_measurements" -> currentLastSync = uploadBodyMeasurement(q, currentLastSync)
                    "workouts" -> currentLastSync = uploadWorkout(q, currentLastSync)
                    "workout_templates" -> currentLastSync = uploadWorkoutTemplate(q, currentLastSync)
                    "week_schedules" -> currentLastSync = uploadWeekSchedule(q, currentLastSync)
                    else -> Log.w(TAG, "Unknown table: ${q.tableName}")
                }
            }catch (e:Exception){
                Log.e(TAG, " Sync failed for ${q.tableName}: ${e.message}")
            }

        }
    }

    private suspend fun uploadWeekSchedule(q: SyncQueue, lastSync: String): String {

        val weekSchedule = q.localId?.let { weekScheduleDao.getWeekScheduleById(it).firstOrNull() }
        val scheduledWorkout = q.localId?.let { scheduledWorkoutDao.getScheduledWorkoutsForWeekSchedule(it) } ?: emptyList()
        val scheduledWorkoutRequest = scheduledWorkout.map { sw ->
            ScheduledWorkoutsRequest(
                templateId = sw.workoutTemplateId,
                day = sw.day,
                time = sw.time
            )
        }

        val request = weekSchedule?.let {
            WeekScheduleRequest(
                name = it.name,
                selected = it.selected,
                scheduledWorkouts = scheduledWorkoutRequest,
                notificationTime = it.notificationTime
            )
        }

        var currentLastSync = lastSync
        try {
            when {
                // Add
                q.globalId == null && weekSchedule != null -> {
                    val response = api.addWeekSchedule(
                        request = request!!,
                        lastSync = currentLastSync
                    )
                    weekSchedule.globalId = response.id
                    weekScheduleDao.updateWeekSchedule(weekSchedule)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Add complete")
                }

                // Update
                q.globalId != null && weekSchedule != null -> {
                    val response = api.updateWeekSchedule(
                        id = q.globalId!!,
                        request = request!!,
                        lastSync = currentLastSync,
                    )

                    weekScheduleDao.updateWeekSchedule(weekSchedule)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Update complete")
                }

                // Delete
                q.globalId != null && weekSchedule == null -> {
                    val response = api.deleteWeekSchedule(
                        id = q.globalId!!,
                        lastSync = currentLastSync
                    )
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Delete complete")
                }

                else -> {
                    Log.w(TAG, "Nothing to sync for week schedule queue: $q")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Week Schedule sync error: ${e.message}")
        }

        return currentLastSync

    }

    private suspend fun uploadExercise(q: SyncQueue, lastSync: String): String{

        val exercise = q.localId?.let { exerciseDao.getExerciseById(it).firstOrNull() }
        val muscleGroupIds = q.localId?.let { exerciseDao.getMuscleGroupIdsForExercise(it) } ?: emptyList()

        val request = exercise?.let {
            ExerciseRequest(
                name = it.name,
                description = it.description,
                muscleGroups = muscleGroupIds
            )
        }

        var currentLastSync = lastSync
        try {
            when {
                // Add
                q.globalId == null && exercise != null -> {
                    val response = api.addExercise(
                        request = request!!,
                        lastSync = currentLastSync
                    )
                    exercise.globalId = response.id
                    exerciseDao.updateExercise(exercise)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Add complete")
                }

                // Update
                q.globalId != null && exercise != null -> {
                    val response = api.updateExercise(
                        id = q.globalId!!,
                        request = request!!,
                        lastSync = currentLastSync,
                    )
                    exerciseDao.updateExercise(exercise)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Update complete")
                }

                // Delete
                q.globalId != null && exercise == null -> {
                    val response = api.deleteExercise(
                        id = q.globalId!!,
                        lastSync = currentLastSync
                    )
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Delete complete")
                }

                else -> {
                    Log.w(TAG, "Nothing to sync for exercise queue: $q")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exercise sync error: ${e.message}")
        }

        return currentLastSync
    }


    private suspend fun uploadBodyMeasurement(q: SyncQueue, lastSync: String): String{
        val bodyMeasurement = q.localId?.let { bodyMeasurementDao.getBodyMeasurementById(it).firstOrNull() }

        val request = bodyMeasurement?.let {
            BodyMeasurementRequest(
                weight = it.weight,
                arm = it.arm,
                forearm = it.forearm,
                chest = it.chest,
                waist = it.waist,
                hips = it.hips,
                thigh = it.thigh,
                calf = it.calf,
                date = it.date
            )
        }

        var currentLastSync = lastSync
        try {
            when {
                // Add
                q.globalId == null && bodyMeasurement != null -> {
                    val response = api.addBodyMeasurement(
                        request = request!!,
                        lastSync = currentLastSync
                    )
                    bodyMeasurement.globalId = response.id
                    bodyMeasurementDao.updateBodyMeasurement(bodyMeasurement)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Add complete")
                }

                // Update
                q.globalId != null && bodyMeasurement != null -> {
                    val response = api.updateBodyMeasurement(
                        id = q.globalId!!,
                        request = request!!,
                        lastSync = currentLastSync,
                    )
                    bodyMeasurementDao.updateBodyMeasurement(bodyMeasurement)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Update complete")
                }

                // Delete
                q.globalId != null && bodyMeasurement == null -> {
                    val response = api.deleteBodyMeasurement(
                        id = q.globalId!!,
                        lastSync = currentLastSync
                    )
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Delete complete")
                }

                else -> {
                    Log.w(TAG, "Nothing to sync for exercise queue: $q")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "BodyMeasurement sync error: ${e.message}")
        }

        return currentLastSync

    }
    private suspend fun uploadWorkout(q: SyncQueue, lastSync: String): String{

        val workout = q.localId?.let { workoutDao.getWorkoutById(it).firstOrNull() }
        val exercisesWithSets = q.localId?.let { workoutDao.getExercisesWithSetsForWorkout(it) } ?: emptyList()

        if (exercisesWithSets.size > 50) {
            Log.e(TAG, "Cannot sync: Maximum 50 exercises per workout")
            return lastSync
        }

        exercisesWithSets.forEach { ex ->
            if (ex.sets.size > 30) {
                Log.e(TAG, "Cannot sync: Maximum 30 sets per exercise (exerciseId=${ex.workoutExercise.exerciseId})")
                return lastSync
            }
        }

        val exercisesRequest = exercisesWithSets.map { we ->
            WorkoutExerciseRequest(
                id = we.workoutExercise.workoutExerciseId,
                sets = we.sets.map { s ->
                    SetRequest(
                        reps = s.reps,
                        weight = s.weight
                    )
                }
            )
        }

        val request = workout?.let {
            WorkoutRequest(
                duration = it.duration,
                template = it.workoutTemplateId,
                date = it.date,
                exercises = exercisesRequest
            )
        }

        var currentLastSync = lastSync
        try {
            when {
                // Add
                q.globalId == null && workout != null -> {
                    val response = api.addWorkout(
                        request = request!!,
                        lastSync = currentLastSync
                    )
                    workout.globalId = response.id
                    workoutDao.updateWorkout(workout)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Add complete")
                }

                // Update
                q.globalId != null && workout != null -> {
                    val response = api.updateWorkout(
                        id = q.globalId!!,
                        request = request!!,
                        lastSync = currentLastSync,
                    )
                    workoutDao.updateWorkout(workout)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Update complete")
                }

                // Delete
                q.globalId != null && workout == null -> {
                    val response = api.deleteWorkout(
                        id = q.globalId!!,
                        lastSync = currentLastSync
                    )
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Delete complete")
                }

                else -> {
                    Log.w(TAG, "Nothing to sync for exercise queue: $q")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Workout sync error: ${e.message}")
        }

        return currentLastSync
    }
    private suspend fun uploadWorkoutTemplate(q: SyncQueue, lastSync: String): String{
        val template = q.localId?.let { workoutTemplateDao.getWorkoutTemplateById(it).firstOrNull() }
        val exerciseIds = q.localId?.let { workoutTemplateDao.getExerciseIdsForTemplate(it) } ?: emptyList()
        if (exerciseIds.size > 50) {
            Log.e(TAG, "Cannot sync: Maximum 50 exercises per template")
            return lastSync
        }

        val request = template?.let {
            WorkoutTemplatesRequest(
                name = it.name,
                exercises = exerciseIds
            )
        }

        var currentLastSync = lastSync
        try {
            when {
                // Add
                q.globalId == null && template != null -> {
                    val response = api.addWorkoutTemplate(
                        request = request!!,
                        lastSync = currentLastSync
                    )
                    template.globalId = response.id
                    workoutTemplateDao.updateWorkoutTemplate(template)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Add complete")
                }

                // Update
                q.globalId != null && template != null -> {
                    val response = api.updateWorkoutTemplate(
                        id = q.globalId!!,
                        request = request!!,
                        lastSync = currentLastSync,
                    )
                    workoutTemplateDao.updateWorkoutTemplate(template)
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Update complete")
                }

                // Delete
                q.globalId != null && template == null -> {
                    val response = api.deleteWorkoutTemplate(
                        id = q.globalId!!,
                        lastSync = currentLastSync
                    )
                    syncQueueDao.deleteSyncQueue(q)
                    userDao.updateLastSync(response.lastSync)
                    currentLastSync = response.lastSync
                    Log.d(TAG, "Delete complete")
                }

                else -> {
                    Log.w(TAG, "Nothing to sync for exercise queue: $q")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Template sync error: ${e.message}")
        }

        return currentLastSync

    }


}