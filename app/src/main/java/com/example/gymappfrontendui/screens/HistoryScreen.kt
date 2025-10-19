@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.gymappfrontendui.screens


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gymappfrontendui.models.WorkoutHistoryItem
import com.example.gymappfrontendui.viewmodel.HistoryViewModel
// Poprawne importy dla wersji 1.4.0
import io.github.boguszpawlowski.composecalendar.SelectableCalendar
import io.github.boguszpawlowski.composecalendar.rememberSelectableCalendarState
import io.github.boguszpawlowski.composecalendar.selection.SelectionMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
private val dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
private val filterChipFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val historyList by historyViewModel.historyState.collectAsState()
    val workoutDates by historyViewModel.workoutDates.collectAsState()
    val selectedDate by historyViewModel.selectedDate.collectAsState()

    var showCalendarSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showCalendarSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCalendarSheet = false },
            sheetState = sheetState
        ) {
            CalendarView(
                activeDates = workoutDates,
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    historyViewModel.onDateSelected(date)
                    showCalendarSheet = false
                },
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Workout History",
                style = MaterialTheme.typography.headlineLarge,
            )
            IconButton(onClick = { showCalendarSheet = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Open Calendar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(visible = selectedDate != null) {
            FilterChip(
                selected = true,
                onClick = { historyViewModel.clearDateFilter() },
                label = {
                    Text(text = "Showing: ${selectedDate?.format(filterChipFormatter)}")
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear filter",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedDate != null) "No workouts recorded on this day." else "No workouts recorded yet.\nStart a new workout to see it here!",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = historyList,
                    key = { workoutItem -> workoutItem.workoutId }
                ) { workoutItem ->
                    WorkoutHistoryCard(
                        item = workoutItem,
                        onDelete = { historyViewModel.deleteWorkout(workoutItem.workoutId) },
                        onEdit = { }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    activeDates: kotlin.collections.Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val calendarState = rememberSelectableCalendarState(
        initialSelection = selectedDate?.let { listOf(it) } ?: emptyList(),
        initialSelectionMode = SelectionMode.Single
    )

    SelectableCalendar(
        calendarState = calendarState,
        modifier = modifier.padding(horizontal = 16.dp),
        dayContent = { dayState ->
            val date = dayState.date
            val hasWorkout = activeDates.contains(date)
            val isSelected = calendarState.selectionState.isDateSelected(date)

            val backgroundColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                hasWorkout -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }

            val textColor = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                hasWorkout -> MaterialTheme.colorScheme.onPrimaryContainer
                dayState.isFromCurrentMonth -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }

            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(backgroundColor)
                    .clickable(
                        enabled = dayState.isFromCurrentMonth,
                        onClick = {
                            calendarState.selectionState.onDateSelected(date)
                            onDateSelected(date)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayState.date.dayOfMonth.toString(),
                    color = textColor,
                    fontWeight = if (hasWorkout || isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    )
}

@Composable
fun WorkoutHistoryCard(
    item: WorkoutHistoryItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.workoutDate.format(dayOfWeekFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.workoutDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = item.duration,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Box(modifier = Modifier.size(40.dp)) {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Workout") },
                            onClick = {
                                onEdit()
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Workout") },
                            onClick = {
                                onDelete()
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exercise", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                Text("Sets", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                Text("Best Set", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.End)
            }

            Column {
                item.exercises.forEach { detail ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = detail.name,
                            modifier = Modifier.weight(2f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = detail.setCount.toString(),
                            modifier = Modifier.weight(0.8f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = detail.bestSet,
                            modifier = Modifier.weight(1.2f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}