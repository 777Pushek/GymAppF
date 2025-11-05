package com.example.gymappfrontendui.screens

import androidx.compose.animation.animateColorAsState

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.gymappfrontendui.db.entity.WeekSchedule
import com.example.gymappfrontendui.models.NotificationTime
import com.example.gymappfrontendui.viewmodel.ScheduleViewModel

import kotlinx.coroutines.delay

fun NotificationTime.toDisplayString(): String {
    return when (this) {
        NotificationTime.DISABLED -> "Disabled"
        NotificationTime.ONE_MINUTE -> "1 minute before"
        NotificationTime.FIVE_MINUTES -> "5 minutes before"
        NotificationTime.FIFTEEN_MINUTES -> "15 minutes before"
        NotificationTime.THIRTY_MINUTES -> "30 minutes before"
        NotificationTime.FORTY_FIVE_MINUTES -> "45 minutes before"
        NotificationTime.ONE_HOUR -> "1 hour before"
        NotificationTime.TWO_HOURS -> "2 hours before"
        NotificationTime.THREE_HOURS -> "3 hours before"
        NotificationTime.FOUR_HOURS -> "4 hours before"
        NotificationTime.FIVE_HOURS -> "5 hours before"
        NotificationTime.SIX_HOURS -> "6 hours before"
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleListScreen(
    navController: NavController,
    scheduleViewModel: ScheduleViewModel = viewModel()
) {
    val schedules by scheduleViewModel.weekSchedules.collectAsState(initial = emptyList())
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Schedules") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Schedule")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (schedules.isEmpty()) {
                item {
                    Text(
                        "No schedules created yet. Tap '+' to add one.",
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(schedules, key = { it.weekScheduleId }) { schedule ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                scheduleViewModel.deleteSchedule(schedule)
                                true
                            } else {
                                false
                            }
                        },
                        positionalThreshold = { distance -> distance * 0.5f }
                    )
                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            delay(300)
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                    else -> Color.Transparent
                                }, label = "Swipe Background Color"
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
                        ScheduleCard(
                            schedule = schedule,
                            onActivateClick = { scheduleViewModel.setActiveSchedule(schedule) },
                            onClick = {
                                navController.navigate(com.example.gymappfrontendui.Routes.scheduleDetailRoute(schedule.weekScheduleId))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateScheduleDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, notificationTime ->
                scheduleViewModel.createSchedule(name, notificationTime)
                showCreateDialog = false
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleCard(
    schedule: WeekSchedule,
    onActivateClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = if (schedule.selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (schedule.selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Active Schedule",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp).padding(end = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(28.dp))
            }

            Text(
                text = schedule.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                color = if (schedule.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )

            if (!schedule.selected) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text("Set as active schedule") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = onActivateClick) {
                        Icon(
                            Icons.Outlined.Add,
                            contentDescription = "Set as active",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "View details",
                tint = if (schedule.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, NotificationTime) -> Unit
) {
    var scheduleName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    val notificationOptions = remember { NotificationTime.values().toList() }
    var selectedNotificationTime by remember { mutableStateOf(NotificationTime.DISABLED) }
    var notificationDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = scheduleName,
                    onValueChange = { scheduleName = it; nameError = false },
                    label = { Text("Schedule Name") },
                    singleLine = true,
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (nameError) { }

                ExposedDropdownMenuBox(
                    expanded = notificationDropdownExpanded,
                    onExpandedChange = { notificationDropdownExpanded = !notificationDropdownExpanded }
                ) {
                    TextField(
                        value = selectedNotificationTime.toDisplayString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Notification Time (Before)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = notificationDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = notificationDropdownExpanded,
                        onDismissRequest = { notificationDropdownExpanded = false }
                    ) {
                        notificationOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.toDisplayString()) },
                                onClick = {
                                    selectedNotificationTime = option
                                    notificationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (scheduleName.isNotBlank()) {
                    onConfirm(scheduleName, selectedNotificationTime)
                } else {
                    nameError = true
                }
            }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}