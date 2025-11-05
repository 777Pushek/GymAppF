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
            MuscleGroup(name = "chest"),
            MuscleGroup(name = "shoulders"),
            MuscleGroup(name = "biceps"),
            MuscleGroup(name = "triceps"),
            MuscleGroup(name = "forearms"),
            MuscleGroup(name = "abs"),
            MuscleGroup(name = "traps"),
            MuscleGroup(name = "lats"),
            MuscleGroup(name = "lower back"),
            MuscleGroup(name = "glutes"),
            MuscleGroup(name = "quads"),
            MuscleGroup(name = "hamstrings"),
            MuscleGroup(name = "calves"),

            )

        muscleGroupDao.insertMuscleGroups(groups)
        userDao.insertUser(User(
            userName = "guest",
            isLoggedIn = false,
            email = null,
            accountType = AccountType.GUEST
        ))
        val exercises = listOf(
            Exercise(name = "bench press", description = "Bench Press is a compound strength exercise that primarily targets the pectoralis major (chest muscles), while also engaging the triceps and anterior deltoids (front shoulders). The movement involves lying on a flat bench, lowering a barbell to your chest under control, and then pressing it upward until your arms are fully extended. It is one of the most effective exercises for building upper body strength and muscle mass."),
            Exercise(name = "bar dip", description = "Bar Dips are a compound upper-body exercise that primarily targets the triceps, pectoralis major (chest), and anterior deltoids (front shoulders). The movement is performed on parallel bars by supporting your body with straight arms, then lowering your torso by bending your elbows until your shoulders drop slightly below your elbows, and pressing back up to full arm extension. This exercise helps build pushing strength and upper-body muscle mass."),
            Exercise(name = "cable chest press", description = "Cable Chest Press is a chest-focused compound exercise performed using a cable machine. Standing or seated between two cable pulleys, you push the handles forward in a controlled motion while keeping your elbows slightly bent. The exercise primarily works the pectoralis major, with secondary activation of the triceps and anterior deltoids. Cables provide constant tension throughout the movement, which helps improve strength and muscle engagement across the full range of motion."),
            Exercise(name = "dumbbell chest fly", description = "Dumbbell Chest Fly is an isolation exercise that primarily targets the pectoralis major. Performed on a flat, incline, or decline bench, you begin with dumbbells held above your chest and arms slightly bent. You then lower the weights out to the sides in a wide arc while maintaining the same elbow angle, feeling a stretch across the chest, before bringing the dumbbells back together over your chest. This exercise focuses on chest muscle expansion and definition rather than heavy pressing strength."),
            Exercise(name = "dumbbell chest press", description = "Dumbbell Chest Press is a compound upper-body exercise that primarily targets the pectoralis major, while also engaging the triceps and anterior deltoids. Lying on a flat, incline, or decline bench, you press two dumbbells upward from chest level until your arms are fully extended, then lower them back down in a controlled motion. Using dumbbells allows for a greater range of motion and helps improve muscular balance between both sides of the body."),
            Exercise(name = "incline bench press", description = "Incline Bench Press is a compound upper-body exercise that emphasizes the upper portion of the pectoralis major, while also working the anterior deltoids and triceps. Performed on a bench set at an incline angle (typically 30–45°), you lower the barbell to the upper chest in a controlled manner and then press it upward to full arm extension. This variation places more load on the upper chest compared to the flat bench press and contributes to a fuller, well-rounded chest development."),
            Exercise(name = "incline dumbbell press", description = "Incline Dumbbell Press is a compound exercise that targets the upper chest (pectoralis major) while also engaging the triceps and anterior deltoids. Performed on a bench set at a 30–45° incline, you press two dumbbells upward from shoulder level until your arms are extended, then lower them back down with control. Using dumbbells increases the range of motion and helps improve muscular balance and stability in the upper body."),
            Exercise(name = "machine chest fly", description = "Machine Chest Fly is an isolation exercise that primarily targets the pectoralis major with minimal involvement of the anterior deltoids and biceps as stabilizers. Sitting upright in a chest fly machine, you bring the handles together in front of your chest while keeping a slight bend in your elbows, then slowly return to the starting position. The guided path of the machine helps maintain proper form and constant tension on the chest muscles throughout the movement."),
            Exercise(name = "machine chest press", description = "Machine Chest Press is a compound exercise that primarily targets the pectoralis major, with secondary involvement of the triceps and anterior deltoids. Seated in a chest press machine, you push the handles forward and extend your arms, then return to the starting position in a controlled manner. The machine provides a fixed movement path, helping maintain proper form and consistent tension on the muscles, making it a beginner-friendly and safe option for building upper-body pushing strength."),
            Exercise(name = "smith machine bench press", description = "Smith Machine Bench Press is a compound upper-body exercise that targets the pectoralis major, with additional activation of the triceps and anterior deltoids. Performed on a flat bench under a guided bar path, you lower the bar to your chest and press it back up to full extension. The fixed trajectory of the Smith machine provides greater stability and control, allowing you to focus on muscle engagement and reduce the need for balance compared to a free-weight bench press."),
            Exercise(name = "machine shoulder press", description = "Machine Shoulder Press is a compound exercise that primarily targets the anterior deltoids, while also engaging the lateral deltoids and triceps. Seated in a shoulder press machine, you press the handles upward until your arms are extended overhead, then lower them back down with control. The guided motion of the machine helps maintain proper form and stability, making it effective and safe for building overhead pressing strength."),
            Exercise(name = "overhead press", description = "Overhead Press is a compound upper-body exercise that primarily targets the deltoid muscles—especially the anterior and lateral heads—while also engaging the triceps and upper trapezius. Standing with a barbell at shoulder height, you press it overhead until your arms are fully extended, then lower it back down under control. This movement builds overall shoulder strength, stability, and upper-body power."),
            Exercise(name = "dumbbell shoulder press", description = "Dumbbell Shoulder Press is a compound exercise that targets the deltoids—particularly the anterior and lateral heads—while also working the triceps and stabilizing muscles of the upper back. Performed seated or standing, you press two dumbbells overhead from shoulder level until your arms are fully extended, then lower them back down in a controlled motion. Using dumbbells enhances shoulder stability and helps correct strength imbalances between sides."),
            Exercise(name = "dumbbell lateral raise", description = "Dumbbell Lateral Raise is an isolation exercise that primarily targets the lateral deltoids, helping to build shoulder width and a broader upper body look. Standing or seated with a dumbbell in each hand, you raise your arms out to the sides until they reach shoulder height while keeping a slight bend in your elbows, then lower them back down under control. This exercise focuses on controlled movement rather than heavy weight to effectively engage the side shoulders."),
            Exercise(name = "dumbbell front raise", description = "Dumbbell Front Raise is an isolation exercise that mainly targets the anterior deltoids (front of the shoulders). Standing or seated with a dumbbell in each hand, you lift the weights straight in front of you to shoulder height while keeping your arms slightly bent, then lower them back down slowly. This movement helps develop front shoulder strength and definition, contributing to improved pressing performance and upper-body aesthetics."),
            Exercise(name = "cable lateral raise", description = "Cable Lateral Raise is an isolation exercise that primarily targets the lateral deltoids, helping create shoulder width and a stronger V-shape. Standing beside a low cable pulley, you lift the handle outward to the side until your arm reaches shoulder height while keeping a slight bend in the elbow, then lower it back under control. The cable provides constant tension throughout the movement, enhancing muscle activation compared to free weights."),
            Exercise(name = "cable front raise", description = "Cable Front Raise is an isolation exercise that focuses on the anterior deltoids (front shoulders). Standing facing away from or toward a low cable pulley, you lift the handle straight forward to shoulder height with a slight bend in your elbow, then lower it back slowly. The cable provides continuous tension throughout the movement, improving muscle engagement and control compared to using dumbbells."),
            Exercise(name = "barbell upright row", description = "Barbell Upright Row is a compound upper-body exercise that primarily targets the lateral deltoids and upper trapezius, with additional involvement of the biceps and forearms. Standing upright with the barbell held in front of your thighs, you pull the bar straight up toward your collarbone, keeping it close to your body and elbows leading the movement, then lower it back down under control. This exercise helps build shoulder width and upper back strength."),
            Exercise(name = "barbell behind neck press", description = "Barbell Behind-the-Neck Press is a compound shoulder exercise that targets the deltoids, especially the lateral and anterior heads, while also engaging the triceps and upper trapezius. Seated or standing with a barbell resting across the upper back, you press the bar overhead until your arms are fully extended, then lower it behind the head in a controlled motion. This variation increases shoulder activation but requires good shoulder mobility to perform safely."),
            Exercise(name = "kettlebell press", description = "Kettlebell Press is a compound exercise that targets the deltoids, triceps, and upper back stabilizers. Performed standing (or seated), you press a kettlebell overhead from shoulder level until your arm is fully extended, then lower it back with control. The offset handle and weight distribution of the kettlebell require greater shoulder stability and core engagement, helping to develop balanced upper-body strength."),
            Exercise(name = "barbell curl", description = "Barbell Curl is an isolation exercise that primarily targets the biceps brachii, with additional involvement of the brachialis and forearms as stabilizers. Standing with a barbell held at arm’s length, you curl the weight upward by bending the elbows while keeping your upper arms stationary, then slowly lower it back down. This exercise is a classic movement for building biceps size and strength."),
            Exercise(name = "barbell preacher curl", description = "Barbell Preacher Curl is an isolation exercise that targets the biceps brachii, with strong emphasis on the lower portion of the muscle and the brachialis. Performed while seated with your upper arms resting on a preacher bench, you curl the barbell upward while keeping the arms fixed against the pad, then lower it under control. The setup reduces momentum and helps maintain strict form, increasing focus on biceps activation."),
            Exercise(name = "dumbbell curl", description = "Dumbbell Curl is an isolation exercise that primarily targets the biceps brachii, with the brachialis and brachioradialis assisting. Standing or seated with a dumbbell in each hand, you curl the weights upward by bending your elbows while keeping your upper arms stable, then lower them back down slowly. Using dumbbells allows each arm to work independently, helping improve muscle balance and control."),
            Exercise(name = "dumbbell preacher curl", description = "Dumbbell Preacher Curl is an isolation exercise that targets the biceps brachii, especially the lower portion of the muscle, with additional involvement of the brachialis. Seated with your upper arm supported on a preacher bench, you curl a dumbbell upward while keeping the arm fixed in place, then lower it back in a controlled motion. This setup limits momentum and increases biceps tension throughout the movement, helping improve muscle strength and definition."),
            Exercise(name = "hammer curl", description = "Hammer Curl is an isolation exercise that primarily targets the brachioradialis and brachialis, while also working the biceps brachii. Holding dumbbells with a neutral grip (palms facing each other), you curl the weights upward by bending your elbows while keeping your upper arms steady, then lower them back under control. This grip variation helps build forearm strength and overall arm thickness."),
            Exercise(name = "barbell standing triceps extension", description = "Barbell Standing Triceps Extension is an isolation exercise that primarily targets the triceps brachii, especially the long head. Standing with a barbell held overhead, you lower the weight behind your head by bending your elbows, then extend your arms back to the starting position in a controlled motion. This exercise helps build triceps size and strength while improving overhead arm stability."),
            Exercise(name = "barbell incline triceps extension", description = "Barbell Incline Triceps Extension is an isolation exercise that targets the triceps brachii, with strong emphasis on the long head due to the incline angle. Lying on an incline bench, you lower the barbell behind your head by bending your elbows, then extend your arms to lift the weight back up. The inclined position increases the stretch on the triceps, promoting greater muscle activation and growth."),
            Exercise(name = "tricep pushdown with bar", description = "Tricep Pushdown With Bar is an isolation exercise that focuses on the triceps brachii, particularly the lateral and medial heads. Standing facing a cable machine with a straight or angled bar attachment, you extend your elbows to push the bar down until your arms are fully straight, then return to the starting position with control. The cable resistance keeps tension on the triceps throughout the movement, making it effective for strength and muscle development."),
            Exercise(name = "tricep pushdown with rope", description = "Tricep Pushdown With Rope is an isolation exercise that primarily targets the triceps brachii, especially the lateral head. Standing at a cable machine with a rope attachment, you push the rope downward by extending your elbows, then separate the rope ends at the bottom of the movement before returning to the starting position with control. The rope allows for a greater range of motion and improved muscle contraction compared to a straight bar."),
            Exercise(name = "smith machine skull crushers", description = "Smith Machine Skull Crushers are an isolation exercise that targets the triceps brachii, especially the long head. Performed lying on a flat bench under the Smith machine bar, you lower the bar toward your forehead by bending your elbows, then extend your arms back to the starting position. The guided bar path improves stability and control, helping maintain proper form and constant tension on the triceps."),
            Exercise(name = "squat", description = "Squat is a fundamental compound lower-body exercise that primarily targets the quadriceps, glutes, and hamstrings, while also engaging the core for stability. Standing with your feet shoulder-width apart, you bend your knees and hips to lower your body as if sitting back into a chair, then drive through your heels to return to a standing position. Squats help build strength, power, and muscle mass in the legs and improve overall functional movement."),
            Exercise(name = "barbell lunge", description = "Barbell Lunge is a compound lower-body exercise that primarily targets the quadriceps and glutes, with additional activation of the hamstrings and core. With a barbell placed across your upper back, you step forward into a lunge position, lowering your back knee toward the floor while keeping your front knee aligned over your foot. You then push through the front heel to return to the starting position. This exercise helps improve leg strength, balance, and unilateral muscle development."),
            Exercise(name = "barbell front squat", description = "Barbell Front Squat is a compound lower-body exercise that primarily targets the quadriceps, while also engaging the glutes, hamstrings, and core—especially the upper back to keep the torso upright. With the barbell resting on the front of your shoulders in the rack position, you squat down by bending your knees and hips, then drive back up through your heels to stand tall. The front-loaded position increases quad activation and encourages better posture compared to the traditional back squat."),
            Exercise(name = "leg press", description = "Leg Press is a compound lower-body exercise that primarily targets the quadriceps, with secondary activation of the glutes and hamstrings. Seated in a leg press machine with your feet on the platform, you push the weight away by extending your knees and hips, then lower it back in a controlled motion without letting your knees collapse inward. The machine provides stability, allowing you to safely use heavier loads to build leg strength and muscle mass."),
            Exercise(name = "seated leg curl", description = "Seated Leg Curl is an isolation exercise that targets the hamstrings, with some involvement of the calves as stabilizers. Seated in a leg curl machine, you flex your knees to pull the pad downward toward the back of your legs, then slowly return to the starting position. This exercise helps build hamstring strength and improves stability and knee joint function."),
            Exercise(name = "barbell rows", description = "Barbell Rows are a compound back exercise that primarily targets the latissimus dorsi, rhomboids, and trapezius, while also engaging the lower back, biceps, and forearm muscles for pulling strength and stability. Bending at the hips with a flat back, you pull the barbell toward your torso, squeezing your shoulder blades together, then lower it back under control. This movement helps build a strong and thick upper back and improves overall pulling power."),
            Exercise(name = "deadlift", description = "Deadlift is a full-body compound exercise that primarily targets the glutes, hamstrings, and lower back, while also engaging the upper back, core, and forearms. Starting with the barbell on the floor, you hinge at the hips and bend your knees to grip the bar, then stand up tall by driving through your legs and extending your hips, keeping your back straight. The deadlift is one of the most effective movements for developing strength, power, and overall muscle mass."),
            Exercise(name = "chin-up", description = "Chin-Up is a compound upper-body pulling exercise that primarily targets the latissimus dorsi (lats) and biceps, with additional activation of the upper back and core. Hanging from a bar with an underhand (supinated) grip, you pull your body upward until your chin passes the bar, then lower yourself back down with control. This movement helps build back strength and arm muscle development."),
            Exercise(name = "muscle-up", description = "Muscle-Up is an advanced compound bodyweight exercise that combines a pull-up with a dip movement. Starting from a hanging position on a bar or rings, you explosively pull your body upward until your chest reaches above the bar, then transition by leaning forward and pressing down to lock out your arms at the top. This exercise primarily targets the lats, upper back, chest, triceps, and core, building upper-body strength, power, and coordination."),
            Exercise(name = "pull-up", description = "Pull-Up is a compound upper-body exercise that primarily Targets the latissimus dorsi (lats), while also engaging the upper back, biceps, forearms, and core. Hanging from a bar with an overhand (pronated) grip, you pull your body upward until your chin rises above the bar, then lower yourself down under control. Pull-ups build significant back strength, grip power, and overall upper-body pulling capability."),
            Exercise(name = "hip thrust", description = "Hip Thrust is a compound lower-body exercise that primarily targets the gluteus maximus, with additional activation of the hamstrings and core. With your upper back supported on a bench and a barbell positioned over your hips, you drive through your heels to extend your hips upward until your body forms a straight line from shoulders to knees, then lower back down under control. This movement is one of the most effective exercises for building glute strength, size, and power."),
            Exercise(name = "cable crunch", description = "Cable Crunch is an abdominal isolation exercise that primarily targets the rectus abdominis, with involvement of the obliques as stabilizers. Kneeling in front of a cable machine while holding a rope attachment, you contract your abs to pull your elbows down toward your knees, then slowly return to the starting position while maintaining tension. This exercise emphasizes spinal flexion and helps build core strength and definition."),
            Exercise(name = "crunches", description = "Crunches are an abdominal isolation exercise that primarily targets the rectus abdominis (the “six-pack” muscle). Lying on your back with knees bent, you lift your shoulders and upper back off the floor by contracting your abs while keeping your lower back grounded, then lower yourself back down under control. This movement focuses on core flexion and helps improve abdominal strength and muscle definition.")
        )
        exerciseDao.insertExercises(exercises)

         val workoutTemplates = listOf(
            WorkoutTemplate(name = "example FBW"),
            WorkoutTemplate(name = "example PUSH"),
            WorkoutTemplate(name = "example PULL"),
        )
        workoutTemplateDao.insertAllWorkoutTemplates(workoutTemplates)
        val exerciseMuscleGroups = listOf(
            ExerciseMuscleGroup(exerciseId = 1, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 1, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 1, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 2, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 2, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 2, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 3, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 3, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 3, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 4, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 4, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 5, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 5, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 5, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 6, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 6, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 6, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 7, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 7, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 7, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 8, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 8, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 9, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 9, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 9, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 10, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 10, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 10, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 11, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 11, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 12, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 12, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 13, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 13, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 14, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 15, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 16, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 17, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 18, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 18, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 18, muscleGroupId = 7),
            ExerciseMuscleGroup(exerciseId = 19, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 19, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 19, muscleGroupId = 7),
            ExerciseMuscleGroup(exerciseId = 20, muscleGroupId = 2),
            ExerciseMuscleGroup(exerciseId = 20, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 21, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 21, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 22, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 22, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 23, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 23, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 24, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 24, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 25, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 25, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 26, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 27, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 28, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 29, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 30, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 31, muscleGroupId = 10),
            ExerciseMuscleGroup(exerciseId = 31, muscleGroupId = 11),
            ExerciseMuscleGroup(exerciseId = 31, muscleGroupId = 13),
            ExerciseMuscleGroup(exerciseId = 32, muscleGroupId = 10),
            ExerciseMuscleGroup(exerciseId = 32, muscleGroupId = 11),
            ExerciseMuscleGroup(exerciseId = 33, muscleGroupId = 10),
            ExerciseMuscleGroup(exerciseId = 33, muscleGroupId = 11),
            ExerciseMuscleGroup(exerciseId = 34, muscleGroupId = 10),
            ExerciseMuscleGroup(exerciseId = 34, muscleGroupId = 11),
            ExerciseMuscleGroup(exerciseId = 34, muscleGroupId = 12),
            ExerciseMuscleGroup(exerciseId = 35, muscleGroupId = 12),
            ExerciseMuscleGroup(exerciseId = 36, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 36, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 36, muscleGroupId = 7),
            ExerciseMuscleGroup(exerciseId = 36, muscleGroupId = 8),
            ExerciseMuscleGroup(exerciseId = 36, muscleGroupId = 9),
            ExerciseMuscleGroup(exerciseId = 37, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 37, muscleGroupId = 7),
            ExerciseMuscleGroup(exerciseId = 37, muscleGroupId = 9),
            ExerciseMuscleGroup(exerciseId = 37, muscleGroupId = 10),
            ExerciseMuscleGroup(exerciseId = 37, muscleGroupId = 11),
            ExerciseMuscleGroup(exerciseId = 37, muscleGroupId = 12),
            ExerciseMuscleGroup(exerciseId = 38, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 38, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 38, muscleGroupId = 8),
            ExerciseMuscleGroup(exerciseId = 39, muscleGroupId = 1),
            ExerciseMuscleGroup(exerciseId = 39, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 39, muscleGroupId = 4),
            ExerciseMuscleGroup(exerciseId = 39, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 39, muscleGroupId = 8),
            ExerciseMuscleGroup(exerciseId = 40, muscleGroupId = 3),
            ExerciseMuscleGroup(exerciseId = 40, muscleGroupId = 5),
            ExerciseMuscleGroup(exerciseId = 40, muscleGroupId = 8),
            ExerciseMuscleGroup(exerciseId = 41, muscleGroupId = 10),
            ExerciseMuscleGroup(exerciseId = 41, muscleGroupId = 11),
            ExerciseMuscleGroup(exerciseId = 42, muscleGroupId = 6),
            ExerciseMuscleGroup(exerciseId = 43, muscleGroupId = 6)
        )

        exerciseMuscleGroupDao.insertAllExercisesMuscleGroups(exerciseMuscleGroups)

        val workoutTemplateExercises = listOf(
            WorkoutTemplateExercise(workoutTemplateId = 1, exerciseId = 1, position = 1),
            WorkoutTemplateExercise(workoutTemplateId = 2, exerciseId = 1, position = 0),
            WorkoutTemplateExercise(workoutTemplateId = 2, exerciseId = 8, position = 2),
            WorkoutTemplateExercise(workoutTemplateId = 1, exerciseId = 12, position = 3),
            WorkoutTemplateExercise(workoutTemplateId = 2, exerciseId = 12, position = 1),
            WorkoutTemplateExercise(workoutTemplateId = 3, exerciseId = 24, position = 2),
            WorkoutTemplateExercise(workoutTemplateId = 3, exerciseId = 25, position = 3),
            WorkoutTemplateExercise(workoutTemplateId = 2, exerciseId = 30, position = 3),
            WorkoutTemplateExercise(workoutTemplateId = 1, exerciseId = 31, position = 0),
            WorkoutTemplateExercise(workoutTemplateId = 3, exerciseId = 36, position = 1),
            WorkoutTemplateExercise(workoutTemplateId = 1, exerciseId = 37, position = 2),
            WorkoutTemplateExercise(workoutTemplateId = 3, exerciseId = 40, position = 0)
        )


        workoutTemplateExerciseDao.insertAllWorkoutTemplateExercises(workoutTemplateExercises)
    }
}
