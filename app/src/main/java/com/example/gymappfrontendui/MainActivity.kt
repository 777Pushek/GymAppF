package com.example.gymappfrontendui

import AppNavigationRouter
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymappfrontendui.screens.BodyMeasurementProgressScreen
import com.example.gymappfrontendui.screens.BodyMeasurementsScreen
import com.example.gymappfrontendui.screens.EditWorkoutHistoryScreen
import com.example.gymappfrontendui.screens.ForgotPasswordFlowScreen
import com.example.gymappfrontendui.screens.LoginScreen
import com.example.gymappfrontendui.screens.MainScreen
import com.example.gymappfrontendui.screens.RegisterScreen
import com.example.gymappfrontendui.screens.ScheduleDetailScreen
import com.example.gymappfrontendui.screens.ScheduleListScreen
import com.example.gymappfrontendui.screens.WorkoutProgressScreen
import com.example.gymappfrontendui.ui.theme.GymAppFrontendUITheme
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel
import com.example.gymappfrontendui.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    val loginViewModel by viewModels<LoginRegistryViewModel> ()
    val exercisesViewModel by viewModels<ExercisesViewModel>()

    val homeViewModel by viewModels<HomeViewModel>()
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled 💪", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notifications permission denied ⚠️", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        askForNotificationPermission()
        enableEdgeToEdge()
        setContent {
            GymAppFrontendUITheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = Routes.AppRouter) {
                        composable (Routes.AppRouter)
                        {
                            AppNavigationRouter(navController, loginViewModel)
                        }
                        composable(Routes.LoginPage)
                        {
                            LoginScreen(navController, loginViewModel)
                        }
                        composable(Routes.RegisterPage)
                        {
                            RegisterScreen(navController, loginViewModel)
                        }
                        composable(Routes.MainScreen + "/{login}")
                        {
                            val login = it.arguments?.getString("login")
                            MainScreen(
                                navController = navController,
                                login = login.toString(),
                                loginViewModel = loginViewModel,
                                exercisesViewModel = exercisesViewModel,
                                homeViewModel = homeViewModel
                            )
                        }
                        composable(
                            route = Routes.EditWorkoutHistoryFullRoute,
                            arguments = listOf(navArgument(Routes.EditWorkoutHistoryArgId) {
                                type = NavType.IntType
                            })
                        ) {
                            EditWorkoutHistoryScreen(
                                navController = navController,
                            )
                        }
                        composable(Routes.BodyMeasurementsScreen) {
                            BodyMeasurementsScreen(navController = navController)
                        }
                        composable(Routes.WorkoutProgressScreen) {
                            WorkoutProgressScreen(navController = navController)
                        }
                        composable(Routes.BodyMeasurementProgressScreen) {
                            BodyMeasurementProgressScreen(navController = navController)
                        }
                        composable(Routes.ScheduleList) {
                            ScheduleListScreen(navController = navController)
                        }
                        composable(
                            route = Routes.ScheduleDetail,
                            arguments = listOf(navArgument("scheduleId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val scheduleId = backStackEntry.arguments?.getInt("scheduleId")
                            if (scheduleId != null) {
                                ScheduleDetailScreen(scheduleId = scheduleId, navController = navController)
                            } else {
                                Text("Error: Schedule ID not found.")
                            }
                        }
                        composable(Routes.ForgotPasswordFlow) {
                            ForgotPasswordFlowScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Please allow notifications to receive workout reminders 💪",
                    Toast.LENGTH_LONG
                ).show()
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}