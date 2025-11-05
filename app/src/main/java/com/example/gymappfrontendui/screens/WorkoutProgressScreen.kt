package com.example.gymappfrontendui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.viewmodel.ChartMetric
import com.example.gymappfrontendui.viewmodel.DateRangeFilter
import com.example.gymappfrontendui.viewmodel.WorkoutProgressViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutProgressScreen(
    navController: NavController,
    viewModel: WorkoutProgressViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Progress") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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

        if (showStartDatePicker) {
            CustomDatePickerDialog(
                onDismiss = { showStartDatePicker = false },
                onDateSelected = { date ->
                    viewModel.selectCustomStartDate(date)
                },
                maxDate = state.customEndDate
            )
        }

        if (showEndDatePicker) {
            CustomDatePickerDialog(
                onDismiss = { showEndDatePicker = false },
                onDateSelected = { date ->
                    viewModel.selectCustomEndDate(date)
                },
                minDate = state.customStartDate
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ExerciseSelector(
                exercises = state.exercises,
                selectedExerciseId = state.selectedExerciseId,
                onExerciseSelected = viewModel::selectExercise,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            DateRangeFilterSelector(
                currentFilter = state.currentFilter,
                onFilterSelected = viewModel::setDateFilter,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.currentFilter == DateRangeFilter.CUSTOM) {
                Spacer(modifier = Modifier.height(16.dp))
                CustomDateRangeSelectors(
                    startDate = state.customStartDate?.format(dateFormatter) ?: "Start Date",
                    endDate = state.customEndDate?.format(dateFormatter) ?: "End Date",
                    onStartDateClick = { showStartDatePicker = true },
                    onEndDateClick = { showEndDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            ChartMetricSelector(
                selectedMetric = state.selectedChartMetric,
                onMetricSelected = viewModel::selectChartMetric,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "An error occurred.",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (state.chartValues.isEmpty() && state.selectedExerciseId != null) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val message = if (state.currentFilter == DateRangeFilter.CUSTOM) {
                        "No data found for the selected custom range."
                    } else {
                        "No data for ${state.selectedExerciseName} in this period."
                    }
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else if (state.chartValues.isNotEmpty()) {
                AppLineChart(
                    values = state.chartValues,
                    labels = state.chartXAxisLabels,
                    metricLabel = state.selectedChartMetric.displayName
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Select an exercise to see progress",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Personal Record (${state.selectedExerciseName})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.personalRecord,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelector(
    exercises: List<Exercise>,
    selectedExerciseId: Int?,
    onExerciseSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = exercises.find { it.exerciseId == selectedExerciseId }?.name ?: "Select Exercise"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("Exercise") },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = {
                        onExerciseSelected(exercise.exerciseId)
                        expanded = false
                    }
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartMetricSelector(
    selectedMetric: ChartMetric,
    onMetricSelected: (ChartMetric) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = ChartMetric.entries

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        metrics.forEachIndexed { index, metric ->
            SegmentedButton(
                selected = metric == selectedMetric,
                onClick = { onMetricSelected(metric) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = metrics.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                ),
            ) {
                Text(
                    text = metric.displayName,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

