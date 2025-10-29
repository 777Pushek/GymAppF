package com.example.gymappfrontendui

object Routes {
    var LoginPage = "Login_Page"
    var RegisterPage = "Register_Page"

    var MainScreen = "Main_Screen"

    var AppRouter = "app_router"

    var EditWorkoutHistoryScreen = "edit_workout_history"
    var EditWorkoutHistoryArgId = "workoutId"

    var EditWorkoutHistoryFullRoute = "$EditWorkoutHistoryScreen/{$EditWorkoutHistoryArgId}"

    var BodyMeasurementsScreen = "body_measurements"

    var WorkoutProgressScreen = "workout_progress"

    var BodyMeasurementProgressScreen = "body_measurement_progress"

    var ScheduleList = "schedule_list"
    var ScheduleDetail = "schedule_detail/{scheduleId}"
    fun scheduleDetailRoute(scheduleId: Int) = "schedule_detail/$scheduleId"
    var ForgotPasswordFlow = "forgot_password_flow"
}

