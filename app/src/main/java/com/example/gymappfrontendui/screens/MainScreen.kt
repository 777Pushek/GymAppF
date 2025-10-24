package com.example.gymappfrontendui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.screens.NavItem
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import com.example.gymappfrontendui.viewmodel.WorkoutUiState
import com.example.gymappfrontendui.viewmodel.WorkoutViewModel
import androidx.compose.material3.AlertDialog
import com.example.gymappfrontendui.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier, login: String, loginViewModel: LoginRegistryViewModel,
               exercisesViewModel: ExercisesViewModel
)
{

    val navItemList = listOf(
        NavItem(
            label = "Home",
            icon = Icons.Default.Home,
        ),
        NavItem(
            label = "Workout",
            icon = Icons.Filled.Add,
        ),
        NavItem(
            label = "Profile",
            icon = Icons.Default.AccountCircle,
        ),
        NavItem(
            label = "Exercises",
            icon = Icons.Default.Build,
        ),
        NavItem(
            label = "History",
            icon = Icons.Default.Menu,
        ),
    )
    var selectedIndex by remember {
        mutableIntStateOf(0)
    }
    var exerciseAddRequested by remember { mutableStateOf(false) }

    val workoutViewModel: WorkoutViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()


    val workoutState by workoutViewModel.uiState.collectAsState()

    val showConfirmationDialog by workoutViewModel.showConfirmationDialog.collectAsState()

    if (showConfirmationDialog) {
        IncompleteWorkoutDialog(
            onConfirm = { workoutViewModel.confirmSaveChanges() },
            onDismiss = { workoutViewModel.dismissConfirmationDialog() }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (selectedIndex == 1 && workoutState is WorkoutUiState.InProgress) {
                        Text("Workout in Progress")
                    } else {
                        Text("GymApp")
                    }
                },
                actions = {
                    if (selectedIndex == 1 && workoutState is WorkoutUiState.InProgress) {
                        val state = workoutState as WorkoutUiState.InProgress
                        Text(
                            text = workoutViewModel.formatElapsedTime(state.elapsedTime),
                            modifier = Modifier.padding(end = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { workoutViewModel.saveWorkout() },
                            enabled = state.exercises.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        ) {
                            Text("SAVE", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = { selectedIndex = index },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = "Icon") },
                        label = { Text(navItem.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedIndex == 3) {
                FloatingActionButton(
                    onClick = { exerciseAddRequested = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj ćwiczenie")
                }
            }
        }

    ) { innerPadding ->
        ContentScreen(modifier.padding(innerPadding), selectedIndex, login,onAddClick = {exerciseAddRequested = true} ,exerciseAddRequested, onDialogClosed = {exerciseAddRequested = false},loginViewModel = loginViewModel, // Przekaż ViewModele
            exercisesViewModel = exercisesViewModel,navController = navController, workoutViewModel = workoutViewModel, historyViewModel = historyViewModel)
    }
}

@Composable
fun WorkoutBottomBar(
    workoutState: WorkoutUiState.InProgress,
    workoutViewModel: WorkoutViewModel
) {
    BottomAppBar {
        Button(
            onClick = { workoutViewModel.cancelWorkout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {
            Text("CANCEL")
        }
        Button(
            onClick = { workoutViewModel.saveWorkout() },
            enabled = workoutState.exercises.isNotEmpty(),
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
        ) {
            Text("SAVE WORKOUT")
        }
    }
}

@Composable
fun IncompleteWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unfinished Sets") },
        text = { Text("You have uncompleted sets. If you finish the workout now, they will be deleted. Are you sure you want to continue?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex : Int, login: String,onAddClick: () -> Unit,exerciseAddRequested : Boolean,onDialogClosed: () -> Unit,loginViewModel: LoginRegistryViewModel, // Dodaj parametry
                  exercisesViewModel: ExercisesViewModel,navController: NavController, workoutViewModel: WorkoutViewModel, historyViewModel: HistoryViewModel)
{
    when(selectedIndex) {
        0 -> HomePage(
            login = login,
            modifier = modifier
        )
        1 -> WorkoutScreen(
            modifier = modifier,
            exercisesViewModel = exercisesViewModel,
            workoutViewModel = workoutViewModel
        )
        2 -> ProfileScreen(
            modifier = modifier,
            loginViewModel = loginViewModel,
            navController = navController
        )
        3 -> ExerciseScreen(
            modifier = modifier,
            showAddDialogExternal = exerciseAddRequested,
            onDialogClosed = onDialogClosed,
            exercisesViewModel = exercisesViewModel,
            loginViewModel = loginViewModel
        )
        4 -> HistoryScreen(
            modifier = modifier,
            historyViewModel = historyViewModel
            ,navController = navController
        )


    }

}