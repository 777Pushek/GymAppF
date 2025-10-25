package com.example.gymappfrontendui

import AppNavigationRouter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymappfrontendui.screens.BodyMeasurementProgressScreen
import com.example.gymappfrontendui.screens.BodyMeasurementsScreen
import com.example.gymappfrontendui.screens.EditWorkoutHistoryScreen
import com.example.gymappfrontendui.screens.LoginScreen
import com.example.gymappfrontendui.screens.MainScreen
import com.example.gymappfrontendui.screens.RegisterScreen
import com.example.gymappfrontendui.screens.WorkoutProgressScreen
import com.example.gymappfrontendui.ui.theme.GymAppFrontendUITheme
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel

class MainActivity : ComponentActivity() {
    val loginViewModel by viewModels<LoginRegistryViewModel> ()
    val exercisesViewModel by viewModels<ExercisesViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
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
                                exercisesViewModel = exercisesViewModel
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
                    }
                }
            }
        }
    }
}