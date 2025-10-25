package com.example.gymappfrontendui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.viewmodel.BodyMeasurementsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    navController: NavController,
    viewModel: BodyMeasurementsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }

    val currentMillis = remember(state.isoDate) {
        try {
            LocalDate.parse(state.isoDate, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            Instant.now().toEpochMilli()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentMillis,
        yearRange = IntRange(LocalDate.now().year - 5, LocalDate.now().year),

        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= Instant.now().toEpochMilli()
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year <= LocalDate.now().year
            }
        }
    )

    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Measurements saved successfully!")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            showDatePicker = false
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                val selectedDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                viewModel.selectDate(selectedDate)
                            }
                        }
                    ) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(
                    state = datePickerState
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateNavigator(
                    date = state.date,
                    onPrevious = { viewModel.goToPreviousDay() },
                    onNext = { viewModel.goToNextDay() },
                    onDateClick = { showDatePicker = true },
                    isNextEnabled = state.isoDate != LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                MeasurementInputField(
                    label = "Weight",
                    unit = "kg",
                    value = state.weight,
                    onValueChange = viewModel::updateWeight,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Waist",
                    unit = "cm",
                    value = state.waist,
                    onValueChange = viewModel::updateWaist,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Chest",
                    unit = "cm",
                    value = state.chest,
                    onValueChange = viewModel::updateChest,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Arm",
                    unit = "cm",
                    value = state.arm,
                    onValueChange = viewModel::updateArm,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Forearm",
                    unit = "cm",
                    value = state.forearm,
                    onValueChange = viewModel::updateForearm,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Thigh",
                    unit = "cm",
                    value = state.thigh,
                    onValueChange = viewModel::updateThigh,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Calf",
                    unit = "cm",
                    value = state.calf,
                    onValueChange = viewModel::updateCalf,
                    modifier = Modifier.fillMaxWidth()
                )
                MeasurementInputField(
                    label = "Hips",
                    unit = "cm",
                    value = state.hips,
                    onValueChange = viewModel::updateHips,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveMeasurements() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !state.isLoading
                ) {
                    Text("Save Measurements")
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun DateNavigator(
    date: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDateClick: () -> Unit,
    isNextEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }

        Row(
            modifier = Modifier.clickable(onClick = onDateClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
            Text(
                text = date.ifEmpty { "Loading..." },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        IconButton(onClick = onNext, enabled = isNextEnabled) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
        }
    }
}
@Composable
private fun MeasurementInputField(
    label: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(unit) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}