package com.example.gymappfrontendui.screens


import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.db.entity.MuscleGroup
import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import com.example.gymappfrontendui.db.relationships.ExerciseWithMuscleGroups

import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseScreen(
    modifier: Modifier = Modifier,
    showAddDialogExternal: Boolean = false,
    onDialogClosed: () -> Unit,
    exercisesViewModel: ExercisesViewModel,
    loginViewModel: LoginRegistryViewModel
) {

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val exercisesWithGroups by exercisesViewModel.getAvailableExercisesWithMuscleGroups()
        .collectAsState(initial = emptyList())

    if (showAddDialogExternal && !showAddDialog) {
        showAddDialog = true
    }

    val filteredExercises = exercisesWithGroups.filter { exerciseWithGroups ->
        val exercise = exerciseWithGroups.exercise
        exercise.name.contains(searchQuery, ignoreCase = true) ||
                (exercise.description?.contains(searchQuery, ignoreCase = true) ?: false)
    }.sortedBy { it.exercise.name.lowercase() }


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Exercises",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search exercises...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            text = "Found: ${filteredExercises.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredExercises, key = { it.exercise.exerciseId }) { exerciseWithGroups ->

                val isCustom = exerciseWithGroups.exercise.userId != null
                val scope = rememberCoroutineScope()

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            scope.launch {
                                exercisesViewModel.deleteExercise(exerciseWithGroups.exercise)
                            }
                            return@rememberSwipeToDismissBoxState true
                        }
                        return@rememberSwipeToDismissBoxState false
                    },
                    positionalThreshold = { pos -> pos * .25f }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromEndToStart = isCustom,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val color by animateColorAsState(
                            targetValue = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                else -> Color.Transparent
                            },
                            label = "background color animation"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Icon",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                ) {
                    ExerciseItem(exerciseWithGroups = exerciseWithGroups) {
                        println("Clicked: ${exerciseWithGroups.exercise.name}")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            onDismiss = {
                showAddDialog = false
                onDialogClosed()
            },
            onConfirm = {
                showAddDialog = false
                onDialogClosed()
            },
            exercisesViewModel = exercisesViewModel,
            loginViewModel = loginViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseItem(exerciseWithGroups: ExerciseWithMuscleGroups, onClick: () -> Unit) {

    val exercise = exerciseWithGroups.exercise
    val muscleGroups = exerciseWithGroups.muscleGroups
    val isCustom = exercise.userId != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.heightIn(min = 24.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isCustom) {
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = "Custom",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.height(24.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val groupsText = muscleGroups.joinToString(", ") { it.name }
                Text(
                    text = if (groupsText.isEmpty()) "No assigned groups" else groupsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExerciseDialog(
    exercisesViewModel: ExercisesViewModel,
    loginViewModel: LoginRegistryViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val muscleGroups by exercisesViewModel.getAllMuscleGroups().collectAsState(initial = emptyList())

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedMuscleGroups by remember { mutableStateOf(emptySet<MuscleGroup>()) }
    var showError by remember { mutableStateOf(false) }
    var showInsertionError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add new exercise",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (showError) {
                    Text(
                        text = "Name and at least one muscle group are required.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (showInsertionError) {
                    Text(
                        text = "Error adding exercise. Please try again.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("Exercise name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true,
                    isError = showError && name.isBlank()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    minLines = 3,
                    maxLines = 5
                )

                Text(
                    text = "Select muscle groups",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(modifier = Modifier.heightIn(max=150.dp)) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        muscleGroups.forEach { group ->
                            val isSelected = selectedMuscleGroups.contains(group)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMuscleGroups = if (isSelected) {
                                        selectedMuscleGroups - group
                                    } else {
                                        selectedMuscleGroups + group
                                    }
                                    showError = false
                                },
                                label = { Text(group.name) }
                            )
                        }
                    }
                }
                if (showError && selectedMuscleGroups.isEmpty()) {
                    Text(
                        text = "Select at least one muscle group.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || selectedMuscleGroups.isEmpty()) {
                                showError = true
                                showInsertionError = false
                                return@Button
                            }
                            showError = false
                            showInsertionError = false
                            scope.launch {
                                val uId = loginViewModel.getLoggedInUserID() ?: loginViewModel.getGuestUserId()
                                val exerciseToInsert = Exercise(
                                    userId = uId,
                                    name = name,
                                    description = description,
                                )
                                try {
                                    exercisesViewModel.insertExercise(exerciseToInsert,selectedMuscleGroups.map { it.muscleGroupId })
                                    onConfirm()
                                } catch (e: Exception) {
                                    showInsertionError = true
                                    Log.e("AddExerciseDialog", "Error inserting exercise", e)
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}