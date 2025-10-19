package com.example.gymappfrontendui.screens

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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.models.ActiveWorkoutExercise
import com.example.gymappfrontendui.models.ActiveWorkoutSet
import com.example.gymappfrontendui.viewmodel.ExercisesViewModel
import com.example.gymappfrontendui.viewmodel.WorkoutUiState
import com.example.gymappfrontendui.viewmodel.WorkoutViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.example.gymappfrontendui.db.relationships.WorkoutTemplateWithExercises
import androidx.compose.material.icons.filled.List
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
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = onStartEmpty,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Empty Workout", fontSize = 18.sp)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp))
        }

        item {
            Text(
                text = "Templates",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (premadeTemplates.isEmpty()) {
            item {
                Text(
                    text = "No pre-made templates found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Templates",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
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
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                    positionalThreshold = { it * .25f }
                )
                LaunchedEffect(dismissState.currentValue) {
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Template",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(start = 8.dp)) {
                val sortedExercises = template.exercises
                    .sortedBy { it.workoutTemplateExercise.position }
                    .take(3)

                sortedExercises.forEach { detail ->
                    Row {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = detail.exerciseWithGroups.exercise.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (template.exercises.size > 3) {
                    Text(
                        text = "• ...and more",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        itemsIndexed(state.exercises, key = { _, item -> item.exercise.exerciseId }) { index, workoutExercise ->
            ActiveExerciseCard(
                workoutExercise = workoutExercise,
                onAddSet = { workoutViewModel.addSetToExercise(workoutExercise.exercise.exerciseId) },
                onRemoveSet = { set -> workoutViewModel.removeSetFromExercise(workoutExercise.exercise.exerciseId, set) },
                onRemoveExercise = { workoutViewModel.removeExerciseFromWorkout(workoutExercise.exercise.exerciseId) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Button(
                onClick = { showExerciseSelection = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Exercise")
            }
            Spacer(modifier = Modifier.height(8.dp))

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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(workoutExercise.exercise.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
                Text("Set", modifier = Modifier.weight(0.5f), fontWeight = FontWeight.SemiBold)
                Text("Prev", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text("Weight", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Text("Reps", modifier = Modifier.weight(1F), fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.weight(0.5f))
            }
            Divider(modifier = Modifier.padding(vertical = 4.dp))

            workoutExercise.sets.forEach { set ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onRemoveSet(set)
                            return@rememberSwipeToDismissBoxState true
                        }
                        return@rememberSwipeToDismissBoxState false
                    },
                    positionalThreshold = { it * .25f }
                )

                LaunchedEffect(dismissState.currentValue) {
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
                            label = "background color animation"
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onAddSet, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Add Set")
            }
        }
    }
}

@Composable
fun WorkoutSetRow(set: ActiveWorkoutSet) {
    var weight by remember { mutableStateOf(set.weight) }
    var reps by remember { mutableStateOf(set.reps) }
    var isCompleted by remember { mutableStateOf(set.isCompleted) }

    val rowColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFFC8E6C9) else MaterialTheme.colorScheme.surface,
        label = "row color animation"
    )

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
        Text(set.previousPerformance, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)

        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                set.weight = it
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it
                set.reps = it
            },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Checkbox(
            checked = isCompleted,
            onCheckedChange = {
                isCompleted = it
                set.isCompleted = it
            },
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
fun SelectExercisesDialog(
    allExercises: List<Exercise>,
    onDismiss: () -> Unit,
    onConfirm: (List<Exercise>) -> Unit
) {
    val selectedExercises = remember { mutableStateListOf<Exercise>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.8f)) {
            Column {
                Text("Select Exercises", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allExercises, key = { it.exerciseId }) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedExercises.contains(exercise)) {
                                        selectedExercises.remove(exercise)
                                    } else {
                                        selectedExercises.add(exercise)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedExercises.contains(exercise),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(selectedExercises.toList()) }) {
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
    val selectedExercises = remember { mutableStateListOf<Exercise>() }

    val scope = rememberCoroutineScope()

    val isFormValid by remember {
        derivedStateOf {
            templateName.isNotBlank() && selectedExercises.isNotEmpty()
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
                    isError = templateName.isBlank()
                )

                Text(
                    "Select Exercises",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allExercises, key = { it.exerciseId }) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedExercises.contains(exercise)) {
                                        selectedExercises.remove(exercise)
                                    } else {
                                        selectedExercises.add(exercise)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedExercises.contains(exercise),
                                onCheckedChange = null
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

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

                                onConfirm(templateName, selectedExercises.toList(), uId)
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