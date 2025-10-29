@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gymappfrontendui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymappfrontendui.models.ActiveWorkoutExercise
import com.example.gymappfrontendui.models.ActiveWorkoutSet
import com.example.gymappfrontendui.viewmodel.EditWorkoutHistoryViewModel
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutHistoryScreen(
    navController: NavController,
    editViewModel: EditWorkoutHistoryViewModel = viewModel(),
    exercisesViewModel: ExercisesViewModel = viewModel()
) {
    val state by editViewModel.editState.collectAsState()
    var showExerciseSelection by remember { mutableStateOf(false) }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Workout History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.date,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = state.duration,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(
                        items = state.exercises,
                        key = { _, item -> item.workoutExerciseId.takeIf { it != 0 } ?: item.exercise.exerciseId }
                    ) { index, workoutExercise ->
                        EditableActiveExerciseCard(
                            workoutExercise = workoutExercise,
                            onAddSet = { editViewModel.addSetToExercise(workoutExercise.exercise.exerciseId) },
                            onRemoveSet = { set -> editViewModel.removeSetFromExercise(workoutExercise.exercise.exerciseId, set) }
                        )
                    }

                    item {
                        Button(
                            onClick = { showExerciseSelection = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Exercise Icon", modifier = Modifier.size(ButtonDefaults.IconSize))
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Add Exercise")
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        editViewModel.saveChanges()
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }


    if (showExerciseSelection) {
        val allExercises by exercisesViewModel.getAvailableExercises().collectAsState(initial = emptyList())
        SelectExercisesDialog(
            allExercises = allExercises,
            onDismiss = { showExerciseSelection = false },
            onConfirm = { selectedExercises: List<Exercise> ->
                editViewModel.addExercisesToHistory(selectedExercises)
                showExerciseSelection = false
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableActiveExerciseCard(
    workoutExercise: ActiveWorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (ActiveWorkoutSet) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                workoutExercise.exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerColor = MaterialTheme.colorScheme.onSurfaceVariant
                Text("Set", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
                Text("Weight", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
                Text("Reps", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
                Text("✓", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                workoutExercise.sets.forEach { set ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onRemoveSet(set)
                                return@rememberSwipeToDismissBoxState true
                            }
                            return@rememberSwipeToDismissBoxState false
                        },
                        positionalThreshold = { pos -> pos * .25f }
                    )

                    LaunchedEffect(set.setId, dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = when(dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    else -> Color.Transparent
                                },
                                label = "background color animation set edit"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Set Icon",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    ) {
                        EditableWorkoutSetRow(set = set)
                    }
                }
            }

            Button(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            ) {
                Text("Add Set")
            }
        }
    }
}


@Composable
fun EditableWorkoutSetRow(set: ActiveWorkoutSet) {
    var weight by remember(set.setId) { mutableStateOf(set.weight) }
    var reps by remember(set.setId) { mutableStateOf(set.reps) }
    var isCompleted by remember(set.setId) { mutableStateOf(set.isCompleted) }

    LaunchedEffect(weight) { if (set.weight != weight) set.weight = weight }
    LaunchedEffect(reps) { if (set.reps != reps) set.reps = reps }
    LaunchedEffect(isCompleted) { if (set.isCompleted != isCompleted) set.isCompleted = isCompleted }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(set.setNumber.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it.filter { char -> char.isDigit() || char == '.' } },
            modifier = Modifier
                .weight(1.2f)
                .height(56.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it.filter { char -> char.isDigit() } },
            modifier = Modifier
                .weight(1.2f)
                .height(56.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { isCompleted = it },
            modifier = Modifier.weight(0.5f).padding(0.dp)
        )
    }
}