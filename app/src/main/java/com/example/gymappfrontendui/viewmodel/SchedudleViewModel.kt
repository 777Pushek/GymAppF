package com.example.gymappfrontendui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymappfrontendui.db.entity.ScheduledWorkout
import com.example.gymappfrontendui.db.entity.WeekSchedule
import com.example.gymappfrontendui.db.entity.WorkoutTemplate
import com.example.gymappfrontendui.models.NotificationTime
import com.example.gymappfrontendui.models.DayOfWeek
import com.example.gymappfrontendui.repository.ScheduledWorkoutRepository
import com.example.gymappfrontendui.repository.UserRepository
import com.example.gymappfrontendui.repository.WeekScheduleRepository
import com.example.gymappfrontendui.repository.WorkoutTemplateRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class ScheduleDetailState(
    val schedule: WeekSchedule? = null,
    val scheduledWorkouts: Map<DayOfWeek, List<ScheduledWorkoutWithTemplate>> = emptyMap(),
    val isLoading: Boolean = true
)

data class ScheduledWorkoutWithTemplate(
    val scheduledWorkout: ScheduledWorkout,
    val templateName: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val weekScheduleRepository = WeekScheduleRepository(application)
    private val scheduledWorkoutRepository = ScheduledWorkoutRepository(application)
    private val userRepository = UserRepository(application)
    private val workoutTemplateRepository = WorkoutTemplateRepository(application)

    private val userIdFlow: Flow<Int?> = flow {
        emit(userRepository.getLoggedInUserId() ?: userRepository.getGuestUserId())
    }.shareIn(viewModelScope, SharingStarted.WhileSubscribed(5000), replay = 1)

    val weekSchedules: Flow<List<WeekSchedule>> = userIdFlow.filterNotNull().flatMapLatest { userId ->
        weekScheduleRepository.getWeekSchedulesByUserId(userId)
    }

    private val _scheduleDetailState = MutableStateFlow(ScheduleDetailState())
    val scheduleDetailState: StateFlow<ScheduleDetailState> = _scheduleDetailState.asStateFlow()

    val availableTemplates: StateFlow<List<WorkoutTemplate>> =
        workoutTemplateRepository.getAvailableWorkoutTemplates()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadScheduleDetails(scheduleId: Int) {
        viewModelScope.launch {
            _scheduleDetailState.value = ScheduleDetailState(isLoading = true)

            val userId = userIdFlow.firstOrNull()
            if (userId == null) {
                _scheduleDetailState.value = ScheduleDetailState(isLoading = false, schedule = null)
                return@launch
            }

            weekScheduleRepository.getWeekSchedulesWithScheduleWorkouts(userId)
                .collectLatest { allSchedulesWithWorkouts ->
                    val targetScheduleWithWorkouts = allSchedulesWithWorkouts.find {
                        it.weekSchedule.weekScheduleId == scheduleId
                    }

                    if (targetScheduleWithWorkouts != null) {
                        val workoutsWithNames = targetScheduleWithWorkouts.scheduledWorkouts.map { sw ->
                            val template = workoutTemplateRepository.getWorkoutTemplateById(sw.workoutTemplateId).firstOrNull()
                            ScheduledWorkoutWithTemplate(sw, template?.name)
                        }
                        val groupedByDay = workoutsWithNames.groupBy { it.scheduledWorkout.day }
                            .mapValues { (_, workouts) ->
                                workouts.sortedBy {
                                    try {
                                        it.scheduledWorkout.time?.let { timeStr -> LocalTime.parse(timeStr) } ?: LocalTime.MIN
                                    } catch (e: DateTimeParseException) {
                                        LocalTime.MIN
                                    }
                                }
                            }

                        _scheduleDetailState.value = ScheduleDetailState(
                            schedule = targetScheduleWithWorkouts.weekSchedule,
                            scheduledWorkouts = groupedByDay,
                            isLoading = false
                        )
                    } else {

                        _scheduleDetailState.value = ScheduleDetailState(isLoading = false, schedule = null)
                    }
                }
        }
    }

    fun createSchedule(name: String, notificationTime: NotificationTime) {
        viewModelScope.launch {
            val newSchedule = WeekSchedule(
                name = name,
                userId = 0,
                notificationTime = notificationTime,
                selected = false
            )
            weekScheduleRepository.insertWeekSchedule(newSchedule)

        }
    }

    fun deleteSchedule(schedule: WeekSchedule) {
        viewModelScope.launch {
            weekScheduleRepository.deleteWeekSchedule(schedule)
        }
    }

    fun setActiveSchedule(scheduleToActivate: WeekSchedule) {
        viewModelScope.launch {
            val userId = userIdFlow.firstOrNull() ?: return@launch
            val allSchedules = weekScheduleRepository.getWeekSchedulesByUserId(userId).first()

            allSchedules.forEach { schedule ->
                val shouldBeSelected = schedule.weekScheduleId == scheduleToActivate.weekScheduleId
                if (schedule.selected != shouldBeSelected) {
                    weekScheduleRepository.updateWeekSchedule(schedule.copy(selected = shouldBeSelected))
                }
            }
        }
    }
    fun addScheduledWorkout(
        scheduleId: Int,
        templateId: Int,
        day: DayOfWeek,
        time: LocalTime?
    ) {
        viewModelScope.launch {
            val timeString = time?.format(DateTimeFormatter.ISO_LOCAL_TIME)

            val scheduledWorkout = ScheduledWorkout(
                workoutTemplateId = templateId,
                day = day,
                weekScheduleId = scheduleId,
                time = timeString
            )
            scheduledWorkoutRepository.insertScheduledWorkout(scheduledWorkout)
            loadScheduleDetails(scheduleId)
        }
    }

    fun deleteScheduledWorkout(scheduledWorkout: ScheduledWorkout) {
        viewModelScope.launch {
            scheduledWorkoutRepository.deleteScheduledWorkout(scheduledWorkout)
            loadScheduleDetails(scheduledWorkout.weekScheduleId)
        }
    }
}