package com.example.gymappfrontendui.screens

import android.util.Log
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.db.entity.Exercise
import com.example.gymappfrontendui.viewmodel.DateRangeFilter
import com.example.gymappfrontendui.viewmodel.WorkoutProgressViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.*
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutProgressScreen(
    navController: NavController,
    viewModel: WorkoutProgressViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Progress") },
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
                    Text(
                        "No data for ${state.selectedExerciseName} in this period.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else if (state.chartValues.isNotEmpty()) {
                WorkoutLineChartEhsan(
                    values = state.chartValues,
                    labels = state.chartXAxisLabels
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
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
fun DateRangeFilterSelector(
    currentFilter: DateRangeFilter,
    onFilterSelected: (DateRangeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = DateRangeFilter.entries.associateWith {
        when (it) {
            DateRangeFilter.MONTH_1 -> "1M"
            DateRangeFilter.MONTH_3 -> "3M"
            DateRangeFilter.MONTH_6 -> "6M"
            DateRangeFilter.YEAR_1 -> "1Y"
            DateRangeFilter.ALL_TIME -> "All"
        }
    }

    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        filters.forEach { (filter, label) ->
            SegmentedButton(
                selected = filter == currentFilter,
                onClick = { onFilterSelected(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = filters.keys.indexOf(filter),
                    count = filters.size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    inactiveContainerColor = MaterialTheme.colorScheme.surface,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurface,
                    inactiveBorderColor = MaterialTheme.colorScheme.outline
                ),
            ) {
                Text(label)
            }
        }
    }
}
@Composable
fun WorkoutLineChartEhsan(
    values: List<Double>,
    labels: List<String>
) {
    if (values.isEmpty()) {
        Log.w("WorkoutLineChartEhsan", "No values provided to chart.")
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val maxValue = remember(values) { (values.maxOrNull()?.let { it * 1.1 } ?: 100.0) }
    val minValue = 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        LineChart(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
            data = remember(values, primaryColor, onPrimaryColor) {
                listOf(
                    Line(
                        label = "Volume",
                        values = values,
                        color = SolidColor(primaryColor),
                        firstGradientFillColor = primaryColor.copy(alpha = 0.3f),
                        secondGradientFillColor = Color.Transparent,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                        dotProperties = DotProperties(
                            enabled = true,
                            color = SolidColor(onPrimaryColor),
                            strokeWidth = 1.5.dp,
                            radius = 4.dp,
                            strokeColor = SolidColor(primaryColor)
                        )
                    )
                )
            },
            minValue = minValue,
            maxValue = maxValue,
            dividerProperties = DividerProperties(
                enabled = true,
                xAxisProperties = LineProperties(
                    enabled = true,
                    color = SolidColor(outlineVariantColor),
                    thickness = 1.dp
                ),
                yAxisProperties = LineProperties(
                    enabled = true,
                    color = SolidColor(outlineVariantColor),
                    thickness = 1.dp
                )
            ),
            gridProperties = GridProperties(
                enabled = true,
                xAxisProperties = GridProperties.AxisProperties(enabled = false),
                yAxisProperties = GridProperties.AxisProperties(
                    enabled = true,
                    color = SolidColor(outlineVariantColor.copy(alpha = 0.5f)),
                    thickness = 1.dp,
                )
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = onSurfaceVariantColor,
                    fontSize = 10.sp
                ),
                padding = 4.dp,
                labels = labels
            ),
            animationMode = AnimationMode.Together()
        )
    }
}