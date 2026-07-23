package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.StudySession
import com.example.data.model.Subject
import com.example.data.model.Task
import com.example.data.model.UserProfile
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class PomodoroMode(val label: String) {
    WORK("Focus Work"),
    SHORT_BREAK("Short Break"),
    LONG_BREAK("Long Break")
}

data class TimerUiState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val mode: PomodoroMode = PomodoroMode.WORK,
    val selectedSubjectId: Int? = null,
    val sessionNotes: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    val subjects: StateFlow<List<Subject>>
    val sessions: StateFlow<List<StudySession>>
    val tasks: StateFlow<List<Task>>
    val userProfile: StateFlow<UserProfile?>

    private val _timerUiState = MutableStateFlow(TimerUiState())
    val timerUiState: StateFlow<TimerUiState> = _timerUiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudyRepository(
            db.subjectDao(),
            db.studySessionDao(),
            db.taskDao(),
            db.userProfileDao()
        )

        subjects = repository.allSubjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        sessions = repository.allSessions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        tasks = repository.allTasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        userProfile = repository.userProfile
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                UserProfile()
            )

        // Sync default subject for timer when subjects load
        viewModelScope.launch {
            subjects.collect { list ->
                if (list.isNotEmpty() && _timerUiState.value.selectedSubjectId == null) {
                    _timerUiState.update { it.copy(selectedSubjectId = list.first().id) }
                }
            }
        }

        // Sync pomodoro default work time if user profile changes
        viewModelScope.launch {
            userProfile.collect { profile ->
                profile?.let { p ->
                    if (!_timerUiState.value.isRunning) {
                        val duration = when (_timerUiState.value.mode) {
                            PomodoroMode.WORK -> p.pomodoroWorkMinutes * 60
                            PomodoroMode.SHORT_BREAK -> p.pomodoroBreakMinutes * 60
                            PomodoroMode.LONG_BREAK -> p.pomodoroLongBreakMinutes * 60
                        }
                        _timerUiState.update {
                            it.copy(
                                remainingSeconds = duration,
                                totalSeconds = duration
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Subject Operations ---
    fun addSubject(name: String, category: String, colorHex: Long, targetMinutes: Int, iconName: String) {
        viewModelScope.launch {
            repository.insertSubject(
                Subject(
                    name = name,
                    category = category,
                    colorHex = colorHex,
                    targetDailyMinutes = targetMinutes,
                    iconName = iconName
                )
            )
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // --- Task Operations ---
    fun addTask(title: String, description: String, subjectId: Int?, priority: String, dueDateMs: Long?) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    title = title,
                    description = description,
                    subjectId = subjectId,
                    priority = priority,
                    dueDateEpochMs = dueDateMs,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Manual Session Logging ---
    fun logManualSession(subjectId: Int, durationMinutes: Int, notes: String) {
        viewModelScope.launch {
            repository.insertSession(
                StudySession(
                    subjectId = subjectId,
                    durationSeconds = durationMinutes * 60,
                    timestamp = System.currentTimeMillis(),
                    sessionType = "Manual",
                    notes = notes
                )
            )
        }
    }

    fun deleteSession(session: StudySession) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    // --- Profile & Preferences Operations ---
    fun updateProfile(
        name: String,
        academicLevel: String,
        motto: String,
        targetDailyHours: Float,
        avatarUri: String,
        workMins: Int,
        breakMins: Int,
        longBreakMins: Int
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveUserProfile(
                current.copy(
                    name = name,
                    academicLevel = academicLevel,
                    motto = motto,
                    targetDailyHours = targetDailyHours,
                    avatarUri = avatarUri,
                    pomodoroWorkMinutes = workMins,
                    pomodoroBreakMinutes = breakMins,
                    pomodoroLongBreakMinutes = longBreakMins
                )
            )
        }
    }

    fun toggleDarkMode() {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
            repository.saveUserProfile(current.copy(isDarkMode = !current.isDarkMode))
        }
    }

    // --- Pomodoro Timer Control ---
    fun selectTimerSubject(subjectId: Int) {
        _timerUiState.update { it.copy(selectedSubjectId = subjectId) }
    }

    fun setTimerMode(mode: PomodoroMode) {
        timerJob?.cancel()
        val profile = userProfile.value ?: UserProfile()
        val mins = when (mode) {
            PomodoroMode.WORK -> profile.pomodoroWorkMinutes
            PomodoroMode.SHORT_BREAK -> profile.pomodoroBreakMinutes
            PomodoroMode.LONG_BREAK -> profile.pomodoroLongBreakMinutes
        }
        val duration = mins * 60
        _timerUiState.update {
            it.copy(
                isRunning = false,
                mode = mode,
                remainingSeconds = duration,
                totalSeconds = duration
            )
        }
    }

    fun toggleTimer() {
        if (_timerUiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timerUiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_timerUiState.value.isRunning && _timerUiState.value.remainingSeconds > 0) {
                delay(1000L)
                _timerUiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            if (_timerUiState.value.remainingSeconds == 0) {
                onTimerFinished()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerUiState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        val profile = userProfile.value ?: UserProfile()
        val mins = when (_timerUiState.value.mode) {
            PomodoroMode.WORK -> profile.pomodoroWorkMinutes
            PomodoroMode.SHORT_BREAK -> profile.pomodoroBreakMinutes
            PomodoroMode.LONG_BREAK -> profile.pomodoroLongBreakMinutes
        }
        val duration = mins * 60
        _timerUiState.update {
            it.copy(
                isRunning = false,
                remainingSeconds = duration,
                totalSeconds = duration
            )
        }
    }

    private fun onTimerFinished() {
        val currentState = _timerUiState.value
        _timerUiState.update { it.copy(isRunning = false) }

        // Automatically log completed work session if subject selected
        if (currentState.mode == PomodoroMode.WORK && currentState.selectedSubjectId != null) {
            val sessionSeconds = currentState.totalSeconds
            if (sessionSeconds > 0) {
                viewModelScope.launch {
                    repository.insertSession(
                        StudySession(
                            subjectId = currentState.selectedSubjectId,
                            durationSeconds = sessionSeconds,
                            timestamp = System.currentTimeMillis(),
                            sessionType = "Pomodoro",
                            notes = "Completed ${currentState.mode.label} session"
                        )
                    )
                }
            }
        }
    }
}
