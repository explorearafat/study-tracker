package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val subjectDao = db.subjectDao()
    private val taskDao = db.taskDao()
    private val sessionDao = db.studySessionDao()

    val subjects: StateFlow<List<Subject>> = subjectDao.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySession>> = sessionDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Seed initial subjects and tasks if empty
        viewModelScope.launch {
            subjectDao.getAllSubjects().first().let { currentSubjects ->
                if (currentSubjects.isEmpty()) {
                    val initialSubjects = listOf(
                        Subject("sub_1", "Mathematics", "#2196F3", 300, "calculate"),
                        Subject("sub_2", "Physics", "#FF9800", 240, "science"),
                        Subject("sub_3", "Computer Science", "#00BCD4", 360, "code"),
                        Subject("sub_4", "World History", "#4CAF50", 180, "history_edu"),
                        Subject("sub_5", "Literature & Arts", "#E91E63", 200, "menu_book")
                    )
                    initialSubjects.forEach { subjectDao.insertSubject(it) }

                    val initialTasks = listOf(
                        Task("t1", "Solve Calculus Chapter 4 Homework", "sub_1", "Mathematics", "#2196F3", false, "Today", 45),
                        Task("t2", "Read Quantum Mechanics Lab Notes", "sub_2", "Physics", "#FF9800", false, "Tomorrow", 30),
                        Task("t3", "Implement Jetpack Compose UI", "sub_3", "Computer Science", "#00BCD4", true, "Yesterday", 60),
                        Task("t4", "Review Industrial Revolution Essay", "sub_4", "World History", "#4CAF50", false, "Friday", 40),
                        Task("t5", "Analyze Hamlet Act 3 Soliloquy", "sub_5", "Literature & Arts", "#E91E63", false, "Next Week", 25)
                    )
                    initialTasks.forEach { taskDao.insertTask(it) }

                    val initialSessions = listOf(
                        StudySession("s1", "sub_1", "Mathematics", "#2196F3", 45, System.currentTimeMillis() - 86400000 * 1),
                        StudySession("s2", "sub_3", "Computer Science", "#00BCD4", 60, System.currentTimeMillis() - 86400000 * 2),
                        StudySession("s3", "sub_2", "Physics", "#FF9800", 30, System.currentTimeMillis() - 86400000 * 3),
                        StudySession("s4", "sub_4", "World History", "#4CAF50", 50, System.currentTimeMillis() - 86400000 * 4),
                        StudySession("s5", "sub_1", "Mathematics", "#2196F3", 50, System.currentTimeMillis() - 86400000 * 5)
                    )
                    initialSessions.forEach { sessionDao.insertSession(it) }
                }
            }
        }
    }

    fun addSubject(name: String, colorHex: String, targetMinutes: Int, iconName: String = "book") {
        viewModelScope.launch {
            val id = "sub_" + UUID.randomUUID().toString().take(8)
            val subject = Subject(id, name, colorHex, targetMinutes, iconName)
            subjectDao.insertSubject(subject)
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.updateSubject(subject)
            // Cascade updated color and name to linked tasks and sessions
            taskDao.updateTaskSubjectDetails(subject.id, subject.name, subject.colorHex)
            sessionDao.updateSessionSubjectDetails(subject.id, subject.name, subject.colorHex)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectDao.deleteSubject(subject)
        }
    }

    fun addTask(title: String, subject: Subject, dueDate: String = "Today", estimatedMins: Int = 30) {
        viewModelScope.launch {
            val task = Task(
                id = "task_" + UUID.randomUUID().toString().take(8),
                title = title,
                subjectId = subject.id,
                subjectName = subject.name,
                subjectColorHex = subject.colorHex,
                isCompleted = false,
                dueDate = dueDate,
                estimatedMinutes = estimatedMins
            )
            taskDao.insertTask(task)
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            taskDao.updateTask(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun logFocusSession(subject: Subject, durationMinutes: Int) {
        viewModelScope.launch {
            val session = StudySession(
                id = "sess_" + UUID.randomUUID().toString().take(8),
                subjectId = subject.id,
                subjectName = subject.name,
                subjectColorHex = subject.colorHex,
                durationMinutes = durationMinutes,
                timestamp = System.currentTimeMillis()
            )
            sessionDao.insertSession(session)
        }
    }
}
