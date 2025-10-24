package com.example.gymappfrontendui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.viewmodel.BodyMeasurementsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    navController: NavController,
    viewModel: BodyMeasurementsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements") },
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
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.saveMeasurements()
                        },
                        enabled = !state.isLoading && state.error?.contains("User not found") != true
                    ) {
                        Text("Save Measurements")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoading && state.measurementId == 0) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Text(
                    text = state.date.takeUnless { it.isBlank() } ?: "Loading date...",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                if (state.error != null) {
                    Text(
                        text = state.error ?: "An unknown error occurred.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MeasurementRow(label = "Weight", unit = "kg", value = state.weight, onValueChange = viewModel::updateWeight, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Waist", unit = "cm", value = state.waist, onValueChange = viewModel::updateWaist, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Chest", unit = "cm", value = state.chest, onValueChange = viewModel::updateChest, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Hips", unit = "cm", value = state.hips, onValueChange = viewModel::updateHips, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Arm", unit = "cm", value = state.arm, onValueChange = viewModel::updateArm, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Forearm", unit = "cm", value = state.forearm, onValueChange = viewModel::updateForearm, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Thigh", unit = "cm", value = state.thigh, onValueChange = viewModel::updateThigh, imeAction = ImeAction.Next)
                    MeasurementRow(label = "Calf", unit = "cm", value = state.calf, onValueChange = viewModel::updateCalf, imeAction = ImeAction.Done, isLastField = true)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementRow(
    label: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    isLastField: Boolean = false
) {
    val focusManager = LocalFocusManager.current

    Column {
        ListItem(
            headlineContent = { Text(label, style = MaterialTheme.typography.bodyLarge) },
            trailingContent = {
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            onValueChange(newValue)
                        }
                    },
                    modifier = Modifier.widthIn(min = 80.dp, max = 120.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (isLastField) focusManager.clearFocus()
                    }),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
                    shape = RoundedCornerShape(8.dp),
                    suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ),
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(vertical = 4.dp)
        )
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
