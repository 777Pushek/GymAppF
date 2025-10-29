package com.example.gymappfrontendui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.models.ActiveWorkoutExercise
import com.example.gymappfrontendui.models.ActiveWorkoutSet
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import com.example.gymappfrontendui.viewmodel.WorkoutUiState
import com.example.gymappfrontendui.viewmodel.WorkoutViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithExercises
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.lazy.itemsIndexed

import com.example.gymappfrontendui.viewmodel.LoginRegistryViewModel
import kotlinx.coroutines.launch

@Composable
fun WorkoutScreen(
    modifier: Modifier = Modifier,
    workoutViewModel: WorkoutViewModel = viewModel(),
    exercisesViewModel: ExercisesViewModel = viewModel(),
    loginViewModel: LoginRegistryViewModel = viewModel()
) {
    val uiState by workoutViewModel.uiState.collectAsState()
    val premadeTemplates by workoutViewModel.premadeTemplates.collectAsState()
    val userTemplates by workoutViewModel.userTemplates.collectAsState()

    when (val state = uiState) {
        is WorkoutUiState.NotStarted -> {
            StartWorkoutScreen(
                modifier = modifier,
                onStartEmpty = { workoutViewModel.startWorkout() },
                premadeTemplates = premadeTemplates,
                userTemplates = userTemplates,
                onStartTemplate = { template ->
                    workoutViewModel.startWorkoutFromTemplate(template)
                },
                workoutViewModel = workoutViewModel,
                exercisesViewModel = exercisesViewModel,
                loginViewModel = loginViewModel
            )
        }
        is WorkoutUiState.InProgress -> {
            WorkoutInProgressScreen(
                state = state,
                modifier = modifier,
                exercisesViewModel = exercisesViewModel,
                workoutViewModel = workoutViewModel
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartWorkoutScreen(
    modifier: Modifier = Modifier,
    onStartEmpty: () -> Unit,
    premadeTemplates: List<WorkoutTemplateWithExercises>,
    userTemplates: List<WorkoutTemplateWithExercises>,
    onStartTemplate: (WorkoutTemplateWithExercises) -> Unit,
    workoutViewModel: WorkoutViewModel,
    exercisesViewModel: ExercisesViewModel,
    loginViewModel: LoginRegistryViewModel
) {
    var selectedTemplate by remember { mutableStateOf<WorkoutTemplateWithExercises?>(null) }
    var showCreateTemplateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Start workout",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = onStartEmpty,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Empty Workout", style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        }

        item {
            Text(
                text = "Templates",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (premadeTemplates.isEmpty()) {
            item {
                Text(
                    text = "No pre-made templates found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        } else {
            items(premadeTemplates, key = { "premade_${it.workoutTemplate.workoutTemplateId}" }) { template ->
                TemplateCard(
                    template = template,
                    onClick = { selectedTemplate = template }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Templates",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(onClick = { showCreateTemplateDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create new template",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (userTemplates.isEmpty()) {
            item {
                Text(
                    text = "You haven't created any templates yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        } else {
            items(userTemplates, key = { "user_${it.workoutTemplate.workoutTemplateId}" }) { template ->
                val scope = rememberCoroutineScope()
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            scope.launch {
                                workoutViewModel.deleteTemplate(template)
                            }
                            return@rememberSwipeToDismissBoxState true
                        }
                        return@rememberSwipeToDismissBoxState false
                    },
                    positionalThreshold = { pos -> pos * .25f }
                )
                LaunchedEffect(template.workoutTemplate.workoutTemplateId, dismissState.currentValue) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        dismissState.reset()
                    }
                }

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromEndToStart = true,
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
                    TemplateCard(
                        template = template,
                        onClick = { selectedTemplate = template }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    selectedTemplate?.let { template ->
        TemplateDetailsDialog(
            template = template,
            onDismiss = { selectedTemplate = null },
            onStart = {
                onStartTemplate(template)
                selectedTemplate = null
            }
        )
    }

    if (showCreateTemplateDialog) {
        val allExercises by exercisesViewModel.getAvailableExercises().collectAsState(initial = emptyList())
        CreateTemplateDialog(
            allExercises = allExercises,
            loginViewModel = loginViewModel,
            onDismiss = { showCreateTemplateDialog = false },
            onConfirm = { name, exercises, userId ->
                workoutViewModel.createTemplate(name, exercises, userId)
                showCreateTemplateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateCard(
    template: WorkoutTemplateWithExercises,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = template.workoutTemplate.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Template",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            val sortedExercises = template.exercises
                .sortedBy { it.workoutTemplateExercise.position }
                .take(3)

            if (sortedExercises.isNotEmpty()) {
                Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sortedExercises.forEach { detail ->
                        Row {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = detail.exerciseWithGroups.exercise.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (template.exercises.size > 3) {
                        Text(
                            text = "• ...and more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "No exercises in this template.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TemplateDetailsDialog(
    template: WorkoutTemplateWithExercises,
    onDismiss: () -> Unit,
    onStart: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Text(
                    text = template.workoutTemplate.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(24.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .wrapContentHeight()
                        .padding(horizontal = 24.dp)
                        .heightIn(max = 300.dp)
                ) {
                    val sortedExercises = template.exercises
                        .sortedBy { it.workoutTemplateExercise.position }

                    items(sortedExercises, key = { it.exerciseWithGroups.exercise.exerciseId }) { detail ->

                        val exerciseName = detail.exerciseWithGroups.exercise.name
                        val groups = detail.exerciseWithGroups.muscleGroups
                            .joinToString(", ") { it.name }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Exercise",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = exerciseName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (groups.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = groups,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onStart) {
                        Text("Start Workout")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutInProgressScreen(
    state: WorkoutUiState.InProgress,
    modifier: Modifier = Modifier,
    exercisesViewModel: ExercisesViewModel,
    workoutViewModel: WorkoutViewModel
) {
    var showExerciseSelection by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(state.exercises, key = { _, item -> item.exercise.exerciseId }) { index, workoutExercise: ActiveWorkoutExercise ->
                ActiveExerciseCard(
                    workoutExercise = workoutExercise,
                    onAddSet = { workoutViewModel.addSetToExercise(workoutExercise.exercise.exerciseId) },
                    onRemoveSet = { set -> workoutViewModel.removeSetFromExercise(workoutExercise.exercise.exerciseId, set) },
                    onRemoveExercise = { workoutViewModel.removeExerciseFromWorkout(workoutExercise.exercise.exerciseId) }
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showExerciseSelection = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Add Exercise")
                    }
                    Button(
                        onClick = { workoutViewModel.cancelWorkout() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Cancel Workout")
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
            onConfirm = { selectedExercises ->
                workoutViewModel.addExercisesToWorkout(selectedExercises)
                showExerciseSelection = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveExerciseCard(
    workoutExercise: ActiveWorkoutExercise,
    onAddSet: () -> Unit,
    onRemoveSet: (ActiveWorkoutSet) -> Unit,
    onRemoveExercise: () -> Unit
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
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(
                    workoutExercise.exercise.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Exercise",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onRemoveExercise() }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerColor = MaterialTheme.colorScheme.onSurfaceVariant
                Text("Set", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
                Text("Prev", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
                Text("Weight", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
                Text("Reps", modifier = Modifier.weight(1F), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = headerColor)
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
                                label = "background color animation set workout"
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
                                    contentDescription = "Delete Icon",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    ) {
                        WorkoutSetRow(set = set)
                    }
                }
            }

            Button(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add Set")
            }
        }
    }
}

@Composable
fun WorkoutSetRow(set: ActiveWorkoutSet) {
    var weight by remember(set.setId) { mutableStateOf(set.weight) }
    var reps by remember(set.setId) { mutableStateOf(set.reps) }
    var isCompleted by remember(set.setId) { mutableStateOf(set.isCompleted) }

    LaunchedEffect(weight) { if (set.weight != weight) set.weight = weight }
    LaunchedEffect(reps) { if (set.reps != reps) set.reps = reps }
    LaunchedEffect(isCompleted) { if (set.isCompleted != isCompleted) set.isCompleted = isCompleted }

    val rowColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
        label = "row color animation workout"
    )

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowColor)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(set.setNumber.toString(), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
        Text(
            text = set.previousPerformance.ifBlank { "-" },
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it.filter { char -> char.isDigit() || char == '.' } },
            modifier = Modifier.weight(1f).height(56.dp),
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
            modifier = Modifier.weight(1f).height(56.dp),
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
            onCheckedChange = { newCheckedState ->
                if (newCheckedState) {
                    val weightValue = weight.toDoubleOrNull() ?: 0.0
                    val repsValue = reps.toIntOrNull() ?: 0

                    if (weightValue > 0 && repsValue > 0) {
                        isCompleted = true
                    } else {
                        Toast.makeText(
                            context,
                            "Weight and reps must be greater than 0 to complete set",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    isCompleted = false
                }
            },
            modifier = Modifier.weight(0.5f).padding(0.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionList(
    modifier: Modifier = Modifier,
    allExercises: List<Exercise>,
    initiallySelected: List<Exercise>,
    onSelectionChanged: (List<Exercise>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedExercises = remember { mutableStateListOf<Exercise>().also { it.addAll(initiallySelected) } }

    LaunchedEffect(selectedExercises.size) {
        onSelectionChanged(selectedExercises.toList())
    }

    val filteredExercises = remember(searchQuery, allExercises) {
        if (searchQuery.isBlank()) {
            allExercises
        } else {
            allExercises.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }.sortedBy { it.name.lowercase() }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            placeholder = { Text("Search exercise...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            if (filteredExercises.isEmpty()) {
                item {
                    Text(
                        text = "No exercises found.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredExercises, key = { it.exerciseId }) { exercise ->
                    val isSelected = selectedExercises.contains(exercise)
                    ListItem(
                        headlineContent = { Text(exercise.name) },
                        modifier = Modifier.clickable {
                            if (isSelected) {
                                selectedExercises.remove(exercise)
                            } else {
                                selectedExercises.add(exercise)
                            }
                        },
                        leadingContent = {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
                        )
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}


@Composable
fun SelectExercisesDialog(
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onConfirm: (List<Exercise>) -> Unit
) {
    var currentSelection by remember { mutableStateOf<List<Exercise>>(emptyList()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f)) {
            Column {
                Text(
                    "Select Exercises",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                ExerciseSelectionList(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    allExercises = allExercises,
                    initiallySelected = emptyList(),
                    onSelectionChanged = { updatedSelection ->
                        currentSelection = updatedSelection
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(currentSelection) },
                        enabled = currentSelection.isNotEmpty()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTemplateDialog(
    allExercises: List<Exercise>,
    loginViewModel: LoginRegistryViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, List<Exercise>, Int?) -> Unit
) {
    var templateName by remember { mutableStateOf("") }
    var currentSelection by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val isFormValid by remember {
        derivedStateOf {
            templateName.isNotBlank() && currentSelection.isNotEmpty()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f)) {
            Column {
                Text(
                    "Create New Template",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    isError = templateName.isBlank() && currentSelection.isEmpty()
                )

                Text(
                    "Select Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp)
                )

                ExerciseSelectionList(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    allExercises = allExercises,
                    initiallySelected = emptyList(),
                    onSelectionChanged = { updatedSelection ->
                        currentSelection = updatedSelection
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                val uId = loginViewModel.getLoggedInUserID() ?: loginViewModel.getGuestUserId()
                                onConfirm(templateName, currentSelection, uId)
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}