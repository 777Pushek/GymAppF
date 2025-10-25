package com.example.gymappfrontendui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
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
import com.example.gymappfrontendui.viewmodel.BodyMeasurementProgressViewModel
import com.example.gymappfrontendui.viewmodel.DateRangeFilter
import com.example.gymappfrontendui.viewmodel.MeasurementType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementProgressScreen(
    navController: NavController,
    viewModel: BodyMeasurementProgressViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Measurement Progress") },
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

            MeasurementTypeSelector(
                types = state.measurementTypes,
                selectedType = state.selectedMeasurementType,
                onTypeSelected = viewModel::selectMeasurementType,
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
            } else if (state.chartValues.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val message = if (state.currentFilter == DateRangeFilter.CUSTOM) {
                        "No data found for the selected custom range."
                    } else {
                        "No data for ${state.selectedMeasurementType.displayName} in this period."
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
                        "Select a measurement type to see progress",
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
                        text = "Latest Measurement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.latestMeasurement,
                        style = MaterialTheme.typography.headlineSmall,
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
fun MeasurementTypeSelector(
    types: List<MeasurementType>,
    selectedType: MeasurementType,
    onTypeSelected: (MeasurementType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = selectedType.displayName

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
            label = { Text("Measurement Type") },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            types.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.displayName) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilterSelector(
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
            DateRangeFilter.CUSTOM -> "Custom"
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
                Text(
                    text = label,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateRangeSelectors(
    startDate: String,
    endDate: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = startDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Start Date") },
            trailingIcon = {
                IconButton(onClick = onStartDateClick) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Start Date")
                }
            },
            modifier = Modifier
                .weight(1f)
        )
        OutlinedTextField(
            value = endDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("End Date") },
            trailingIcon = {
                IconButton(onClick = onEndDateClick) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select End Date")
                }
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {
    val today = Instant.now()
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val selectedInstant = Instant.ofEpochMilli(utcTimeMillis)
                val selectedDate = selectedInstant.atZone(ZoneId.systemDefault()).toLocalDate()

                if (selectedDate.isAfter(LocalDate.now())) return false
                if (minDate != null && selectedDate.isBefore(minDate)) return false
                if (maxDate != null && selectedDate.isAfter(maxDate)) return false

                return true
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year <= LocalDate.now().year
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                }
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

