package com.example.gymappfrontendui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymappfrontendui.viewmodel.DateRangeFilter
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AppLineChart(
    values: List<Double>,
    labels: List<String>,
    metricLabel: String
) {
    if (values.isEmpty()) {
        Log.w("AppLineChart", "No values provided to chart.")
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val minValue = 0.0
    val maxValue = remember(values) { (values.maxOrNull()?.let { it * 1.1 } ?: 100.0) }

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
            data = remember(values, primaryColor, onPrimaryColor, metricLabel) {
                listOf(
                    Line(
                        label = metricLabel,
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
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = onSurfaceVariantColor,
                    fontSize = 10.sp
                )
            ),
            animationMode = AnimationMode.Together()
        )
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
            modifier = Modifier.weight(1f)
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
            modifier = Modifier.weight(1f)
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