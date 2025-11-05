package com.example.gymappfrontendui.screens

import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.models.DayOfWeek
import com.example.gymappfrontendui.viewmodel.ScheduleViewModel
import com.example.gymappfrontendui.viewmodel.ScheduledWorkoutWithTemplate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ScheduleDetailScreen(
    scheduleId: Int,
    navController: NavController,
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    val detailState by scheduleViewModel.scheduleDetailState.collectAsState()
    val availableTemplates by scheduleViewModel.availableTemplates.collectAsState()

    var showAddWorkoutDialog by remember { mutableStateOf(false) }
    var selectedDayForDialog by remember { mutableStateOf(DayOfWeek.MONDAY) }

    val daysOfWeek = remember { DayOfWeek.values().toList() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }



    LaunchedEffect(scheduleId) {
        scheduleViewModel.loadScheduleDetails(scheduleId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detailState.schedule?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (detailState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (detailState.schedule == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Schedule not found.")
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                ScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                    daysOfWeek.forEachIndexed { index, day ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(day.name.substring(0, 3).uppercase(Locale.getDefault())) }
                        )
                    }
                }

                val currentDay = daysOfWeek[selectedTabIndex]
                val workoutsForDay = detailState.scheduledWorkouts[currentDay] ?: emptyList()

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    if (workoutsForDay.isEmpty()) {
                        item {
                            Text(
                                "No workouts scheduled for ${currentDay.name.lowercase().replaceFirstChar { it.titlecase() }}.",
                                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(workoutsForDay, key = { it.scheduledWorkout.scheduledWorkoutId }) { workoutWithName ->
                            ScheduledWorkoutItem(
                                workout = workoutWithName,
                                onDelete = { scheduleViewModel.deleteScheduledWorkout(workoutWithName.scheduledWorkout) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedDayForDialog = currentDay
                                showAddWorkoutDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Add Workout to ${currentDay.name.lowercase().replaceFirstChar { it.titlecase() }}")
                        }
                    }
                }
            }
        }
    }

    if (showAddWorkoutDialog) {
        AddScheduledWorkoutDialog(
            availableTemplates = availableTemplates,
            day = selectedDayForDialog,
            onDismiss = { showAddWorkoutDialog = false },
            onConfirm = { templateId, time ->
                scheduleViewModel.addScheduledWorkout(scheduleId, templateId, selectedDayForDialog, time)
                showAddWorkoutDialog = false
            }
        )
    }
}
@Composable
fun ScheduledWorkoutItem(
    workout: ScheduledWorkoutWithTemplate,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.templateName ?: "Unknown Template",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                workout.scheduledWorkout.time?.let { timeString ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = try {
                            LocalTime.parse(timeString).format(DateTimeFormatter.ofPattern("HH:mm"))
                        } catch (e: DateTimeParseException) {
                            timeString
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } ?: run {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No time set",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete scheduled workout", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduledWorkoutDialog(
    availableTemplates: List<WorkoutTemplate>,
    day: DayOfWeek,
    onDismiss: () -> Unit,
    onConfirm: (templateId: Int, time: LocalTime?) -> Unit
) {
    var selectedTemplateId by remember { mutableStateOf<Int?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(LocalTime.of(9, 0)) }
    var showTimePicker by remember { mutableStateOf(false) }
    var templateError by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 9,
        initialMinute = selectedTime?.minute ?: 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Workout to ${day.name.lowercase().replaceFirstChar { it.titlecase() }}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                var expanded by remember { mutableStateOf(false) }
                val selectedTemplateName = availableTemplates.find { it.workoutTemplateId == selectedTemplateId }?.name ?: "Select Template"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedTemplateName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Workout Template") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        isError = templateError,
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (availableTemplates.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No templates available") },
                                onClick = { expanded = false },
                                enabled = false
                            )
                        } else {
                            availableTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        selectedTemplateId = template.workoutTemplateId
                                        templateError = false
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                if (templateError) {
                    Text(
                        "Please select a template",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = selectedTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "No time set",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Time") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedTime != null) {
                                IconButton(onClick = { selectedTime = null }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear Time")
                                }
                            }
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Time")
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedTemplateId != null) {
                    onConfirm(selectedTemplateId!!, selectedTime)
                } else {
                    templateError = true
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    content()
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}