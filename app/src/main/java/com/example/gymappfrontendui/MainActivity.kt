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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gymappfrontendui.screens.LoginScreen
import com.example.gymappfrontendui.screens.MainScreen
import com.example.gymappfrontendui.screens.RegisterScreen
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
            GymAppFrontendUITheme {
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
                                navController, login = login.toString(),
                                loginViewModel = loginViewModel,
                                exercisesViewModel = exercisesViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}