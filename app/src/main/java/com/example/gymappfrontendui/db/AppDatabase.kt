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
import com.example.gymappfrontendui.db.dao.ExerciseTranslationDao
import com.example.gymappfrontendui.db.dao.LanguageDao
import com.example.gymappfrontendui.db.dao.MuscleGroupDao
import com.example.gymappfrontendui.db.dao.MuscleGroupTranslationDao
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
import com.example.gymappfrontendui.db.entity.ExerciseTranslation
import com.example.gymappfrontendui.db.entity.Language
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.db.entity.MuscleGroupTranslation
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
    ScheduledWorkout::class,
    ExerciseTranslation::class,
    Language::class,
    MuscleGroupTranslation::class
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
    abstract fun exerciseTranslationDao(): ExerciseTranslationDao
    abstract fun languageDao(): LanguageDao
    abstract fun muscleGroupTranslationDao(): MuscleGroupTranslationDao

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
        val languageDao = database.languageDao()
        val exerciseTranslationDao = database.exerciseTranslationDao()
        val muscleGroupTranslationDao = database.muscleGroupTranslationDao()


        languageDao.insertAllLanguages(
            listOf(
                Language(name = "English"),
                Language(name = "Polish")
            )
        )

        val muscleNamesEn = listOf(
            "chest", "shoulders", "biceps", "triceps", "forearms",
            "abs", "traps", "lats", "lower back", "glutes",
            "quads", "hamstrings", "calves"
        )

        val muscleNamesPl = listOf(
            "klatka piersiowa", "barki", "bicepsy", "tricepsy", "przedramiona",
            "brzuch", "pułapki", "szerokie grzbietu", "dolny odcinek pleców",
            "pośladki", "czworogłowe uda", "tylne uda", "łydki"
        )
        val groups = muscleNamesEn.map { MuscleGroup(name = it) }
        muscleGroupDao.insertMuscleGroups(groups)

        val translationGroup =
            muscleNamesEn.mapIndexed { index, nameEn ->
                MuscleGroupTranslation(
                    muscleGroupId = index + 1,
                    languageId = 1,
                    name = nameEn
                )
            } +
                    muscleNamesPl.mapIndexed { index, namePl ->
                        MuscleGroupTranslation(
                            muscleGroupId = index + 1,
                            languageId = 2,
                            name = namePl
                        )
                    }

        muscleGroupTranslationDao.insertAllMuscleGroupTranslations(translationGroup)

        userDao.insertUser(User(
            userName = "guest",
            isLoggedIn = false,
            email = null,
            accountType = AccountType.GUEST
        ))
        val descriptionsEn = listOf(
            "Bench Press is a compound strength exercise that primarily targets the pectoralis major (chest muscles), while also engaging the triceps and anterior deltoids (front shoulders). The movement involves lying on a flat bench, lowering a barbell to your chest under control, and then pressing it upward until your arms are fully extended. It is one of the most effective exercises for building upper body strength and muscle mass.",
            "Bar Dips are a compound upper-body exercise that primarily targets the triceps, pectoralis major (chest), and anterior deltoids (front shoulders). The movement is performed on parallel bars by supporting your body with straight arms, then lowering your torso by bending your elbows until your shoulders drop slightly below your elbows, and pressing back up to full arm extension. This exercise helps build pushing strength and upper-body muscle mass.",
            "Cable Chest Press is a chest-focused compound exercise performed using a cable machine. Standing or seated between two cable pulleys, you push the handles forward in a controlled motion while keeping your elbows slightly bent. The exercise primarily works the pectoralis major, with secondary activation of the triceps and anterior deltoids. Cables provide constant tension throughout the movement, which helps improve strength and muscle engagement across the full range of motion.",
            "Dumbbell Chest Fly is an isolation exercise that primarily targets the pectoralis major. Performed on a flat, incline, or decline bench, you begin with dumbbells held above your chest and arms slightly bent. You then lower the weights out to the sides in a wide arc while maintaining the same elbow angle, feeling a stretch across the chest, before bringing the dumbbells back together over your chest. This exercise focuses on chest muscle expansion and definition rather than heavy pressing strength.",
            "Dumbbell Chest Press is a compound upper-body exercise that primarily targets the pectoralis major, while also engaging the triceps and anterior deltoids. Lying on a flat, incline, or decline bench, you press two dumbbells upward from chest level until your arms are fully extended, then lower them back down in a controlled motion. Using dumbbells allows for a greater range of motion and helps improve muscular balance between both sides of the body.",
            "Incline Bench Press is a compound upper-body exercise that emphasizes the upper portion of the pectoralis major, while also working the anterior deltoids and triceps. Performed on a bench set at an incline angle (typically 30–45°), you lower the barbell to the upper chest in a controlled manner and then press it upward to full arm extension. This variation places more load on the upper chest compared to the flat bench press and contributes to a fuller, well-rounded chest development.",
            "Incline Dumbbell Press is a compound exercise that targets the upper chest (pectoralis major) while also engaging the triceps and anterior deltoids. Performed on a bench set at a 30–45° incline, you press two dumbbells upward from shoulder level until your arms are extended, then lower them back down with control. Using dumbbells increases the range of motion and helps improve muscular balance and stability in the upper body.",
            "Machine Chest Fly is an isolation exercise that primarily targets the pectoralis major with minimal involvement of the anterior deltoids and biceps as stabilizers. Sitting upright in a chest fly machine, you bring the handles together in front of your chest while keeping a slight bend in your elbows, then slowly return to the starting position. The guided path of the machine helps maintain proper form and constant tension on the chest muscles throughout the movement.",
            "Machine Chest Press is a compound exercise that primarily targets the pectoralis major, with secondary involvement of the triceps and anterior deltoids. Seated in a chest press machine, you push the handles forward and extend your arms, then return to the starting position in a controlled manner. The machine provides a fixed movement path, helping maintain proper form and consistent tension on the muscles, making it a beginner-friendly and safe option for building upper-body pushing strength.",
            "Smith Machine Bench Press is a compound upper-body exercise that targets the pectoralis major, with additional activation of the triceps and anterior deltoids. Performed on a flat bench under a guided bar path, you lower the bar to your chest and press it back up to full extension. The fixed trajectory of the Smith machine provides greater stability and control, allowing you to focus on muscle engagement and reduce the need for balance compared to a free-weight bench press.",
            "Machine Shoulder Press is a compound exercise that primarily targets the anterior deltoids, while also engaging the lateral deltoids and triceps. Seated in a shoulder press machine, you press the handles upward until your arms are extended overhead, then lower them back down with control. The guided motion of the machine helps maintain proper form and stability, making it effective and safe for building overhead pressing strength.",
            "Overhead Press is a compound upper-body exercise that primarily targets the deltoid muscles—especially the anterior and lateral heads—while also engaging the triceps and upper trapezius. Standing with a barbell at shoulder height, you press it overhead until your arms are fully extended, then lower it back down under control. This movement builds overall shoulder strength, stability, and upper-body power.",
            "Dumbbell Shoulder Press is a compound exercise that targets the deltoids—particularly the anterior and lateral heads—while also working the triceps and stabilizing muscles of the upper back. Performed seated or standing, you press two dumbbells overhead from shoulder level until your arms are fully extended, then lower them back down in a controlled motion. Using dumbbells enhances shoulder stability and helps correct strength imbalances between sides.",
            "Dumbbell Lateral Raise is an isolation exercise that primarily targets the lateral deltoids, helping to build shoulder width and a broader upper body look. Standing or seated with a dumbbell in each hand, you raise your arms out to the sides until they reach shoulder height while keeping a slight bend in your elbows, then lower them back down under control. This exercise focuses on controlled movement rather than heavy weight to effectively engage the side shoulders.",
            "Dumbbell Front Raise is an isolation exercise that mainly targets the anterior deltoids (front of the shoulders). Standing or seated with a dumbbell in each hand, you lift the weights straight in front of you to shoulder height while keeping your arms slightly bent, then lower them back down slowly. This movement helps develop front shoulder strength and definition, contributing to improved pressing performance and upper-body aesthetics.",
            "Cable Lateral Raise is an isolation exercise that primarily targets the lateral deltoids, helping create shoulder width and a stronger V-shape. Standing beside a low cable pulley, you lift the handle outward to the side until your arm reaches shoulder height while keeping a slight bend in the elbow, then lower it back under control. The cable provides constant tension throughout the movement, enhancing muscle activation compared to free weights.",
            "Cable Front Raise is an isolation exercise that focuses on the anterior deltoids (front shoulders). Standing facing away from or toward a low cable pulley, you lift the handle straight forward to shoulder height with a slight bend in your elbow, then lower it back slowly. The cable provides continuous tension throughout the movement, improving muscle engagement and control compared to using dumbbells.",
            "Barbell Upright Row is a compound upper-body exercise that primarily targets the lateral deltoids and upper trapezius, with additional involvement of the biceps and forearms. Standing upright with the barbell held in front of your thighs, you pull the bar straight up toward your collarbone, keeping it close to your body and elbows leading the movement, then lower it back down under control. This exercise helps build shoulder width and upper back strength.",
            "Barbell Behind-the-Neck Press is a compound shoulder exercise that targets the deltoids, especially the lateral and anterior heads, while also engaging the triceps and upper trapezius. Seated or standing with a barbell resting across the upper back, you press the bar overhead until your arms are fully extended, then lower it behind the head in a controlled motion. This variation increases shoulder activation but requires good shoulder mobility to perform safely.",
            "Kettlebell Press is a compound exercise that targets the deltoids, triceps, and upper back stabilizers. Performed standing (or seated), you press a kettlebell overhead from shoulder level until your arm is fully extended, then lower it back with control. The offset handle and weight distribution of the kettlebell require greater shoulder stability and core engagement, helping to develop balanced upper-body strength.",
            "Barbell Curl is an isolation exercise that primarily targets the biceps brachii, with additional involvement of the brachialis and forearms as stabilizers. Standing with a barbell held at arm’s length, you curl the weight upward by bending the elbows while keeping your upper arms stationary, then slowly lower it back down. This exercise is a classic movement for building biceps size and strength.",
            "Barbell Preacher Curl is an isolation exercise that targets the biceps brachii, with strong emphasis on the lower portion of the muscle and the brachialis. Performed while seated with your upper arms resting on a preacher bench, you curl the barbell upward while keeping the arms fixed against the pad, then lower it under control. The setup reduces momentum and helps maintain strict form, increasing focus on biceps activation.",
            "Dumbbell Curl is an isolation exercise that primarily targets the biceps brachii, with the brachialis and brachioradialis assisting. Standing or seated with a dumbbell in each hand, you curl the weights upward by bending your elbows while keeping your upper arms stable, then lower them back down slowly. Using dumbbells allows each arm to work independently, helping improve muscle balance and control.",
            "Dumbbell Preacher Curl is an isolation exercise that targets the biceps brachii, especially the lower portion of the muscle, with additional involvement of the brachialis. Seated with your upper arm supported on a preacher bench, you curl a dumbbell upward while keeping the arm fixed in place, then lower it back in a controlled motion. This setup limits momentum and increases biceps tension throughout the movement, helping improve muscle strength and definition.",
            "Hammer Curl is an isolation exercise that primarily targets the brachioradialis and brachialis, while also working the biceps brachii. Holding dumbbells with a neutral grip (palms facing each other), you curl the weights upward by bending your elbows while keeping your upper arms steady, then lower them back under control. This grip variation helps build forearm strength and overall arm thickness.",
            "Barbell Standing Triceps Extension is an isolation exercise that primarily targets the triceps brachii, especially the long head. Standing with a barbell held overhead, you lower the weight behind your head by bending your elbows, then extend your arms back to the starting position in a controlled motion. This exercise helps build triceps size and strength while improving overhead arm stability.",
            "Barbell Incline Triceps Extension is an isolation exercise that targets the triceps brachii, with strong emphasis on the long head due to the incline angle. Lying on an incline bench, you lower the barbell behind your head by bending your elbows, then extend your arms to lift the weight back up. The inclined position increases the stretch on the triceps, promoting greater muscle activation and growth.",
            "Tricep Pushdown With Bar is an isolation exercise that focuses on the triceps brachii, particularly the lateral and medial heads. Standing facing a cable machine with a straight or angled bar attachment, you extend your elbows to push the bar down until your arms are fully straight, then return to the starting position with control. The cable resistance keeps tension on the triceps throughout the movement, making it effective for strength and muscle development.",
            "Tricep Pushdown With Rope is an isolation exercise that primarily targets the triceps brachii, especially the lateral head. Standing at a cable machine with a rope attachment, you push the rope downward by extending your elbows, then separate the rope ends at the bottom of the movement before returning to the starting position with control. The rope allows for a greater range of motion and improved muscle contraction compared to a straight bar.",
            "Smith Machine Skull Crushers are an isolation exercise that targets the triceps brachii, especially the long head. Performed lying on a flat bench under the Smith machine bar, you lower the bar toward your forehead by bending your elbows, then extend your arms back to the starting position. The guided bar path improves stability and control, helping maintain proper form and constant tension on the triceps.",
            "Squat is a fundamental compound lower-body exercise that primarily targets the quadriceps, glutes, and hamstrings, while also engaging the core for stability. Standing with your feet shoulder-width apart, you bend your knees and hips to lower your body as if sitting back into a chair, then drive through your heels to return to a standing position. Squats help build strength, power, and muscle mass in the legs and improve overall functional movement.",
            "Barbell Lunge is a compound lower-body exercise that primarily targets the quadriceps and glutes, with additional activation of the hamstrings and core. With a barbell placed across your upper back, you step forward into a lunge position, lowering your back knee toward the floor while keeping your front knee aligned over your foot. You then push through the front heel to return to the starting position. This exercise helps improve leg strength, balance, and unilateral muscle development.",
            "Barbell Front Squat is a compound lower-body exercise that primarily targets the quadriceps, while also engaging the glutes, hamstrings, and core—especially the upper back to keep the torso upright. With the barbell resting on the front of your shoulders in the rack position, you squat down by bending your knees and hips, then drive back up through your heels to stand tall. The front-loaded position increases quad activation and encourages better posture compared to the traditional back squat.",
            "Leg Press is a compound lower-body exercise that primarily targets the quadriceps, with secondary activation of the glutes and hamstrings. Seated in a leg press machine with your feet on the platform, you push the weight away by extending your knees and hips, then lower it back in a controlled motion without letting your knees collapse inward. The machine provides stability, allowing you to safely use heavier loads to build leg strength and muscle mass.",
            "Seated Leg Curl is an isolation exercise that targets the hamstrings, with some involvement of the calves as stabilizers. Seated in a leg curl machine, you flex your knees to pull the pad downward toward the back of your legs, then slowly return to the starting position. This exercise helps build hamstring strength and improves stability and knee joint function.",
            "Barbell Rows are a compound back exercise that primarily targets the latissimus dorsi, rhomboids, and trapezius, while also engaging the lower back, biceps, and forearm muscles for pulling strength and stability. Bending at the hips with a flat back, you pull the barbell toward your torso, squeezing your shoulder blades together, then lower it back under control. This movement helps build a strong and thick upper back and improves overall pulling power.",
            "Deadlift is a full-body compound exercise that primarily targets the glutes, hamstrings, and lower back, while also engaging the upper back, core, and forearms. Starting with the barbell on the floor, you hinge at the hips and bend your knees to grip the bar, then stand up tall by driving through your legs and extending your hips, keeping your back straight. The deadlift is one of the most effective movements for developing strength, power, and overall muscle mass.",
            "Chin-Up is a compound upper-body pulling exercise that primarily targets the latissimus dorsi (lats) and biceps, with additional activation of the upper back and core. Hanging from a bar with an underhand (supinated) grip, you pull your body upward until your chin passes the bar, then lower yourself back down with control. This movement helps build back strength and arm muscle development.",
            "Muscle-Up is an advanced compound bodyweight exercise that combines a pull-up with a dip movement. Starting from a hanging position on a bar or rings, you explosively pull your body upward until your chest reaches above the bar, then transition by leaning forward and pressing down to lock out your arms at the top. This exercise primarily targets the lats, upper back, chest, triceps, and core, building upper-body strength, power, and coordination.",
            "Pull-Up is a compound upper-body exercise that primarily Targets the latissimus dorsi (lats), while also engaging the upper back, biceps, forearms, and core. Hanging from a bar with an overhand (pronated) grip, you pull your body upward until your chin rises above the bar, then lower yourself down under control. Pull-ups build significant back strength, grip power, and overall upper-body pulling capability.",
            "Hip Thrust is a compound lower-body exercise that primarily targets the gluteus maximus, with additional activation of the hamstrings and core. With your upper back supported on a bench and a barbell positioned over your hips, you drive through your heels to extend your hips upward until your body forms a straight line from shoulders to knees, then lower back down under control. This movement is one of the most effective exercises for building glute strength, size, and power.",
            "Cable Crunch is an abdominal isolation exercise that primarily targets the rectus abdominis, with involvement of the obliques as stabilizers. Kneeling in front of a cable machine while holding a rope attachment, you contract your abs to pull your elbows down toward your knees, then slowly return to the starting position while maintaining tension. This exercise emphasizes spinal flexion and helps build core strength and definition.",
            "Crunches are an abdominal isolation exercise that primarily targets the rectus abdominis (the “six-pack” muscle). Lying on your back with knees bent, you lift your shoulders and upper back off the floor by contracting your abs while keeping your lower back grounded, then lower yourself back down under control. This movement focuses on core flexion and helps improve abdominal strength and muscle definition."
        )

        val descriptionsPl = listOf(
            "Wyciskanie sztangi leżąc (Bench Press) to złożone ćwiczenie siłowe, które angażuje głównie mięsień piersiowy większy (klatka piersiowa), a także tricepsy i przednie aktony mięśni naramiennych (przednie barki). Ruch polega na leżeniu na płaskiej ławce, kontrolowanym opuszczaniu sztangi do klatki piersiowej, a następnie wypychaniu jej w górę do pełnego wyprostu ramion. Jest to jedno z najskuteczniejszych ćwiczeń do budowania siły i masy mięśniowej górnej części ciała.",
            "Pompki na poręczach (Bar Dips) to złożone ćwiczenie na górną część ciała, które angażuje głównie tricepsy, mięsień piersiowy większy (klatka piersiowa) i przednie aktony mięśni naramiennych (przednie barki). Ruch jest wykonywany na poręczach równoległych poprzez podpieranie ciała na wyprostowanych ramionach, a następnie opuszczanie tułowia poprzez zginanie łokci, aż barki znajdą się nieco poniżej łokci, i ponowne wypychanie ciała do pełnego wyprostu ramion. Ćwiczenie to pomaga budować siłę wypychania i masę mięśniową górnej części ciała.",
            "Wyciskanie na klatkę piersiową na maszynie Cable Chest Press to złożone ćwiczenie ukierunkowane na klatkę piersiową, wykonywane przy użyciu maszyny z linkami. Stojąc lub siedząc między dwoma wyciągami linkowymi, pchasz uchwyty do przodu w kontrolowanym ruchu, utrzymując łokcie lekko ugięte. Ćwiczenie angażuje głównie mięsień piersiowy większy, z wtórną aktywacją tricepsów i przednich aktonów mięśni naramiennych. Linki zapewniają stałe napięcie w całym ruchu, co pomaga poprawić siłę i zaangażowanie mięśni w pełnym zakresie ruchu.",
            "Rozpiętki z hantlami (Dumbbell Chest Fly) to ćwiczenie izolacyjne, które angażuje głównie mięsień piersiowy większy. Wykonywane na ławce płaskiej, skośnej w górę lub w dół, rozpoczynasz z hantlami trzymanymi nad klatką piersiową i lekko ugiętymi ramionami. Następnie opuszczasz ciężary na boki szerokim łukiem, utrzymując ten sam kąt ugięcia łokcia, czując rozciągnięcie w poprzek klatki piersiowej, po czym przyciągasz hantle z powrotem nad klatkę. Ćwiczenie to koncentruje się na rozszerzaniu i definicji mięśni klatki piersiowej, a nie na dużej sile wyciskania.",
            "Wyciskanie hantli leżąc (Dumbbell Chest Press) to złożone ćwiczenie na górną część ciała, które angażuje głównie mięsień piersiowy większy, a także tricepsy i przednie aktony mięśni naramiennych. Leżąc na ławce płaskiej, skośnej w górę lub w dół, wypychasz dwie hantle w górę z poziomu klatki piersiowej do pełnego wyprostu ramion, a następnie opuszczasz je z powrotem w kontrolowanym ruchu. Użycie hantli pozwala na większy zakres ruchu i pomaga poprawić równowagę mięśniową między obiema stronami ciała.",
            "Wyciskanie sztangi na ławce skośnej (Incline Bench Press) to złożone ćwiczenie na górną część ciała, które akcentuje górną część mięśnia piersiowego większego, angażując jednocześnie przednie aktony mięśni naramiennych i tricepsy. Wykonywane na ławce ustawionej pod kątem (zwykle 30–45°), opuszczasz sztangę do górnej części klatki piersiowej w kontrolowany sposób, a następnie wypychasz ją w górę do pełnego wyprostu ramion. Ta odmiana bardziej obciąża górną część klatki piersiowej w porównaniu do wyciskania na ławce płaskiej i przyczynia się do pełniejszego, wszechstronnego rozwoju klatki piersiowej.",
            "Wyciskanie hantli na ławce skośnej (Incline Dumbbell Press) to złożone ćwiczenie, które angażuje górną część klatki piersiowej (mięsień piersiowy większy), a także tricepsy i przednie aktony mięśni naramiennych. Wykonywane na ławce ustawionej pod kątem 30–45°, wypychasz dwie hantle w górę z poziomu barków do wyprostu ramion, a następnie opuszczasz je z powrotem z kontrolą. Użycie hantli zwiększa zakres ruchu i pomaga poprawić równowagę mięśniową i stabilność w górnej części ciała.",
            "Rozpiętki na maszynie (Machine Chest Fly) to ćwiczenie izolacyjne, które angażuje głównie mięsień piersiowy większy z minimalnym udziałem przednich aktonów mięśni naramiennych i bicepsów jako stabilizatorów. Siedząc prosto w maszynie do rozpiętek, ściągasz uchwyty razem przed klatką piersiową, utrzymując niewielkie ugięcie w łokciach, a następnie powoli wracasz do pozycji wyjściowej. Prowadzony tor maszyny pomaga utrzymać prawidłową formę i stałe napięcie w mięśniach klatki piersiowej przez cały ruch.",
            "Wyciskanie na klatkę na maszynie (Machine Chest Press) to złożone ćwiczenie, które angażuje głównie mięsień piersiowy większy, z wtórnym zaangażowaniem tricepsów i przednich aktonów mięśni naramiennych. Siedząc w maszynie do wyciskania na klatkę, pchasz uchwyty do przodu i prostujesz ramiona, a następnie wracasz do pozycji wyjściowej w kontrolowany sposób. Maszyna zapewnia ustalony tor ruchu, pomagając utrzymać prawidłową formę i stałe napięcie mięśni, co czyni ją opcją przyjazną dla początkujących i bezpieczną do budowania siły wypychania górnej części ciała.",
            "Wyciskanie sztangi na Smith'cie (Smith Machine Bench Press) to złożone ćwiczenie na górną część ciała, które angażuje mięsień piersiowy większy, z dodatkową aktywacją tricepsów i przednich aktonów mięśni naramiennych. Wykonywane leżąc na płaskiej ławce pod prowadzonym torem sztangi, opuszczasz sztangę do klatki piersiowej i wypychasz ją z powrotem do pełnego wyprostu. Stała trajektoria maszyny Smitha zapewnia większą stabilność i kontrolę, pozwalając skupić się na zaangażowaniu mięśni i zmniejszyć potrzebę równowagi w porównaniu do wyciskania sztangi wolnym ciężarem.",
            "Wyciskanie na barki na maszynie (Machine Shoulder Press) to złożone ćwiczenie, które angażuje głównie przednie aktony mięśni naramiennych, a także boczne aktony i tricepsy. Siedząc w maszynie do wyciskania na barki, wypychasz uchwyty w górę, aż ramiona będą w pełni wyprostowane nad głową, a następnie opuszczasz je z powrotem z kontrolą. Prowadzony ruch maszyny pomaga utrzymać prawidłową formę i stabilność, czyniąc ją skuteczną i bezpieczną do budowania siły wyciskania nad głową.",
            "Wyciskanie żołnierskie (Overhead Press) to złożone ćwiczenie na górną część ciała, które angażuje głównie mięśnie naramienne – zwłaszcza przednie i boczne aktony – a także tricepsy i górne aktony mięśni czworobocznych. Stojąc ze sztangą na wysokości barków, wypychasz ją nad głowę, aż ramiona będą w pełni wyprostowane, a następnie opuszczasz ją z powrotem z kontrolą. Ten ruch buduje ogólną siłę barków, stabilność i moc górnej części ciała.",
            "Wyciskanie hantli nad głowę (Dumbbell Shoulder Press) to złożone ćwiczenie, które angażuje mięśnie naramienne – w szczególności przednie i boczne aktony – a także tricepsy i mięśnie stabilizujące górnej części pleców. Wykonywane w pozycji siedzącej lub stojącej, wypychasz dwie hantle nad głowę z poziomu barków, aż ramiona będą w pełni wyprostowane, a następnie opuszczasz je z powrotem w kontrolowanym ruchu. Użycie hantli zwiększa stabilność barków i pomaga skorygować nierównowagę siły między stronami.",
            "Unoszenie hantli bokiem (Dumbbell Lateral Raise) to ćwiczenie izolacyjne, które angażuje głównie boczne aktony mięśni naramiennych, pomagając budować szerokość barków i szerszy wygląd górnej części ciała. Stojąc lub siedząc z hantlem w każdej dłoni, unosisz ramiona na boki, aż osiągną wysokość barków, utrzymując niewielkie ugięcie w łokciach, a następnie opuszczasz je z powrotem z kontrolą. Ćwiczenie to koncentruje się na kontrolowanym ruchu, a nie na dużym ciężarze, aby skutecznie zaangażować boczne aktony barków.",
            "Unoszenie hantli przed siebie (Dumbbell Front Raise) to ćwiczenie izolacyjne, które angażuje głównie przednie aktony mięśni naramiennych (przód barków). Stojąc lub siedząc z hantlem w każdej dłoni, podnosisz ciężary prosto przed siebie do wysokości barków, utrzymując ramiona lekko ugięte, a następnie powoli je opuszczasz. Ten ruch pomaga rozwijać siłę i definicję przedniej części barków, przyczyniając się do poprawy wyników w ćwiczeniach wyciskających i estetyki górnej części ciała.",
            "Unoszenie linki bokiem (Cable Lateral Raise) to ćwiczenie izolacyjne, które angażuje głównie boczne aktony mięśni naramiennych, pomagając stworzyć szerokość barków i mocniejszy kształt litery V. Stojąc obok niskiego wyciągu linkowego, podnosisz uchwyt na zewnątrz na bok, aż ramię osiągnie wysokość barku, utrzymując niewielkie ugięcie w łokciu, a następnie opuszczasz je z powrotem z kontrolą. Linka zapewnia stałe napięcie w całym ruchu, zwiększając aktywację mięśni w porównaniu do wolnych ciężarów.",
            "Unoszenie linki przed siebie (Cable Front Raise) to ćwiczenie izolacyjne, które koncentruje się na przednich aktonach mięśni naramiennych (przód barków). Stojąc tyłem lub przodem do niskiego wyciągu linkowego, podnosisz uchwyt prosto do przodu do wysokości barków z lekkim ugięciem w łokciu, a następnie powoli go opuszczasz. Linka zapewnia ciągłe napięcie w całym ruchu, poprawiając zaangażowanie mięśni i kontrolę w porównaniu do używania hantli.",
            "Wiosłowanie sztangą do brody (Barbell Upright Row) to złożone ćwiczenie na górną część ciała, które angażuje głównie boczne aktony mięśni naramiennych i górne aktony mięśni czworobocznych, z dodatkowym udziałem bicepsów i przedramion. Stojąc prosto ze sztangą trzymaną przed udami, ciągniesz sztangę prosto w górę w kierunku obojczyka, trzymając ją blisko ciała i prowadząc ruch łokciami, a następnie opuszczasz ją z powrotem z kontrolą. To ćwiczenie pomaga budować szerokość barków i siłę górnej części pleców.",
            "Wyciskanie sztangi za karku (Barbell Behind-the-Neck Press) to złożone ćwiczenie na barki, które angażuje mięśnie naramienne, zwłaszcza boczne i przednie aktony, a także tricepsy i górne aktony mięśni czworobocznych. Siedząc lub stojąc ze sztangą opartą na górnej części pleców, wypychasz sztangę nad głowę, aż ramiona będą w pełni wyprostowane, a następnie opuszczasz ją za głowę w kontrolowany sposób. Ta odmiana zwiększa aktywację barków, ale wymaga dobrej mobilności barków do bezpiecznego wykonania.",
            "Wyciskanie kettlebell (Kettlebell Press) to złożone ćwiczenie, które angażuje mięśnie naramienne, tricepsy i stabilizatory górnej części pleców. Wykonywane w pozycji stojącej (lub siedzącej), wypychasz kettlebell nad głowę z poziomu barków, aż ramię będzie w pełni wyprostowane, a następnie opuszczasz go z powrotem z kontrolą. Przesunięty uchwyt i rozkład ciężaru kettlebell wymagają większej stabilności barków i zaangażowania rdzenia, pomagając rozwijać zrównoważoną siłę górnej części ciała.",
            "Uginanie ramion ze sztangą (Barbell Curl) to ćwiczenie izolacyjne, które angażuje głównie mięsień dwugłowy ramienia (biceps), z dodatkowym udziałem mięśnia ramiennego i przedramion jako stabilizatorów. Stojąc ze sztangą trzymaną na wyciągnięcie ramion, uginasz ciężar w górę, zginając łokcie, utrzymując górne partie ramion nieruchomo, a następnie powoli opuszczasz go z powrotem. To ćwiczenie jest klasycznym ruchem do budowania rozmiaru i siły bicepsów.",
            "Uginanie ramion ze sztangą na modlitewniku (Barbell Preacher Curl) to ćwiczenie izolacyjne, które angażuje mięsień dwugłowy ramienia, z silnym akcentem na dolną część mięśnia i mięsień ramienny. Wykonywane w pozycji siedzącej z górnymi partiami ramion opartymi na modlitewniku, uginasz sztangę w górę, utrzymując ramiona nieruchomo przy podkładce, a następnie opuszczasz ją z kontrolą. Ustawienie to zmniejsza użycie pędu i pomaga utrzymać ścisłą formę, zwiększając koncentrację na aktywacji bicepsów.",
            "Uginanie ramion z hantlami (Dumbbell Curl) to ćwiczenie izolacyjne, które angażuje głównie mięsień dwugłowy ramienia, przy udziale mięśnia ramiennego i ramienno-promieniowego. Stojąc lub siedząc z hantlem w każdej dłoni, uginasz ciężary w górę, zginając łokcie, utrzymując górne partie ramion stabilnie, a następnie powoli je opuszczasz. Użycie hantli pozwala każdemu ramieniu pracować niezależnie, pomagając poprawić równowagę i kontrolę mięśniową.",
            "Uginanie ramion z hantlami na modlitewniku (Dumbbell Preacher Curl) to ćwiczenie izolacyjne, które angażuje mięsień dwugłowy ramienia, zwłaszcza dolną część mięśnia, z dodatkowym udziałem mięśnia ramiennego. Siedząc z górną częścią ramienia opartą na modlitewniku, uginasz hantel w górę, utrzymując ramię w miejscu, a następnie opuszczasz go z powrotem w kontrolowanym ruchu. To ustawienie ogranicza pęd i zwiększa napięcie bicepsów w całym ruchu, pomagając poprawić siłę i definicję mięśni.",
            "Uginanie ramion chwytem młotkowym (Hammer Curl) to ćwiczenie izolacyjne, które angażuje głównie mięsień ramienno-promieniowy i mięsień ramienny, pracując również na mięśniu dwugłowym ramienia. Trzymając hantle chwytem neutralnym (dłonie skierowane do siebie), uginasz ciężary w górę, zginając łokcie, utrzymując górne partie ramion stabilnie, a następnie opuszczasz je z powrotem z kontrolą. Ta odmiana chwytu pomaga budować siłę przedramion i ogólną grubość ramion.",
            "Prostowanie ramion ze sztangą w staniu (Barbell Standing Triceps Extension) to ćwiczenie izolacyjne, które angażuje głównie mięsień trójgłowy ramienia (triceps), zwłaszcza głowę długą. Stojąc ze sztangą trzymaną nad głową, opuszczasz ciężar za głowę, zginając łokcie, a następnie prostujesz ramiona z powrotem do pozycji wyjściowej w kontrolowanym ruchu. To ćwiczenie pomaga budować rozmiar i siłę tricepsów, jednocześnie poprawiając stabilność ramion nad głową.",
            "Prostowanie ramion ze sztangą na ławce skośnej (Barbell Incline Triceps Extension) to ćwiczenie izolacyjne, które angażuje triceps, z silnym akcentem na głowę długą z powodu kąta nachylenia. Leżąc na ławce skośnej, opuszczasz sztangę za głowę, zginając łokcie, a następnie prostujesz ramiona, aby podnieść ciężar z powrotem. Pozycja skośna zwiększa rozciągnięcie tricepsów, sprzyjając większej aktywacji i wzrostowi mięśni.",
            "Prostowanie ramion z drążkiem na wyciągu (Tricep Pushdown With Bar) to ćwiczenie izolacyjne, które koncentruje się na mięśniu trójgłowym ramienia, szczególnie na głowach bocznej i przyśrodkowej. Stojąc twarzą do maszyny z linkami z prostym lub kątowym drążkiem, prostujesz łokcie, aby pchnąć drążek w dół, aż ramiona będą w pełni wyprostowane, a następnie wracasz do pozycji wyjściowej z kontrolą. Opor z linki utrzymuje napięcie na tricepsach przez cały ruch, czyniąc go skutecznym dla rozwoju siły i mięśni.",
            "Prostowanie ramion z liną na wyciągu (Tricep Pushdown With Rope) to ćwiczenie izolacyjne, które angażuje głównie mięsień trójgłowy ramienia, zwłaszcza głowę boczną. Stojąc przy maszynie z linkami z uchwytem linowym, pchasz linę w dół, prostując łokcie, a następnie rozdzielasz końce liny na dole ruchu przed powrotem do pozycji wyjściowej z kontrolą. Lina pozwala na większy zakres ruchu i lepszą kontrakcję mięśni w porównaniu do prostego drążka.",
            "Wyciskanie francuskie na maszynie Smitha (Smith Machine Skull Crushers) to ćwiczenie izolacyjne, które angażuje triceps, zwłaszcza głowę długą. Wykonywane leżąc na płaskiej ławce pod sztangą maszyny Smitha, opuszczasz sztangę w kierunku czoła, zginając łokcie, a następnie prostujesz ramiona z powrotem do pozycji wyjściowej. Prowadzony tor sztangi poprawia stabilność i kontrolę, pomagając utrzymać prawidłową formę i stałe napięcie na tricepsach.",
            "Przysiad (Squat) to fundamentalne złożone ćwiczenie na dolną część ciała, które angażuje głównie mięśnie czworogłowe uda, pośladki i mięśnie dwugłowe uda, jednocześnie angażując rdzeń dla stabilności. Stojąc ze stopami rozstawionymi na szerokość barków, zginasz kolana i biodra, aby opuścić ciało, jakbyś siadał na krześle, a następnie wracasz do pozycji stojącej, pchając przez pięty. Przysiady pomagają budować siłę, moc i masę mięśniową w nogach oraz poprawiają ogólny ruch funkcjonalny.",
            "Wykrok ze sztangą (Barbell Lunge) to złożone ćwiczenie na dolną część ciała, które angażuje głównie mięśnie czworogłowe uda i pośladki, z dodatkową aktywacją mięśni dwugłowych uda i rdzenia. Ze sztangą umieszczoną na górnej części pleców, robisz krok do przodu w pozycję wykroku, opuszczając tylne kolano w kierunku podłogi, utrzymując przednie kolano w jednej linii nad stopą. Następnie pchasz przez przednią piętę, aby wrócić do pozycji wyjściowej. To ćwiczenie pomaga poprawić siłę nóg, równowagę i jednostronny rozwój mięśni.",
            "Przysiad przedni ze sztangą (Barbell Front Squat) to złożone ćwiczenie na dolną część ciała, które angażuje głównie mięśnie czworogłowe uda, jednocześnie angażując pośladki, mięśnie dwugłowe uda i rdzeń – zwłaszcza górną część pleców, aby utrzymać tułów w pionie. Ze sztangą opartą z przodu na barkach w pozycji stojaka, kucasz, zginając kolana i biodra, a następnie wracasz do pozycji stojącej, pchając przez pięty. Obciążenie z przodu zwiększa aktywację mięśnia czworogłowego i sprzyja lepszej postawie w porównaniu do tradycyjnego przysiadu tylnego.",
            "Wypychanie ciężaru na suwnicy (Leg Press) to złożone ćwiczenie na dolną część ciała, które angażuje głównie mięśnie czworogłowe uda, z wtórną aktywacją pośladków i mięśni dwugłowych uda. Siedząc w maszynie do wypychania nóg ze stopami na platformie, pchasz ciężar, prostując kolana i biodra, a następnie opuszczasz go z powrotem w kontrolowanym ruchu, nie pozwalając kolanom zapaść się do wewnątrz. Maszyna zapewnia stabilność, umożliwiając bezpieczne użycie większych obciążeń do budowania siły i masy mięśniowej nóg.",
            "Uginanie nóg siedząc na maszynie (Seated Leg Curl) to ćwiczenie izolacyjne, które angażuje mięśnie dwugłowe uda, z pewnym udziałem łydek jako stabilizatorów. Siedząc w maszynie do uginania nóg, zginasz kolana, aby pociągnąć podkładkę w dół w kierunku tyłu nóg, a następnie powoli wracasz do pozycji wyjściowej. To ćwiczenie pomaga budować siłę mięśni dwugłowych uda i poprawia stabilność oraz funkcję stawu kolanowego.",
            "Wiosłowanie sztangą (Barbell Rows) to złożone ćwiczenie na plecy, które angażuje głównie mięsień najszerszy grzbietu, równoległoboczne i czworoboczne, jednocześnie angażując dolną część pleców, bicepsy i mięśnie przedramion dla siły ciągnięcia i stabilności. Pochylając się w biodrach z płaskimi plecami, ciągniesz sztangę w kierunku tułowia, ściskając łopatki, a następnie opuszczasz ją z powrotem z kontrolą. Ten ruch pomaga budować silne i grube górne partie pleców oraz poprawia ogólną moc ciągnięcia.",
            "Martwy ciąg (Deadlift) to złożone ćwiczenie na całe ciało, które angażuje głównie pośladki, mięśnie dwugłowe uda i dolną część pleców, jednocześnie angażując górną część pleców, rdzeń i przedramiona. Zaczynając ze sztangą na podłodze, zginasz się w biodrach i kolanach, aby chwycić sztangę, a następnie wstajesz, napędzając ruch nogami i prostując biodra, utrzymując proste plecy. Martwy ciąg jest jednym z najskuteczniejszych ruchów do rozwijania siły, mocy i ogólnej masy mięśniowej.",
            "Podciąganie podchwytem (Chin-Up) to złożone ćwiczenie na górną część ciała typu 'ciągnięcie', które angażuje głównie mięsień najszerszy grzbietu (lats) i bicepsy, z dodatkową aktywacją górnej części pleców i rdzenia. Wisząc na drążku z podchwytem (supinacja), podciągasz ciało w górę, aż broda minie drążek, a następnie opuszczasz się z powrotem z kontrolą. Ten ruch pomaga budować siłę pleców i rozwój mięśni ramion.",
            "Muscle-Up to zaawansowane, złożone ćwiczenie z masą własnego ciała, które łączy podciąganie z ruchem dipów. Zaczynając z pozycji wiszącej na drążku lub kółkach, wybuchowo podciągasz ciało w górę, aż klatka piersiowa znajdzie się nad drążkiem, a następnie przechodzisz, pochylając się do przodu i wypychając w dół, aby zablokować ramiona na górze. To ćwiczenie angażuje głównie latsy, górną część pleców, klatkę piersiową, tricepsy i rdzeń, budując siłę, moc i koordynację górnej części ciała.",
            "Podciąganie nachwytem (Pull-Up) to złożone ćwiczenie na górną część ciała, które angażuje głównie mięsień najszerszy grzbietu (lats), a także górną część pleców, bicepsy, przedramiona i rdzeń. Wisząc na drążku z nachwytem (pronacja), podciągasz ciało w górę, aż broda wzniesie się nad drążek, a następnie opuszczasz się z kontrolą. Podciąganie buduje znaczną siłę pleców, siłę chwytu i ogólną zdolność ciągnięcia górnej części ciała.",
            "Hip Thrust to złożone ćwiczenie na dolną część ciała, które angażuje głównie mięsień pośladkowy wielki, z dodatkową aktywacją mięśni dwugłowych uda i rdzenia. Z górną częścią pleców opartą o ławkę i sztangą umieszczoną na biodrach, wypychasz biodra w górę, aż ciało utworzy prostą linię od barków do kolan, a następnie opuszczasz z powrotem z kontrolą. Ten ruch jest jednym z najskuteczniejszych ćwiczeń do budowania siły, rozmiaru i mocy pośladków.",
            "Spięcia brzucha z linką (Cable Crunch) to ćwiczenie izolacyjne na mięśnie brzucha, które angażuje głównie mięsień prosty brzucha, z udziałem mięśni skośnych jako stabilizatorów. Klęcząc przed maszyną z linkami, trzymając uchwyt linowy, napinasz mięśnie brzucha, aby przyciągnąć łokcie w dół do kolan, a następnie powoli wracasz do pozycji wyjściowej, utrzymując napięcie. To ćwiczenie akcentuje zgięcie kręgosłupa i pomaga budować siłę i definicję rdzenia.",
            "Spięcia brzucha (Crunches) to ćwiczenie izolacyjne na mięśnie brzucha, które angażuje głównie mięsień prosty brzucha (mięsień 'sześciopaka'). Leżąc na plecach z ugiętymi kolanami, podnosisz barki i górną część pleców z podłogi, kurcząc mięśnie brzucha, jednocześnie utrzymując dolną część pleców na ziemi, a następnie opuszczasz się z powrotem z kontrolą. Ten ruch koncentruje się na zgięciu rdzenia i pomaga poprawić siłę i definicję mięśni brzucha."
        )
        val exerciseNamesEn = listOf(
            "bench press",
            "bar dip",
            "cable chest press",
            "dumbbell chest fly",
            "dumbbell chest press",
            "incline bench press",
            "incline dumbbell press",
            "machine chest fly",
            "machine chest press",
            "smith machine bench press",
            "machine shoulder press",
            "overhead press",
            "dumbbell shoulder press",
            "dumbbell lateral raise",
            "dumbbell front raise",
            "cable lateral raise",
            "cable front raise",
            "barbell upright row",
            "barbell behind neck press",
            "kettlebell press",
            "barbell curl",
            "barbell preacher curl",
            "dumbbell curl",
            "dumbbell preacher curl",
            "hammer curl",
            "barbell standing triceps extension",
            "barbell incline triceps extension",
            "tricep pushdown with bar",
            "tricep pushdown with rope",
            "smith machine skull crushers",
            "squat",
            "barbell lunge",
            "barbell front squat",
            "leg press",
            "seated leg curl",
            "barbell rows",
            "deadlift",
            "chin-up",
            "muscle-up",
            "pull-up",
            "hip thrust",
            "cable crunch",
            "crunches"
        )
        val exerciseNamesPL = listOf(
            "wyciskanie sztangi na ławce płaskiej",
            "dipy na poręczach",
            "wyciskanie na maszynie wyciągowej",
            "rozpiętki z hantlami",
            "wyciskanie hantli na ławce",
            "wyciskanie sztangi na ławce skośnej",
            "wyciskanie hantli na ławce skośnej",
            "rozpiętki na maszynie",
            "wyciskanie na maszynie",
            "wyciskanie sztangi na ławce w maszynie Smitha",
            "wyciskanie na maszynie na barki",
            "wyciskanie sztangi nad głowę",
            "wyciskanie hantli nad głowę",
            "unoszenie hantli bokiem",
            "unoszenie hantli przodem",
            "unoszenie na boki na wyciągu",
            "unoszenie przodem na wyciągu",
            "wiosłowanie sztangą w opadzie",
            "wyciskanie sztangi zza głowy",
            "wyciskanie kettlebella nad głowę",
            "uginanie ramion ze sztangą",
            "uginanie ramion na modlitewniku ze sztangą",
            "uginanie ramion z hantlami",
            "uginanie ramion na modlitewniku z hantlą",
            "uginanie młotkowe",
            "prostowanie ramion ze sztangą stojąc",
            "prostowanie ramion na ławce skośnej",
            "prostowanie ramion na wyciągu z drążkiem",
            "prostowanie ramion na wyciągu z liną",
            "wyciskanie sztangi w leżeniu w maszynie Smitha (skull crushers)",
            "przysiad",
            "wypad z sztangą",
            "przysiad przedni ze sztangą",
            "wyciskanie nogami na maszynie",
            "uginanie nóg na maszynie siedząc",
            "wiosłowanie sztangą",
            "martwy ciąg",
            "podciąganie podchwytem",
            "muscle-up",
            "podciąganie nachwytem",
            "unoszenie bioder ze sztangą (hip thrust)",
            "skłony na wyciągu",
            "brzuszki"
        )



        val exercises = exerciseNamesEn.mapIndexed { index, name ->
            Exercise(
                name = name,
                description = descriptionsEn.getOrNull(index) ?: ""
            )
        }
        exerciseDao.insertExercises(exercises)

        val exercisesTranslation =
            exerciseNamesEn.mapIndexed { index, nameEn ->
                ExerciseTranslation(
                    exerciseId = index + 1,
                    languageId = 1,
                    name = nameEn,
                    description = descriptionsEn.getOrNull(index) ?: ""
                )
            } +
                    exerciseNamesPL.mapIndexed { index, namePl ->
                        ExerciseTranslation(
                            exerciseId = index + 1,
                            languageId = 2,
                            name = namePl,
                            description = descriptionsPl.getOrNull(index) ?: ""
                        )

                    }


        exerciseTranslationDao.insertAllExercisesTranslations(exercisesTranslation)

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
