package com.example.gymappfrontendui.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.gymappfrontendui.db.dao.BodyMeasurementDao
import com.example.gymappfrontendui.db.dao.ExerciseDao
import com.example.gymappfrontendui.db.dao.ExerciseMuscleGroupDao
import com.example.gymappfrontendui.db.dao.MuscleGroupDao
import com.example.gymappfrontendui.db.dao.ScheduledWorkoutDao
import com.example.gymappfrontendui.db.dao.SetDao
import com.example.gymappfrontendui.db.dao.SyncQueueDao
import com.example.gymappfrontendui.db.dao.UserDao
import com.example.gymappfrontendui.db.dao.WeekScheduleDao
import com.example.gymappfrontendui.db.dao.WorkoutDao
import com.example.gymappfrontendui.db.dao.WorkoutExerciseDao
import com.example.gymappfrontendui.db.dao.WorkoutTemplateDao
import com.example.gymappfrontendui.db.dao.WorkoutTemplateExerciseDao
import com.example.gymappfrontendui.db.entity.BodyMeasurement
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.ExerciseMuscleGroup
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.db.entity.ScheduledWorkout
import com.example.gymappfrontendui.db.entity.Set
import com.example.gymappfrontendui.db.entity.SyncQueue
import com.example.gymappfrontendui.db.entity.User
import com.example.gymappfrontendui.db.entity.WeekSchedule
import com.example.gymappfrontendui.db.entity.Workout
import com.example.gymappfrontendui.db.entity.WorkoutExercise
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.db.entity.WorkoutTemplateExercise
import com.example.gymappfrontendui.models.AccountType


@Database(entities = [
    User::class,
    Exercise::class,
    MuscleGroup::class,
    ExerciseMuscleGroup::class,
    WorkoutTemplate::class,
    WorkoutTemplateExercise::class,
    Workout::class,
    WorkoutExercise::class,
    Set::class,
    BodyMeasurement::class,
    SyncQueue::class,
    WeekSchedule::class,
    ScheduledWorkout::class
], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseMuscleGroupDao(): ExerciseMuscleGroupDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun workoutTemplateExerciseDao(): WorkoutTemplateExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun setDao(): SetDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun weekScheduleDao(): WeekScheduleDao
    abstract fun scheduledWorkoutDao(): ScheduledWorkoutDao


}
object AppDb{
    @Volatile
    private var db: AppDatabase? = null
    fun getInstance(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "gym"
            )
                .addCallback(AppDatabaseCallback(context))
                .build()
            db = instance
            instance
        }
    }
}

class AppDatabaseCallback(private val context: Context): RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateDatabase(context)
        }
    }

    private suspend fun prepopulateDatabase(context: Context) {
        val database = AppDb.getInstance(context)

        val muscleGroupDao = database.muscleGroupDao()
        val userDao = database.userDao()
        val exerciseDao = database.exerciseDao()
        val exerciseMuscleGroupDao = database.exerciseMuscleGroupDao()
        val workoutTemplateDao = database.workoutTemplateDao()
        val workoutTemplateExerciseDao = database.workoutTemplateExerciseDao()

        val groups = listOf(
            MuscleGroup(name = "Klatka piersiowa"),
            MuscleGroup(name = "Plecy"),
            MuscleGroup(name = "Nogi"),
            MuscleGroup(name = "Ramiona"),
            MuscleGroup(name = "Biceps"),
            MuscleGroup(name = "Triceps")
        )

        muscleGroupDao.insertMuscleGroups(groups)
        userDao.insertUser(User(
            userName = "guest",
            isLoggedIn = false,
            email = null,
            accountType = AccountType.GUEST
        ))
        /*
        val ex1_benchId = exerciseDao.insertExercise(Exercise(
            name = "Wyciskanie na ławce",
            description = "Klasyczne wyciskanie na klatkę piersiową.",
            userId = null
        ))

        val ex2_squatId = exerciseDao.insertExercise(Exercise(
            name = "Przysiad ze sztangą",
            description = "Podstawowe ćwiczenie na nogi.",
            userId = null
        ))

        val ex3_rowId = exerciseDao.insertExercise(Exercise(
            name = "Wiosłowanie sztangą",
            description = "Ćwiczenie na plecy.",
            userId = null
        ))

        exerciseMuscleGroupDao.insertExerciseMuscleGroup(ExerciseMuscleGroup(exerciseId = ex1_benchId.toInt(), muscleGroupId = 1))
        exerciseMuscleGroupDao.insertExerciseMuscleGroup(ExerciseMuscleGroup(exerciseId = ex2_squatId.toInt(), muscleGroupId = 3))
        exerciseMuscleGroupDao.insertExerciseMuscleGroup(ExerciseMuscleGroup(exerciseId = ex3_rowId.toInt(), muscleGroupId = 2))


        val template1_fwaId = workoutTemplateDao.insertWorkoutTemplate(WorkoutTemplate(
            name = "Full Body Workout A",
            userId = null
        ))

        workoutTemplateExerciseDao.insertWorkoutTemplateExercise(WorkoutTemplateExercise(
            workoutTemplateId = template1_fwaId.toInt(),
            exerciseId = ex1_benchId.toInt(),
            position = 1,
        ))

        workoutTemplateExerciseDao.insertWorkoutTemplateExercise(WorkoutTemplateExercise(
            workoutTemplateId = template1_fwaId.toInt(),
            exerciseId = ex2_squatId.toInt(),
            position = 2,
        ))

        workoutTemplateExerciseDao.insertWorkoutTemplateExercise(WorkoutTemplateExercise(
            workoutTemplateId = template1_fwaId.toInt(),
            exerciseId = ex3_rowId.toInt(),
            position = 3,
        ))
    */
    }
}
